package com.eigenroute.portfolioanalysis.rebalancing

class NewDesiredValuesCalculator extends PortfolioValueCalculation {

  def newDesiredValues(
      portfolioDesign: PortfolioDesign,
      weightDifferences: Seq[PortfolioWeightDifference],
      portfolioSnapshot: PortfolioSnapshot,
      maxAllowedDeviation: Double,
      perETFTradingCost: Double,
      accumulatedExDividends: Double,
      accumulatedCash: Double):Seq[ETFDesiredValue] = {

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
      portfolioValueFromETFDatas(portfolioSnapshot.sameDateUniqueCodesETFDatas.filter { eTFData =>
        normalizedWeightOfETFsToTrade.map(_.eTFCode).contains(eTFData.eTFCode) }) + extraCashToInvest

    val valueOfETFsToTrade: Seq[ETFDesiredValue]  =
      normalizedWeightOfETFsToTrade.map {eTFSelection =>
        ETFDesiredValue(eTFSelection.eTFCode, eTFSelection.desiredWeight * totalValueOfETFsToTrade, isToTrade = true) }

    valueOfETFsToTrade ++ valueOfETFsNotToTrade
  }

}
