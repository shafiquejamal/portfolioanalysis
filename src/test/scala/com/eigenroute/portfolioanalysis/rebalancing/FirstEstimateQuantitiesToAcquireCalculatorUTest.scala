package com.eigenroute.portfolioanalysis.rebalancing

import org.scalatest._

class FirstEstimateQuantitiesToAcquireCalculatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture  {

  "The quantity difference calculator" should "calculate the new quantities to be purchased such that as much free cash as" +
  "possible is invested, with the minimum deviation from the desired weights" in new DesiredValueFixture
  with EstimatedQuantitiesToAcquire {

    checkFirstEstimateQuantitiesToAcquire(expectedValueDifferenceAllTrades, expectedFirstEstimateQuantitiesAllTrades)
    checkFirstEstimateQuantitiesToAcquire(
      expectedValueDifferenceAllTrades, expectedFirstEstimateQuantitiesAllTradesExpensive, 0.0025)
    checkFirstEstimateQuantitiesToAcquire(expectedValueDifferenceOneTrade, expectedFirstEstimateQuantitiesOneTrade)
    checkFirstEstimateQuantitiesToAcquire(expectedValueDifferenceNoTrades, expectedFirstEstimateQuantitiesNoTrades)

  }

  private def checkFirstEstimateQuantitiesToAcquire(
                                                     expectedValueDifference: Seq[PortfolioValueDifference],
                                                     expectedFirstEstimateQuantities: Seq[PortfolioQuantityToAcquire],
                                                     bidAskCost: Double = 0.0011) {

    new FirstEstimateQuantitiesToAcquireCalculator()
    .firstEstimateQuantitiesToAcquire(portfolioDesign, portfolioSnapshot, expectedValueDifference, bidAskCost)
    .map { est =>
      PortfolioQuantityToAcquire(
        est.eTFCode, est.quantityToAcquire, round(est.effectivePrice), round(est.fractionalQuantity)) } should
    contain theSameElementsAs expectedFirstEstimateQuantities

  }

}
