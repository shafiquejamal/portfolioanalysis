package com.eigenroute.portfolioanalysis.rebalancing

class FirstEstimateQuantitiesToAcquireCalculator extends PortfolioValueCalculation {

  def firstEstimateQuantitiesToAcquire(
      portfolioDesign: PortfolioDesign,
      portfolioSnapshot: PortfolioSnapshot,
      valueDifferences: Seq[PortfolioValueDifference],
      bidAskCostFractionOfNAV: Double): Seq[PortfolioQuantityToAcquire] = {

    def price(valueDifference:Double, nAV: Double):Double =
      if (valueDifference > 0)
        nAV * (1 + bidAskCostFractionOfNAV)
      else
        nAV / (1 + bidAskCostFractionOfNAV)

    valueDifferences.map { pVD =>
      val maybeNAV = portfolioSnapshot.eTFDatas.find(_.eTFCode == pVD.eTFCode).map(_.nAV)
      val maybeEffectivePrice = maybeNAV.map(nAV => price(pVD.valueDifference, nAV))
      val quantity: Double = maybeEffectivePrice map (pVD.valueDifference / _) getOrElse 0d
      PortfolioQuantityToAcquire(pVD.eTFCode, math.floor(quantity).toInt, maybeEffectivePrice.getOrElse(0d), quantity)
    }
  }

}
