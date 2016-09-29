package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, FinalPortfolioQuantityToHave, PortfolioSnapshot}

case class RebalancedPortfolio(
    rebalancedETFData: Seq[ETFDataPlus],
    newQuantitiesChosenForThisRebalancing: Seq[FinalPortfolioQuantityToHave],
    accumulatedExDiv: BigDecimal,
    accumulatedCash: BigDecimal,
    endOfPeriodSnapshot: PortfolioSnapshot,
    initialInvestment: BigDecimal,
    portfolioPerformance: PortfolioPerformance,
    liquidatedValue: BigDecimal = 0,
    totalReturnFraction: BigDecimal = 0)

