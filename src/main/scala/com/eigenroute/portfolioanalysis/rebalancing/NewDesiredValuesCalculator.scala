package com.eigenroute.portfolioanalysis.rebalancing

class NewDesiredValuesCalculator(
    weightDifferenceCalculator: WeightDifferenceCalculator = new WeightDifferenceCalculator)
  extends PortfolioValueCalculation {

  def newDesiredValues(
      portfolioDesign: PortfolioDesign,
      portfolioSnapshot: PortfolioSnapshot,
      maxAllowedDeviation: BigDecimal,
      perETFTradingCost: BigDecimal,
      accumulatedExDividends: BigDecimal,
      accumulatedCash: BigDecimal): Seq[ETFDesiredValue] = {

    val weightDifferences = weightDifferenceCalculator.weightDifferences(portfolioDesign, portfolioSnapshot)

    val eTFsToTrade = weightDifferences.filter( pWD => math.abs(pWD.weightDifference.toDouble) > maxAllowedDeviation)
    val eTFsToNotTrade = weightDifferences.diff(eTFsToTrade)
    val valueOfETFsNotToTrade: Seq[ETFDesiredValue] =
      eTFsToNotTrade.map { pWD =>
        ETFDesiredValue(pWD.eTFCode, actualValue(portfolioSnapshot, pWD.eTFCode), isToTrade = false) }

    val sumOfRemainingWeights: BigDecimal =
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
