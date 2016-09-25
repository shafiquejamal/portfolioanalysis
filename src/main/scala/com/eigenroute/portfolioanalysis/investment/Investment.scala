package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.investment.InvestmentPeriod._
import com.eigenroute.portfolioanalysis.rebalancing._
import org.apache.spark.sql.{SparkSession, Dataset}

case class RebalancedPortfolio(ds: Dataset[ETFDataPlus], accumulatedExDiv: Double, accumulatedCash: Double)

class Investment(
    investmentPeriod: InvestmentPeriod,
    rebalancingInterval: RebalancingInterval,
    initialInvestment: Double,
    perTransactionTradingCost: Double,
    bidAskCostFractionOfNav: Double,
    portfolioDesign: PortfolioDesign,
    maxAllowedDeviation: Double,
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

  def run()(sparkSession: SparkSession): RebalancedPortfolio = {

    import sparkSession.implicits._
    val portfolioRebalancer = new PortfolioRebalancer()

    val rebalanced:RebalancedPortfolio =
      sortedDatasetsSplitByRebalancingPeriod
      .foldLeft[RebalancedPortfolio](RebalancedPortfolio(Seq[ETFDataPlus]().toDS, 0d, initialInvestment))
        { case (acc, datasetForRebalancingPeriod) =>

      val commonDatesDatasets = portfolioDesign.eTFSelections.map { selection =>
        datasetForRebalancingPeriod.filter(_.eTFCode == selection.eTFCode)
      }
      val portfolioSnapshot = PortfolioSnapshot(portfolioDesign, commonDatesDatasets)
      val valueDifferences =
        new ValueDifferencesCalculator().valueDifferences(
            portfolioDesign,
            portfolioSnapshot,
            maxAllowedDeviation,
            perTransactionTradingCost,
            acc.accumulatedExDiv,
            acc.accumulatedCash)
      val firstEstimatedQuantitiesToAcquire = null
//        new FirstEstimateQuantitiesToAcquireCalculator().firstEstimateQuantitiesToAcquire(
//            portfolioDesign,
//            portfolioSnapshot,
//            valueDifferences,
//            bidAskCostFractionOfNav)
      val maxQuantities =
        portfolioRebalancer
        .maxQuantities(firstEstimatedQuantitiesToAcquire, portfolioSnapshot, acc.accumulatedExDiv, acc.accumulatedCash)
      val additionalQuantities = portfolioRebalancer.additionalQuantities(maxQuantities)
      val finalQuanitities =
        portfolioRebalancer.finalQuantities(
            portfolioDesign,
            portfolioSnapshot,
            PortfolioQuantitiesToAcquire(firstEstimatedQuantitiesToAcquire),
            additionalQuantities,
            acc.accumulatedExDiv,
            acc.accumulatedCash)
      val accumulatedExDiv = datasetForRebalancingPeriod.map(_.exDividend).collect().sum
        RebalancedPortfolio(datasetForRebalancingPeriod.union(acc.ds), accumulatedExDiv, finalQuanitities.cashRemaining)
    }
    rebalanced
  }
}
