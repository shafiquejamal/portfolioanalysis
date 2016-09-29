package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, PortfolioDesign}
import org.apache.spark.sql.{Dataset, SparkSession}

class PortfolioSimulation(
    investmentDurationInYears: Int,
    rebalancingInterval: RebalancingInterval,
    initialInvestment: BigDecimal,
    perTransactionTradingCost: BigDecimal,
    bidAskCostFractionOfNav: BigDecimal,
    portfolioDesign: PortfolioDesign,
    maxAllowedDeviation: BigDecimal,
    sortedCommonDatesDataset:Dataset[ETFDataPlus]) {

  def simulate(implicit sparkSession: SparkSession) = {
    val investmentPeriods =
      new InvestmentPeriodsCreator(portfolioDesign, sortedCommonDatesDataset, investmentDurationInYears).create

    val temp = investmentPeriods.map { investmentPeriod =>
      val foo = new Investment(
          investmentPeriod,
          rebalancingInterval,
          initialInvestment,
          perTransactionTradingCost,
          bidAskCostFractionOfNav,
          portfolioDesign,
          maxAllowedDeviation,
          sortedCommonDatesDataset
          ).rebalancePortfolio
      (foo.investmentPeriod, foo.averageAnnualReturnFraction, foo.endOfPeriodSnapshot)
    }

    temp
  }
}
