package com.eigenroute.portfolioanalysis

case class PortfolioValueDifference(eTFCode: ETFCode, valueDifference: Double)
case class PortfolioWeightDifference(eTFCode: ETFCode, weightDifference: Double)
case class ETFDesiredValue(eTFCode: ETFCode, value:Double, isToTrade: Boolean)
case class PorfolioQuanitiesToAcquire(
            eTFCode: ETFCode, quantityToAcquire:Int, effectivePrice: Double, fractionalQuantity: Double)

class PortfolioRebalancer extends PortfolioValueCalculator {

  def valueDifferences(
    desiredValues:Seq[ETFDesiredValue],
    portfolioSnapshot: PortfolioSnapshot): Seq[PortfolioValueDifference] =
    desiredValues.map { desiredValue =>
      if (desiredValue.isToTrade) {
        val currentValue =
          portfolioSnapshot.eTFDatas.find(_.eTFCode == desiredValue.eTFCode)
          .map( eTFData => eTFData.nAV * eTFData.quantity).getOrElse(0d)
        PortfolioValueDifference(desiredValue.eTFCode, desiredValue.value - currentValue)
      } else {
        PortfolioValueDifference(desiredValue.eTFCode, 0)
      }
    }

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
