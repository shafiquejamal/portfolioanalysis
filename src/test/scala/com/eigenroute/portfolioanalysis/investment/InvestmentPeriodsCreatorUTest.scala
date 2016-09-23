package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.{DatasetFixture, PortfolioFixture}
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, ShouldMatchers}

class InvestmentPeriodsCreatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  import DatasetFixture._

  "The investment periods creator" should "create x investment periods of approximately y duration" in {
    val actualInvestmentPeriods = iPC.create(portfolioDesign, 10)
    val endDates = actualInvestmentPeriods.map(_.endDate)
    val startDates = actualInvestmentPeriods.map(_.startDate)

    startDates should contain(commonStartDate)
    startDates intersect datesToOmit shouldBe empty
    endDates should contain(commonEndDate)
    endDates intersect datesToOmit shouldBe empty
  }

  "The earliest date retriever" should "return the earliest date that is common to all the chosen ETFs in the dataset" in {
    iPC.earliestDateForAllETFs should contain(commonStartDate)
    iPC.latestDateForAllETFs should contain(commonEndDate)
  }

  "Finding the latest earlier common date when the initial end date is not in the ETF dataset" should "return the latest " +
  "earlier date" in {
    iPC.searchForLatestEarlierCommonDate(new DateTime(commonStartDate.plusDays(4203))) should
      contain(new DateTime(commonStartDate.plusDays(4200)))

    iPC.searchForLatestEarlierCommonDate(new DateTime(commonStartDate.plusDays(21))) should
      contain(new DateTime(commonStartDate.plusDays(21)))
  }

}
