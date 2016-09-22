package com.eigenroute.portfolioanalysis.rebalancing

import org.scalatest.{FlatSpec, ShouldMatchers}

class PortfolioQuantitiesToAcquireUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  "Adding additional quantities to the portfolio quantities to be acquired" should "add the correct quantity" in
  new EstimatedQuantitiesToAcquire {
    val quantitiesToAdd: Seq[AddnlQty] = Seq(AddnlQty(eTFA, 3), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))
    val portfolioQuantitiesToAcquire = PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesAllTrades)
    val expected = PortfolioQuantitiesToAcquire(
      Seq(
        PortfolioQuantityToAcquire(eTFA, 77, round(20 * (1 + 0.0011)), 74.91759),
        PortfolioQuantityToAcquire(eTFB, 68, round(30 * (1 + 0.0011)), 66.59341),
        PortfolioQuantityToAcquire(eTFC, -76, round(40 / (1 + 0.0011)), -75.0825),
        PortfolioQuantityToAcquire(eTFD, -11, round(50 / (1 + 0.0011)), -10.011)
      )
    )
    portfolioQuantitiesToAcquire + quantitiesToAdd shouldEqual expected
  }


}
