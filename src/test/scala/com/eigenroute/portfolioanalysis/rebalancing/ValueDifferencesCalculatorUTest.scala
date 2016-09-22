package com.eigenroute.portfolioanalysis.rebalancing

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.scalatest._

class ValueDifferencesCalculatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  val valueDifferencesCalculator = new ValueDifferencesCalculator()

  "The value difference calculator" should "calculate the difference in value as non-zero for ETFs to be traded, and zero " +
  "for ETFs not to be traded, and should get the signs correct" in new DesiredValueFixture {

    valueDifferencesCalculator.valueDifferences(expectedDesiredValuesOneToBeTraded, portfolioSnapshot).map { vDiff =>
      PortfolioValueDifference(vDiff.eTFCode, round(vDiff.valueDifference))
    } should contain theSameElementsAs expectedValueDifferenceOneTrade

    valueDifferencesCalculator.valueDifferences(expectedDesiredValuesNoTrades, portfolioSnapshot) should
    contain theSameElementsAs expectedValueDifferenceNoTrades

    valueDifferencesCalculator.valueDifferences(expectedDesiredValuesAllToBeTraded, portfolioSnapshot) should
    contain theSameElementsAs expectedValueDifferenceAllTrades
  }

}
