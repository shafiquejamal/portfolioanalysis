package com.eigenroute.portfolioanalysis.rebalancing

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.scalatest.{FlatSpec, ShouldMatchers}

class WeightDifferenceCalculatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  val wDC = new WeightDifferenceCalculator()

  "The weight difference calculator" should "accurately calculate the weight difference" in {
    wDC.weightDifferences(portfolioDesign, portfolioSnapshot).map { portfolioWeightDifference =>
      PortfolioWeightDifference(portfolioWeightDifference.eTFCode, round(portfolioWeightDifference.weightDifference))
    } should contain theSameElementsAs weightDifferences
  }

  it should "calculate the weight differences as equal to the desired weights if the portfolio value (ETFS only) is " +
  "zero (e.g. for the first trades)" in {
    wDC.weightDifferences(portfolioDesign, portfolioSnapshotZeroQuantity).map { portfolioWeightDifference =>
      PortfolioWeightDifference(portfolioWeightDifference.eTFCode, round(portfolioWeightDifference.weightDifference))
    } should contain theSameElementsAs portfolioDesign.eTFSelections.map { selection =>
      PortfolioWeightDifference(selection.eTFCode, selection.desiredWeight) }
  }

}
