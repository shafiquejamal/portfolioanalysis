package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.investment.InvestmentPeriod._
import com.eigenroute.portfolioanalysis.rebalancing._

class Investment(
    investmentPeriod: InvestmentPeriod,
    rebalancingInterval: RebalancingInterval,
    initialInvestment: BigDecimal,
    perTransactionTradingCost: BigDecimal,
    bidAskCostFractionOfNav: BigDecimal,
    portfolioDesign: PortfolioDesign,
    maxAllowedDeviation: BigDecimal,
    commonDatesETFData:Seq[ETFDataPlus]) {

  val totalNumberOfRebalancingIntervals: Int = lengthInMonths(investmentPeriod) / rebalancingInterval.months
  val sortedETFDataSplitByRebalancingPeriod: Seq[Seq[ETFDataPlus]] =
    1.to(totalNumberOfRebalancingIntervals).map { rebalancingIntervalNumber =>
      val monthsToAddToStartOfPeriod = (rebalancingIntervalNumber - 1) * rebalancingInterval.months
      val startOfPeriod = investmentPeriod.startDate.plusMonths(monthsToAddToStartOfPeriod)
      val endOfPeriod = startOfPeriod.plusMonths(rebalancingInterval)
      commonDatesETFData.filter(eTFData =>
        eTFData.asOfDate.getTime >= startOfPeriod.getMillis &
        eTFData.asOfDate.getTime < endOfPeriod.getMillis)
      }.toSeq

  def rebalancePortfolio: RebalancedPortfolio = {

    val finalRebalancedPortfolio: RebalancedPortfolio =
      sortedETFDataSplitByRebalancingPeriod.foldLeft[RebalancedPortfolio](
        RebalancedPortfolio(
          Seq[ETFDataPlus](),
          Seq(),
          0d,
          initialInvestment,
          PortfolioSnapshot(Seq()), initialInvestment, PortfolioPerformance(investmentPeriod, BigDecimal(0))))
        { case (rebalancedPortfolio, eTFDataForRebalancingPeriod) =>

      val eTFDataForRebalancingPeriodWithQuantitiesFromPrevious: Seq[ETFDataPlus] =
        eTFDataForRebalancingPeriod.map { eTFData =>
        eTFData.copy(
          quantity =
            rebalancedPortfolio.newQuantitiesChosenForThisRebalancing
            .find(_.eTFCode == eTFData.eTFCode).fold(0d) { portfolioQuantityToHave =>
              portfolioQuantityToHave.quantity.toDouble},
          cash = rebalancedPortfolio.accumulatedCash)
        }

      val sameDateUniqueCodesETFDatasBeforeRebalancing: Seq[ETFDataPlus] =
        portfolioDesign.eTFSelections.flatMap { selection =>
          eTFDataForRebalancingPeriodWithQuantitiesFromPrevious.toSeq.find(_.eTFCode == selection.eTFCode)
      }

      val finalQuantitiesAndCash: FinalPortfolioQuantitiesToHave =
        new PortfolioRebalancer(
          portfolioDesign,
          PortfolioSnapshot(sameDateUniqueCodesETFDatasBeforeRebalancing),
          bidAskCostFractionOfNav,
          maxAllowedDeviation,
          perTransactionTradingCost,
          rebalancedPortfolio.accumulatedExDiv,
          rebalancedPortfolio.accumulatedCash).finalQuantities
      val finalQuantities: Seq[FinalPortfolioQuantityToHave] = finalQuantitiesAndCash.quantitiesToHave
      val updatedETFDataForRebalancingPeriod: Seq[ETFDataPlus] =
        eTFDataForRebalancingPeriodWithQuantitiesFromPrevious.map { eTFData =>
          eTFData.copy(
            quantity = finalQuantities.find(_.eTFCode == eTFData.eTFCode).fold(0d){ portfolioQuantityToHave =>
              portfolioQuantityToHave.quantity.toDouble},
            cash = finalQuantitiesAndCash.cashRemaining)
      }
      val accumulatedExDiv = eTFDataForRebalancingPeriod.map(_.exDividend.toDouble).sum

      lazy val sameDateUniqueCodesETFDatasAfterRebalancing: Seq[ETFDataPlus] =
        portfolioDesign.eTFSelections.flatMap { selection =>
          updatedETFDataForRebalancingPeriod.reverse.find(_.eTFCode == selection.eTFCode)
      }

      val endOfPeriodSnapshot: PortfolioSnapshot =
        if (sortedETFDataSplitByRebalancingPeriod.reverse.headOption.fold(false) {_ == eTFDataForRebalancingPeriod})
          PortfolioSnapshot(sameDateUniqueCodesETFDatasAfterRebalancing)
        else
          PortfolioSnapshot(Seq())

        RebalancedPortfolio(
        updatedETFDataForRebalancingPeriod ++ rebalancedPortfolio.rebalancedETFData,
        finalQuantities,
        accumulatedExDiv,
        finalQuantitiesAndCash.cashRemaining,
        endOfPeriodSnapshot,
        initialInvestment,
        PortfolioPerformance(investmentPeriod, 0d)
      )
    }

    val liquidatedValue =
      new LiquidatedValueCalculator().liquidatedValue(
        finalRebalancedPortfolio.endOfPeriodSnapshot,
        bidAskCostFractionOfNav,
        perTransactionTradingCost,
        finalRebalancedPortfolio.accumulatedExDiv,
        finalRebalancedPortfolio.accumulatedCash)

    val totalReturnFraction: BigDecimal = (liquidatedValue / initialInvestment) - 1
    val averageAnnualReturnFraction: BigDecimal =
      math.pow((liquidatedValue / initialInvestment).toDouble,
               1d/InvestmentPeriod.lengthInYears(investmentPeriod)) - BigDecimal(1)

    finalRebalancedPortfolio.copy(
      liquidatedValue = liquidatedValue,
      totalReturnFraction = totalReturnFraction,
      portfolioPerformance = PortfolioPerformance(investmentPeriod, averageAnnualReturnFraction)
    )
  }
}
