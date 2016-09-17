package com.eigenroute.portfolioanalysis

import org.scalatest.{FlatSpec, ShouldMatchers}

class PortfolioRebalancerUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  val pr = new PortfolioRebalancer

  "The max quantities generator" should "calculate the maximum quantity of each ETF that can be purchased with the" +
  "remaining cash" in new EstimatedQuantitiesToAcquire {

    val expectedAllMatch = Seq(AddnlQty(eTFA, 6), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))
    val firstEstimateQuantitiesAllTradesWithNonMatch = Seq(
      PorfolioQuanitiesToAcquire(eTFNotInSnapshot, 74, round(20*(1 + 0.0011)), 74.91759),
      PorfolioQuanitiesToAcquire(eTFB, 66, round(30*(1 + 0.0011)), 66.59341),
      PorfolioQuanitiesToAcquire(eTFC, -76, round(40/(1 + 0.0011)), -75.0825),
      PorfolioQuanitiesToAcquire(eTFD, -11, round(50/(1 + 0.0011)), -10.011)
    )
    val expectedOneNotMatched = Seq(AddnlQty(eTFNotInSnapshot, 0), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))

    pr.maxQuantitiesGenerator(expectedFirstEstimateQuantitiesAllTrades, portfolioSnapshot) should
      contain theSameElementsAs expectedAllMatch
    pr.maxQuantitiesGenerator(firstEstimateQuantitiesAllTradesWithNonMatch, portfolioSnapshot) should
      contain theSameElementsAs expectedOneNotMatched

  }

  "The additional quantities to acquire generator" should "generate and exhaustive list of possible additional " +
  "quantities" in {

    val expectedAllMatch = Seq(AddnlQty(eTFA, 6), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))
    val expectedFull = Seq(
      Seq(AddnlQty(eTFA, 6), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 5), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 4), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 3), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 2), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 0), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 6), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 5), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 4), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 3), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 2), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 0), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 6), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 5), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 4), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 3), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 2), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 0), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 6), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 5), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 4), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 3), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 2), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 0), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 6), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 5), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 4), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 3), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 2), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 0), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))
    )
    val allMatchReverse = Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 2), AddnlQty(eTFA, 3))
    val expectedReverse = Seq(
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 2), AddnlQty(eTFA, 3)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 1), AddnlQty(eTFA, 3)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 0), AddnlQty(eTFA, 3)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 2), AddnlQty(eTFA, 2)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 1), AddnlQty(eTFA, 2)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 0), AddnlQty(eTFA, 2)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 2), AddnlQty(eTFA, 1)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 1), AddnlQty(eTFA, 1)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 0), AddnlQty(eTFA, 1)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 2), AddnlQty(eTFA, 0)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 1), AddnlQty(eTFA, 0)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 0), AddnlQty(eTFA, 0))
    )

    pr.additionalQuanititiesGenerator(expectedAllMatch) should contain theSameElementsAs expectedFull
    pr.additionalQuanititiesGenerator(allMatchReverse) should contain theSameElementsAs expectedReverse
  }

}
