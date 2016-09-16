package com.eigenroute.portfolioanalysis

case class PortfolioValueDifference(eTFCode: ETFCode, valueDifference: Double)
case class PortfolioWeightDifference(eTFCode: ETFCode, weightDifference: Double)
case class ETFDesiredValue(eTFCode: ETFCode, value:Double, isToTrade: Boolean)

case class PortfolioValueDifferences()

class PortfolioManager {

  def weightDifferenceCalculator(
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

  def newDesiredValueCalculator(
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

  def portfolioValue(portfolioSnapshot: PortfolioSnapshot) = portfolioValueFromETFDatas(portfolioSnapshot.eTFDatas)

  def portfolioValueFromETFDatas(eTFDatas: Seq[ETFDataPlus]) = eTFDatas.map { eTFData => eTFData.nAV * eTFData.quantity }.sum

  def actualValue(portfolioSnapshot: PortfolioSnapshot, eTFCode: ETFCode) =
    portfolioSnapshot.eTFDatas.find(_.eTFCode == eTFCode).map(eTFDATA => eTFDATA.nAV * eTFDATA.quantity).getOrElse(0d)

}
