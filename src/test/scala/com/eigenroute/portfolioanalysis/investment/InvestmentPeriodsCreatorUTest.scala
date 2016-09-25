package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.{DatasetFixture, PortfolioFixture}
import org.scalatest.{FlatSpec, ShouldMatchers}

class InvestmentPeriodsCreatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  import DatasetFixture._

  "The investment periods creator" should "create x investment periods of approximately y duration" in {
    val actualInvestmentPeriods = iPC.create
    val endDates = actualInvestmentPeriods.map(_.endDate)
    val startDates = actualInvestmentPeriods.map(_.startDate)

    startDates should contain(commonStartDate)
    endDates should contain(commonEndDate)
  }

  "The earliest date retriever" should "return the earliest date that is common to all the chosen ETFs in the dataset" in {
    iPC.earliestDate shouldEqual commonStartDate
    iPC.maybeLatestDate should contain(commonEndDate)
  }


}
