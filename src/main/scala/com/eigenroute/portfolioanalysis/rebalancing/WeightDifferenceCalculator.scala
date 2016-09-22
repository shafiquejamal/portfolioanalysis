package com.eigenroute.portfolioanalysis.rebalancing

class WeightDifferenceCalculator extends PortfolioValueCalculation {

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

}