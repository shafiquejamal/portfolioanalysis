package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.investment.InvestmentPeriod._
import com.eigenroute.portfolioanalysis.rebalancing._
import org.apache.spark.sql.{Dataset, SparkSession}

class Investment(
    investmentPeriod: InvestmentPeriod,
    rebalancingInterval: RebalancingInterval,
    initialInvestment: BigDecimal,
    perTransactionTradingCost: BigDecimal,
    bidAskCostFractionOfNav: BigDecimal,
    portfolioDesign: PortfolioDesign,
    maxAllowedDeviation: BigDecimal,
    commonDatesDataset:Dataset[ETFDataPlus]) {

  val totalNumberOfRebalancingIntervals: Int = lengthInMonths(investmentPeriod) / rebalancingInterval.months
  val sortedDatasetsSplitByRebalancingPeriod: Seq[Dataset[ETFDataPlus]] =
    1.to(totalNumberOfRebalancingIntervals).map { rebalancingIntervalNumber =>
      val monthsToAddToStartOfPeriod = (rebalancingIntervalNumber - 1) * rebalancingInterval.months
      val startOfPeriod = investmentPeriod.startDate.plusMonths(monthsToAddToStartOfPeriod)
      val endOfPeriod = startOfPeriod.plusMonths(rebalancingInterval)
      commonDatesDataset.filter(eTFData =>
        eTFData.asOfDate.getTime >= startOfPeriod.getMillis &
        eTFData.asOfDate.getTime < endOfPeriod.getMillis)
      }.toSeq

  val temp1 = sortedDatasetsSplitByRebalancingPeriod.map(_.collect().toList)
  val temp2 = temp1

  def run()(sparkSession: SparkSession): RebalancedPortfolio = {

    import sparkSession.implicits._

    sortedDatasetsSplitByRebalancingPeriod
      .foldLeft[RebalancedPortfolio](RebalancedPortfolio(Seq[ETFDataPlus]().toDS, Seq(), 0d, initialInvestment))
        { case (rebalancedPortfolio, datasetForRebalancingPeriod) =>

      val datasetForRebalancingPeriodWithQuantitiesFromPrevious = datasetForRebalancingPeriod.map { eTFData =>
        eTFData.copy(
          quantity =
            rebalancedPortfolio.newQuantitiesChosenForThisRebalancing
            .find(_.eTFCode == eTFData.eTFCode).fold(0d) { portfolioQuantityToHave =>
              portfolioQuantityToHave.quantity.toDouble},
          cash = rebalancedPortfolio.accumulatedCash
          )
      }.persist()

      val commonDatesDatasets = portfolioDesign.eTFSelections.map { selection =>
        datasetForRebalancingPeriodWithQuantitiesFromPrevious.filter(_.eTFCode == selection.eTFCode)
      }

      val finalQuantitiesAndCash =
        new PortfolioRebalancer(
          portfolioDesign,
          PortfolioSnapshot(portfolioDesign, commonDatesDatasets),
          bidAskCostFractionOfNav,
          maxAllowedDeviation,
          perTransactionTradingCost,
          rebalancedPortfolio.accumulatedExDiv,
          rebalancedPortfolio.accumulatedCash).finalQuantities
      val finalQuantities = finalQuantitiesAndCash.quantitiesToHave
      val updatedDatasetForRebalancingPeriod = datasetForRebalancingPeriodWithQuantitiesFromPrevious.map { eTFData =>
        eTFData.copy(
          quantity = finalQuantities.find(_.eTFCode == eTFData.eTFCode).fold(0d){ portfolioQuantityToHave =>
            portfolioQuantityToHave.quantity.toDouble},
          cash = finalQuantitiesAndCash.cashRemaining)
      }
      val accumulatedExDiv = datasetForRebalancingPeriod.map(_.exDividend.toDouble).collect().sum

      RebalancedPortfolio(
        updatedDatasetForRebalancingPeriod.union(rebalancedPortfolio.rebalancedDataset),
        finalQuantities,
        accumulatedExDiv,
        finalQuantitiesAndCash.cashRemaining)
    }
  }
}
