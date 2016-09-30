package com.eigenroute.portfolioanalysis.rebalancing

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.scalamock.scalatest.MockFactory
import org.scalatest._

class NewDesiredValuesCalculatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture with MockFactory {

  trait Fixture {
    val mockWeightDifferenceCalculator = mock[WeightDifferenceCalculator]
    (mockWeightDifferenceCalculator.weightDifferences _)
    .expects(portfolioDesign, portfolioSnapshot).returning(weightDifferences).noMoreThanTwice()
  }

  "The new desired value calculator" should "calculate the new desired value when the max dev is low but not " +
  "zero" in new DesiredValueFixture with Fixture {
    checkNewDesiredValue(
      mockWeightDifferenceCalculator, 0.05, expectedDesiredValuesOneNotTraded, 10d, 20d, 20d, portfolioSnapshot)
  }

  it should "calculate the new desired value when the max dev is zero" in new DesiredValueFixture with Fixture {
    checkNewDesiredValue(
      mockWeightDifferenceCalculator, 0d, expectedDesiredValuesAllToBeTraded, 10d, 0d, 40d, portfolioSnapshot)
    checkNewDesiredValue(
      mockWeightDifferenceCalculator, 0d, expectedDesiredValuesAllToBeTradedcost15ExDivCash100, 15d, 60d, 40d,
        portfolioSnapshot)
  }

  it should "calculate the new desired value when the max dev is one" in new DesiredValueFixture with Fixture {
    checkNewDesiredValue(
      mockWeightDifferenceCalculator, 1d, expectedDesiredValuesNoTrades, 10d, 20d, 20d, portfolioSnapshot)
  }

  it should "calculate the new desired value when quantities of all ETFs are zero" in new DesiredValueFixture {
    val mockWeightDifferenceCalculator = mock[WeightDifferenceCalculator]
    (mockWeightDifferenceCalculator.weightDifferences _)
    .expects(portfolioDesign, portfolioSnapshotZeroQuantity).returning(weightDifferences)
    checkNewDesiredValue(
      mockWeightDifferenceCalculator, 0d, expectedDesiredValuesFirstTrades, 10d, 0d, 10040d, portfolioSnapshotZeroQuantity)
  }

  private def checkNewDesiredValue(
      weightDifferenceCalculator: WeightDifferenceCalculator,
      maxAllowedDeviation: Double,
      expected: Seq[ETFDesiredValue],
      perETFTradingCost: Double,
      accExDiv: Double,
      accCash: Double,
      pS: PortfolioSnapshot): Unit = {
    new NewDesiredValuesCalculator(weightDifferenceCalculator).newDesiredValues(
      portfolioDesign, pS, maxAllowedDeviation, perETFTradingCost, accExDiv, accCash)
    .map { dV => ETFDesiredValue(dV.eTFCode, round(dV.value), dV.isToTrade) } should contain theSameElementsAs expected
  }

}
