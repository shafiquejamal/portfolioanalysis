package com.eigenroute.portfolioanalysis.rebalancing

import org.scalatest.{FlatSpec, ShouldMatchers}

class PortfolioValueCalculationUTest
  extends FlatSpec
  with ShouldMatchers
  with PortfolioFixture
  with PortfolioValueCalculation {

  "The portfolio value calculator" should "calculate the value of the portfolio using the nav and quantity from the " +
  "snapshot" in {
    portfolioValue(portfolioSnapshot) shouldEqual 10000d
    portfolioValueFromETFDatas(portfolioSnapshot.eTFDatas) shouldEqual 10000d
  }

  "The actual value calculator" should "calculate the current value of an ETF in the portfolio" in {
    actualValue(portfolioSnapshot, eTFB) shouldEqual 3000d
    actualValue(portfolioSnapshot, eTFNotInSnapshot) shouldEqual 0d
  }
}
