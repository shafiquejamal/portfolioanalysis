package com.eigenroute.portfolioanalysis.rebalancing

class ValueDifferencesCalculator(
    desiredValuesCalculator: NewDesiredValuesCalculator = new NewDesiredValuesCalculator())
  extends PortfolioValueCalculation {

  def valueDifferences(
      portfolioDesign: PortfolioDesign,
      portfolioSnapshot: PortfolioSnapshot,
      maxAllowedDeviation: BigDecimal,
      perETFTradingCost: BigDecimal,
      accumulatedExDividends: BigDecimal,
      accumulatedCash: BigDecimal): Seq[PortfolioValueDifference] = {
    val desiredValues =
      desiredValuesCalculator.newDesiredValues(
        portfolioDesign, portfolioSnapshot, maxAllowedDeviation, perETFTradingCost, accumulatedExDividends, accumulatedCash)
    desiredValues.map { desiredValue =>
      if (desiredValue.isToTrade) {
        val currentValue: BigDecimal =
          portfolioSnapshot.sameDateUniqueCodesETFDatas.find(_.eTFCode == desiredValue.eTFCode)
          .map(eTFData => eTFData.nAV * eTFData.quantity).getOrElse(0)
        PortfolioValueDifference(desiredValue.eTFCode, desiredValue.value - currentValue)
      } else
        PortfolioValueDifference(desiredValue.eTFCode, 0)
    }
  }
}
