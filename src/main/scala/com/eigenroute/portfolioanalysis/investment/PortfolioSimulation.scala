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
    sortedCommonDatesETFData:Seq[ETFDataPlus]) {

  def simulate(implicit sparkSession: SparkSession) = {
    val investmentPeriods =
      new InvestmentPeriodsCreator(portfolioDesign, sortedCommonDatesETFData, investmentDurationInYears).create

    val temp = investmentPeriods.map { investmentPeriod =>
      val foo = new Investment(
          investmentPeriod,
          rebalancingInterval,
          initialInvestment,
          perTransactionTradingCost,
          bidAskCostFractionOfNav,
          portfolioDesign,
          maxAllowedDeviation,
          sortedCommonDatesETFData
          ).rebalancePortfolio
      (foo.investmentPeriod, foo.averageAnnualReturnFraction, foo.endOfPeriodSnapshot)
    }

    temp
  }
}
