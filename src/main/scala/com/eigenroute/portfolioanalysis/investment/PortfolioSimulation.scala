package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFData, PortfolioDesign}

class PortfolioSimulation(
    investmentDurationInYears: Int,
    rebalancingInterval: RebalancingInterval,
    initialInvestment: BigDecimal,
    perTransactionTradingCost: BigDecimal,
    bidAskCostFractionOfNav: BigDecimal,
    portfolioDesign: PortfolioDesign,
    maxAllowedDeviation: BigDecimal,
    sortedCommonDatesETFData:Seq[ETFData]) {

  def simulate: Seq[PortfolioPerformance] = {
    val investmentPeriods =
      new InvestmentPeriodsCreator(portfolioDesign, sortedCommonDatesETFData, investmentDurationInYears).create

    investmentPeriods.map { investmentPeriod =>
      new Investment(
          investmentPeriod,
          rebalancingInterval,
          initialInvestment,
          perTransactionTradingCost,
          bidAskCostFractionOfNav,
          portfolioDesign,
          maxAllowedDeviation,
          sortedCommonDatesETFData).rebalancePortfolio.portfolioPerformance
    }
  }
}
