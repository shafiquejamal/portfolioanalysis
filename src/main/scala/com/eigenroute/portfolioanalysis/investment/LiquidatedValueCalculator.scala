package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.PortfolioSnapshot

class LiquidatedValueCalculator {

  def liquidatedValue(
      portfolioSnapshot: PortfolioSnapshot,
      bidAskCostFractionOfNAV: BigDecimal,
      perETFTradingCost: BigDecimal,
      accumulatedExDividends: BigDecimal,
      accumulatedCash: BigDecimal): BigDecimal = {

    val eTFLiquidatedValue =
      portfolioSnapshot
      .sameDateUniqueCodesETFDatas.map( eTFData => eTFData.quantity * eTFData.nAV/(1 + bidAskCostFractionOfNAV)).sum

    eTFLiquidatedValue + accumulatedExDividends + accumulatedCash -
      perETFTradingCost*portfolioSnapshot.sameDateUniqueCodesETFDatas.filterNot(_.quantity == 0).length
  }

}
