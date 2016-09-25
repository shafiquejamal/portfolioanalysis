package com.eigenroute.portfolioanalysis.rebalancing

class FirstEstimateQuantitiesToAcquireCalculator(
    valueDifferencesCalculator: ValueDifferencesCalculator = new ValueDifferencesCalculator())
  extends PortfolioValueCalculation {

  def firstEstimateQuantitiesToAcquire(
      portfolioDesign: PortfolioDesign,
      portfolioSnapshot: PortfolioSnapshot,
      bidAskCostFractionOfNAV: Double,
      maxAllowedDeviation: Double,
      perETFTradingCost: Double,
      accumulatedExDividends: Double,
      accumulatedCash: Double): Seq[PortfolioQuantityToAcquire] = {
    val valueDifferences =
      valueDifferencesCalculator.valueDifferences(
        portfolioDesign,
        portfolioSnapshot,
        maxAllowedDeviation,
        perETFTradingCost,
        accumulatedExDividends,
        accumulatedCash)
    def price(valueDifference:Double, nAV: Double):Double =
      if (valueDifference > 0)
        nAV * (1 + bidAskCostFractionOfNAV)
      else
        nAV / (1 + bidAskCostFractionOfNAV)

    valueDifferences.map { pVD =>
      val maybeNAV = portfolioSnapshot.sameDateUniqueCodesETFDatas.find(_.eTFCode == pVD.eTFCode).map(_.nAV)
      val maybeEffectivePrice = maybeNAV.map(nAV => price(pVD.valueDifference, nAV))
      val quantity: Double = maybeEffectivePrice map (pVD.valueDifference / _) getOrElse 0d
      PortfolioQuantityToAcquire(pVD.eTFCode, math.floor(quantity).toInt, maybeEffectivePrice.getOrElse(0d), quantity)
    }
  }

}
