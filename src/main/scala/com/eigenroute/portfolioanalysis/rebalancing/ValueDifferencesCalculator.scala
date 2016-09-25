package com.eigenroute.portfolioanalysis.rebalancing

class ValueDifferencesCalculator(
    desiredValuesCalculator: NewDesiredValuesCalculator = new NewDesiredValuesCalculator())
  extends PortfolioValueCalculation {

  def valueDifferences(
      portfolioDesign: PortfolioDesign,
      portfolioSnapshot: PortfolioSnapshot,
      maxAllowedDeviation: Double,
      perETFTradingCost: Double,
      accumulatedExDividends: Double,
      accumulatedCash: Double): Seq[PortfolioValueDifference] = {
    val desiredValues =
      desiredValuesCalculator.newDesiredValues(
        portfolioDesign, portfolioSnapshot, maxAllowedDeviation, perETFTradingCost, accumulatedExDividends, accumulatedCash)
    desiredValues.map { desiredValue =>
      if (desiredValue.isToTrade) {
        val currentValue =
          portfolioSnapshot.sameDateUniqueCodesETFDatas.find(_.eTFCode == desiredValue.eTFCode)
          .map(eTFData => eTFData.nAV * eTFData.quantity).getOrElse(0d)
        PortfolioValueDifference(desiredValue.eTFCode, desiredValue.value - currentValue)
      } else {
        PortfolioValueDifference(desiredValue.eTFCode, 0)
      }
    }
  }
}
