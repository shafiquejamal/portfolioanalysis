package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, ShouldMatchers}

class InvestmentPeriodsCreatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  "The investment periods creator" should "create x investment periods of approximately y duration" in
  new InvestmentFixture {
    val actualInvestmentPeriods = new InvestmentPeriodsCreator(portfolioDesign, sortedCommonDatesETFData, 10).create
    val endDates = actualInvestmentPeriods.map(_.endDate)
    val startDates = actualInvestmentPeriods.map(_.startDate)

    startDates should contain(commonStartDate)
    endDates should contain(commonEndDate)
  }

  it should "not contain dates that are not in the common dates ETF data" in new InvestmentFixture {

    val dateToOmit: DateTime = commonStartDate.plusDays(10)
    val iPCWithoutSomeDates =
      new InvestmentPeriodsCreator(portfolioDesign, sortedCommonDatesETFData.filter(!_.asOfDate.isEqual(dateToOmit)), 10)
    val investmentPeriods = iPCWithoutSomeDates.create

    investmentPeriods.map(_.startDate) should not contain dateToOmit
  }

  "The earliest date retriever" should "return the earliest date that is common to all the chosen ETFs in the ETF data" in
  new InvestmentFixture {
    val iPC = new InvestmentPeriodsCreator(portfolioDesign, sortedCommonDatesETFData, 10)
    iPC.maybeEarliestDate should contain(commonStartDate)
    iPC.maybeLatestDate should contain(commonEndDate)
  }


}
