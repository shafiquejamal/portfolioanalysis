package com.eigenroute.portfolioanalysis.rebalancing

import org.scalatest.{FlatSpec, ShouldMatchers}

class WeightDifferenceCalculatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  "The weight difference calculator" should "accurately calculate the weight difference" in {
    new WeightDifferenceCalculator().weightDifferences(portfolioDesign, portfolioSnapshot).map { portfolioWeightDifference =>
      PortfolioWeightDifference(portfolioWeightDifference.eTFCode, round(portfolioWeightDifference.weightDifference))
    } should contain theSameElementsAs weightDifferences
  }

}
