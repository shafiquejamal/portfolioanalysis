package com.eigenroute.portfolioanalysis

import org.scalatest.{FlatSpec, ShouldMatchers}

class PortfolioRebalancerUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  val pr = new PortfolioRebalancer

  "The quantity difference calculator" should "calculate the new quantities to be purchased such that as much free cash as" +
  "possible is invested, with the minimum deviation from the desired weights" in new DesiredValueFixture {

    val expectedFirstEstimateQuantitiesAllTrades = Seq(
      PorfolioQuanitiesToAcquire(eTFA, 74, round(20*(1 + 0.0011)), 74.91759),
      PorfolioQuanitiesToAcquire(eTFB, 66, round(30*(1 + 0.0011)), 66.59341),
      PorfolioQuanitiesToAcquire(eTFC, -76, round(40/(1 + 0.0011)), -75.0825),
      PorfolioQuanitiesToAcquire(eTFD, -11, round(50/(1 + 0.0011)), -10.011)
    )

    val expectedFirstEstimateQuantitiesAllTradesExpensive = Seq(
      PorfolioQuanitiesToAcquire(eTFA, 74, round(20*(1 + 0.0025)), 74.81297),
      PorfolioQuanitiesToAcquire(eTFB, 66, round(30*(1 + 0.0025)), 66.50042),
      PorfolioQuanitiesToAcquire(eTFC, -76, round(40/(1 + 0.0025)), -75.1875),
      PorfolioQuanitiesToAcquire(eTFD, -11, round(50/(1 + 0.0025)), -10.025)
    )

    val expectedFirstEstimateQuantitiesOneTrade = Seq(
      PorfolioQuanitiesToAcquire(eTFA, 67, round(20*(1 + 0.0011)), 67.71963),
      PorfolioQuanitiesToAcquire(eTFB, 56, round(30*(1 + 0.0011)), 56.99613),
      PorfolioQuanitiesToAcquire(eTFC, -77, round(40/(1 + 0.0011)), -76.52526),
      PorfolioQuanitiesToAcquire(eTFD, 0, round(50/(1 + 0.0011)), 0)
    )

    val expectedFirstEstimateQuantitiesNoTrades = Seq(
      PorfolioQuanitiesToAcquire(eTFA, 0, round(20/(1 + 0.0011)), 0),
      PorfolioQuanitiesToAcquire(eTFB, 0, round(30/(1 + 0.0011)), 0),
      PorfolioQuanitiesToAcquire(eTFC, 0, round(40/(1 + 0.0011)), 0),
      PorfolioQuanitiesToAcquire(eTFD, 0, round(50/(1 + 0.0011)), 0)
    )

    checkFirstEstimateQuantitiesToAcquire(expectedValueDifferenceAllTrades, expectedFirstEstimateQuantitiesAllTrades)
    checkFirstEstimateQuantitiesToAcquire(expectedValueDifferenceAllTrades, expectedFirstEstimateQuantitiesAllTradesExpensive, 0.0025)
    checkFirstEstimateQuantitiesToAcquire(expectedValueDifferenceOneTrade, expectedFirstEstimateQuantitiesOneTrade)
    checkFirstEstimateQuantitiesToAcquire(expectedValueDifferenceNoTrades, expectedFirstEstimateQuantitiesNoTrades)

  }

  private def checkFirstEstimateQuantitiesToAcquire(
      expectedValueDifference: Seq[PortfolioValueDifference],
      expectedFirstEstimateQuantities: Seq[PorfolioQuanitiesToAcquire],
      bidAskCost: Double = 0.0011) {

    pr.firstEstimateQuantitiesToAcquire(portfolioDesign, portfolioSnapshot, expectedValueDifference, bidAskCost)
    .map { est =>
      PorfolioQuanitiesToAcquire(
        est.eTFCode, est.quantityToAcquire, round(est.effectivePrice), round(est.fractionalQuantity) ) } should
    contain theSameElementsAs expectedFirstEstimateQuantities

  }
}
