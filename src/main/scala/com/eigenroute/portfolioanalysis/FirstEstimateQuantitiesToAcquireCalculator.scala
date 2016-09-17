package com.eigenroute.portfolioanalysis

class FirstEstimateQuantitiesToAcquireCalculator extends PortfolioValueCalculation {

  def firstEstimateQuantitiesToAcquire(
      portfolioDesign: PortfolioDesign,
      portfolioSnapshot: PortfolioSnapshot,
      valueDifferences: Seq[PortfolioValueDifference],
      bidAskCostFractionOfNAV: Double): Seq[PorfolioQuanitiesToAcquire] = {

    def price(valueDifference:Double, nAV: Double):Double =
      if (valueDifference > 0)
        nAV * (1 + bidAskCostFractionOfNAV)
      else
        nAV / (1 + bidAskCostFractionOfNAV)

    valueDifferences.map { pVD =>
      val nAV: Double = portfolioSnapshot.eTFDatas.find(_.eTFCode == pVD.eTFCode).map(_.nAV).get //TODO: do something about the get
    val effectivePrice: Double = price(pVD.valueDifference, nAV)
      val quantity: Double = pVD.valueDifference / effectivePrice
      PorfolioQuanitiesToAcquire(pVD.eTFCode, math.floor(quantity).toInt, effectivePrice, quantity)
    }
  }

}
