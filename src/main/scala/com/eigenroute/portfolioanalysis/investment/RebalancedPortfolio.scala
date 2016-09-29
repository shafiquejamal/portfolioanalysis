package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, FinalPortfolioQuantityToHave, PortfolioSnapshot}
import org.apache.spark.sql.Dataset

case class RebalancedPortfolio(
    rebalancedDataset: Dataset[ETFDataPlus],
    newQuantitiesChosenForThisRebalancing: Seq[FinalPortfolioQuantityToHave],
    accumulatedExDiv: BigDecimal,
    accumulatedCash: BigDecimal,
    endOfPeriodSnapshot: PortfolioSnapshot,
    initialInvestment: BigDecimal,
    investmentPeriod: InvestmentPeriod,
    liquidatedValue: BigDecimal = 0,
    totalReturnFraction: BigDecimal = 0,
    averageAnnualReturnFraction: BigDecimal = 0)

