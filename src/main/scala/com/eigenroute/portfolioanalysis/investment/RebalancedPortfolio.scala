package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, FinalPortfolioQuantityToHave}
import org.apache.spark.sql.Dataset

case class RebalancedPortfolio(
    rebalancedDataset: Dataset[ETFDataPlus],
    newQuantitiesChosenForThisRebalancing: Seq[FinalPortfolioQuantityToHave],
    accumulatedExDiv: BigDecimal,
    accumulatedCash: BigDecimal)

