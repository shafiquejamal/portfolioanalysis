package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.investment.InvestmentPeriod._
import com.eigenroute.portfolioanalysis.rebalancing.{PortfolioSnapshot, ETFDataPlus, PortfolioDesign}
import org.apache.spark.sql.Dataset
import org.joda.time.DateTime

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
  val datasetsByRebalancingPeriod: Seq[Dataset[ETFDataPlus]] =
    1.to(totalNumberOfRebalancingIntervals).map { rebalancingIntervalNumber =>
    val monthsToAddToStartOfPeriod = (rebalancingIntervalNumber-1)* rebalancingInterval.months
    val startOfPeriod = investmentPeriod.startDate.plusMonths(monthsToAddToStartOfPeriod)
    val endOfPeriod = startOfPeriod.plusMonths(rebalancingInterval)
    commonDatesDataset.filter(eTFData =>
      eTFData.asOfDate.getTime >= startOfPeriod.getMillis &
      eTFData.asOfDate.getTime < endOfPeriod.getMillis).orderBy("asOfDate")
  }.toSeq

}
