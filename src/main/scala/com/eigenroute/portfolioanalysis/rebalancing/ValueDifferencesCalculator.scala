package com.eigenroute.portfolioanalysis.rebalancing

class ValueDifferencesCalculator extends PortfolioValueCalculation {

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

}
