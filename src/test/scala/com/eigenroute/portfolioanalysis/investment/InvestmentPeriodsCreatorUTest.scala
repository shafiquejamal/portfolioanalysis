package com.eigenroute.portfolioanalysis.investment

import java.sql.Date

import com.eigenroute.portfolioanalysis.PortfolioFixture
import com.eigenroute.portfolioanalysis.rebalancing.ETFDataPlus
import org.apache.spark.sql.SparkSession
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, ShouldMatchers}

class InvestmentPeriodsCreatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  implicit val spark = SparkSession.builder().appName("financial_data").master("local").getOrCreate()
  import spark.implicits._

  val commonStartDate = new DateTime(2001, 1, 1, 0, 0, 0)
  val maxDaysToAdd = 365*16
  val commonEndDate = commonStartDate.plusDays(maxDaysToAdd)
  val datesToOmit = Seq(
    new DateTime(commonStartDate.plusDays(905)),
    new DateTime(commonStartDate.plusDays(416)),
    new DateTime(commonStartDate.plusDays(202)),
    new DateTime(commonStartDate.plusDays(4201)),
    new DateTime(commonStartDate.plusDays(4202)),
    new DateTime(commonStartDate.plusDays(4203)),
    new DateTime(commonStartDate.plusDays(4159))
  )
  val allDS = (portfolioDesign.eTFSelections.flatMap { selection =>
    0.to(maxDaysToAdd).map { daysToAdd =>
      ETFDataPlus(
        new Date(new DateTime(commonStartDate.plusDays(daysToAdd)).getMillis), selection.eTFCode, "1", 0d, 0d, 0d, 0)
      }.toSeq
    } ++ Seq(
      ETFDataPlus(new Date(new DateTime(commonStartDate.minusDays(1)).getMillis), eTFA, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.minusDays(2)).getMillis), eTFB, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.minusDays(3)).getMillis), eTFC, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.minusDays(4)).getMillis), eTFD, "1", 0d, 0d, 0d, 0)
    ) ++ Seq(
      ETFDataPlus(new Date(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 1)).getMillis), eTFA, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 2)).getMillis), eTFB, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 3)).getMillis), eTFC, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 4)).getMillis), eTFD, "1", 0d, 0d, 0d, 0)
    )).filterNot{ eTFData => datesToOmit.contains(new DateTime(eTFData.asOfDate.getTime))}
    .toDS()
  val iPC = new InvestmentPeriodsCreator(portfolioDesign, allDS)

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
