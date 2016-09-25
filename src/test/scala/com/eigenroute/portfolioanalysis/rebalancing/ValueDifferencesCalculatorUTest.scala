package com.eigenroute.portfolioanalysis.rebalancing

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.scalamock.scalatest.MockFactory
import org.scalatest._

class ValueDifferencesCalculatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture with MockFactory {

  trait Fixture {
    val mockCalculator = mock[NewDesiredValuesCalculator]
    val valueDifferencesCalculator = new ValueDifferencesCalculator(mockCalculator)
  }

  "The value difference calculator" should "calculate the difference in value as non-zero for ETFs to be traded, and zero " +
  "for ETFs not to be traded, and should get the signs correct" in new DesiredValueFixture with Fixture {

    (mockCalculator.newDesiredValues _)
    .expects(portfolioDesign, portfolioSnapshot, 0.05, 10d, 0d, 0d).returning(expectedDesiredValuesOneToBeTraded)
    valueDifferencesCalculator.valueDifferences(portfolioDesign, portfolioSnapshot, 0.05, 10d, 0d, 0d).map { vDiff =>
      PortfolioValueDifference(vDiff.eTFCode, round(vDiff.valueDifference))
    } should contain theSameElementsAs expectedValueDifferenceOneTrade

    (mockCalculator.newDesiredValues _)
    .expects(portfolioDesign, portfolioSnapshot, 1d, 10d, 0d, 0d).returning(expectedDesiredValuesNoTrades)
    valueDifferencesCalculator.valueDifferences(portfolioDesign, portfolioSnapshot, 1d, 10d, 0d, 0d) should
    contain theSameElementsAs expectedValueDifferenceNoTrades

    (mockCalculator.newDesiredValues _)
    .expects(portfolioDesign, portfolioSnapshot, 0d, 10d, 0d, 0d).returning(expectedDesiredValuesAllToBeTraded)
    valueDifferencesCalculator.valueDifferences(portfolioDesign, portfolioSnapshot, 0d, 10d, 0d, 0d) should
    contain theSameElementsAs expectedValueDifferenceAllTrades
  }

  it should "correctly calculate the desired value difference for the initial trade" in new DesiredValueFixture
  with Fixture {

    (mockCalculator.newDesiredValues _)
    .expects(portfolioDesign, portfolioSnapshotZeroQuantity, 0d, 10d, 0d, 10040d).returning(expectedDesiredValuesFirstTrades)
    valueDifferencesCalculator
    .valueDifferences(portfolioDesign, portfolioSnapshotZeroQuantity, 0d, 10d, 0d, 10040d).map { vDiff =>
      PortfolioValueDifference(vDiff.eTFCode, round(vDiff.valueDifference))
    } should contain theSameElementsAs expectedValueDifferenceFirstTrades

  }

}
