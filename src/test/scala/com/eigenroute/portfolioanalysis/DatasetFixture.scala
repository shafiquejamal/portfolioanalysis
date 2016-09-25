package com.eigenroute.portfolioanalysis

import java.sql.Date

import com.eigenroute.portfolioanalysis.investment.InvestmentPeriodsCreator
import com.eigenroute.portfolioanalysis.rebalancing.{ETFCode, ETFDataPlus}
import org.apache.spark.sql.SparkSession
import org.joda.time.DateTime

object DatasetFixture extends PortfolioFixture {

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

  val eTFPrices: Map[ETFCode, Double] = Map(eTFA -> 20, eTFB -> 30, eTFC -> 40, eTFD -> 50)

  val commonDatesETFData = portfolioDesign.eTFSelections.flatMap { selection =>
      0.to(maxDaysToAdd).map { daysToAdd =>
        ETFDataPlus(
          new Date(new DateTime(commonStartDate.plusDays(daysToAdd)).getMillis),
          selection.eTFCode,
          "1",
          eTFPrices(selection.eTFCode), 0d, 0d, 0)
      }.toSeq
    }

  val sortedCommonDatesDataset = commonDatesETFData.toDS().persist()

  val sortedCommonDatesLessDatesToOmitPlusNonCommon = (commonDatesETFData ++ Seq(
      ETFDataPlus(new Date(new DateTime(commonStartDate.minusDays(1)).getMillis), eTFA, "1", 20d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.minusDays(2)).getMillis), eTFB, "1", 30d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.minusDays(3)).getMillis), eTFC, "1", 40d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.minusDays(4)).getMillis), eTFD, "1", 50d, 0d, 0d, 0)
    ) ++ Seq(
      ETFDataPlus(new Date(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 1)).getMillis), eTFA, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 2)).getMillis), eTFB, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 3)).getMillis), eTFC, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new Date(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 4)).getMillis), eTFD, "1", 0d, 0d, 0d, 0)
    )).filterNot{ eTFData => datesToOmit.contains(new DateTime(eTFData.asOfDate.getTime))}.sortBy(_.asOfDate.getTime)
                                                      .toDS().persist()

  val iPC = new InvestmentPeriodsCreator(portfolioDesign, sortedCommonDatesDataset, 10)
}
