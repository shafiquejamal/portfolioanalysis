package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.PortfolioFixture
import com.eigenroute.portfolioanalysis.investment.InvestmentPeriod._
import org.scalatest.{FlatSpec, ShouldMatchers}

class InvestmentIntervalUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  "The number of months" should "equal the number of years divided by 12" in new InvestmentFixture {
    lengthInMonths(investmentPeriod) shouldEqual 36
  }

}
