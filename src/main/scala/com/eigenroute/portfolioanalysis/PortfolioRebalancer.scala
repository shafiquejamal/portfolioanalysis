package com.eigenroute.portfolioanalysis

case class PortfolioValueDifference(eTFCode: ETFCode, valueDifference: Double)
case class PortfolioWeightDifference(eTFCode: ETFCode, weightDifference: Double)
case class ETFDesiredValue(eTFCode: ETFCode, value:Double, isToTrade: Boolean)
case class PorfolioQuanitiesToAcquire(
            eTFCode: ETFCode, quantityToAcquire:Int, effectivePrice: Double, fractionalQuantity: Double)

class PortfolioRebalancer {

  def weightDifferences(
    portfolioDesign: PortfolioDesign,
    portfolioSnapshot: PortfolioSnapshot):Seq[PortfolioWeightDifference] = {

    val portfolioVal = portfolioValue(portfolioSnapshot)

    portfolioDesign.eTFSelections.map { eTFDATA =>
      val eTFCode = eTFDATA.eTFCode
      val desiredWeight = eTFDATA.desiredWeight
      val actualWeight = actualValue(portfolioSnapshot, eTFCode) / portfolioVal
      PortfolioWeightDifference(eTFCode, desiredWeight - actualWeight)
    }

  }

  def newDesiredValues(
    portfolioDesign: PortfolioDesign,
    weightDifferences: Seq[PortfolioWeightDifference],
    portfolioSnapshot: PortfolioSnapshot,
    maxAllowedDeviation: Double,
    perETFTradingCost: Double,
    accumulatedExDividends: Double,
    accumulatedCash: Double
  ):Seq[ETFDesiredValue] = {

    val eTFsToTrade = weightDifferences.filter( pWD => math.abs(pWD.weightDifference) > maxAllowedDeviation)
    val eTFsToNotTrade = weightDifferences.diff(eTFsToTrade)
    val valueOfETFsNotToTrade: Seq[ETFDesiredValue] =
      eTFsToNotTrade.map { pWD =>
        ETFDesiredValue(pWD.eTFCode, actualValue(portfolioSnapshot, pWD.eTFCode), isToTrade = false) }

    val sumOfRemainingWeights: Double =
      portfolioDesign.eTFSelections.filter( eTFSelection => eTFsToTrade.map(_.eTFCode).contains(eTFSelection.eTFCode))
        .map(_.desiredWeight).sum
    val normalizedWeightOfETFsToTrade =
      portfolioDesign.eTFSelections.filter( eTFSelection => eTFsToTrade.map(_.eTFCode).contains(eTFSelection.eTFCode))
        .map{ eTFSelection => ETFSelection(eTFSelection.eTFCode, eTFSelection.desiredWeight / sumOfRemainingWeights) }

    val tradingCosts = perETFTradingCost * eTFsToTrade.length
    val extraCashToInvest = accumulatedCash + accumulatedExDividends - tradingCosts

    val totalValueOfETFsToTrade =
      portfolioValueFromETFDatas(
        portfolioSnapshot.eTFDatas.filter { eTFData =>
          normalizedWeightOfETFsToTrade.map(_.eTFCode).contains(eTFData.eTFCode) }) + extraCashToInvest

    val valueOfETFsToTrade: Seq[ETFDesiredValue]  =
      normalizedWeightOfETFsToTrade.map {eTFSelection =>
        ETFDesiredValue(eTFSelection.eTFCode, eTFSelection.desiredWeight * totalValueOfETFsToTrade, isToTrade = true) }

    valueOfETFsToTrade ++ valueOfETFsNotToTrade
  }

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

  def portfolioValue(portfolioSnapshot: PortfolioSnapshot): Double = portfolioValueFromETFDatas(portfolioSnapshot.eTFDatas)

  def portfolioValueFromETFDatas(eTFDatas: Seq[ETFDataPlus]): Double =
    eTFDatas.map { eTFData => eTFData.nAV * eTFData.quantity }.sum

  def actualValue(portfolioSnapshot: PortfolioSnapshot, eTFCode: ETFCode): Double =
    portfolioSnapshot.eTFDatas.find(_.eTFCode == eTFCode).map(eTFDATA => eTFDATA.nAV * eTFDATA.quantity).getOrElse(0d)

}
