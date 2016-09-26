package com.eigenroute.portfolioanalysis.rebalancing

class WeightDifferenceCalculator extends PortfolioValueCalculation {

  def weightDifferences(
     portfolioDesign: PortfolioDesign,
     portfolioSnapshot: PortfolioSnapshot):Seq[PortfolioWeightDifference] = {

    val portfolioVal = portfolioValueETFsOnly(portfolioSnapshot)

    portfolioDesign.eTFSelections.map { eTFDATA =>
      val eTFCode = eTFDATA.eTFCode
      val desiredWeight = eTFDATA.desiredWeight
      val actualWeight =
        if (portfolioVal != 0)
          actualValue(portfolioSnapshot, eTFCode) / portfolioVal
        else
          0d
      PortfolioWeightDifference(eTFCode, desiredWeight - actualWeight)
    }

  }

}
