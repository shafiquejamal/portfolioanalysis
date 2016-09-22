package com.eigenroute.portfolioanalysis.rebalancing

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.scalatest._

class NewDesiredValuesCalculatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  "The new desired value calculator" should "calculate the new desired value when the max dev is low but not " +
  "zero" in new DesiredValueFixture {
    checkNewDesiredValue(0.05, expectedDesiredValuesOneToBeTraded, 10d, 20d, 20d)
  }

  it should "calculate the new desired value when the max dev is zero" in new DesiredValueFixture {
    checkNewDesiredValue(0d, expectedDesiredValuesAllToBeTraded, 10d, 0d, 40d)
    checkNewDesiredValue(0d, expectedDesiredValuesAllToBeTradedcost15ExDivCash100, 15d, 60d, 40d)
  }

  it should "calculate the new desired value when the max dev is one" in new DesiredValueFixture {
    checkNewDesiredValue(1d, expectedDesiredValuesNoTrades, 10d, 20d, 20d)
  }

  private def checkNewDesiredValue(
      maxAllowedDeviation: Double,
      expected: Seq[ETFDesiredValue],
      perETFTradingCost: Double,
      accExDiv: Double,
      accCash: Double): Unit = {
    new NewDesiredValuesCalculator().newDesiredValues(
      portfolioDesign, weightDifferences, portfolioSnapshot, maxAllowedDeviation, perETFTradingCost, accExDiv, accCash)
    .map { dV => ETFDesiredValue(dV.eTFCode, round(dV.value), dV.isToTrade) } should contain theSameElementsAs expected
  }

}
