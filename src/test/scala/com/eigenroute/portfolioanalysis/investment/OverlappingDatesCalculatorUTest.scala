package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.PortfolioFixture
import com.eigenroute.portfolioanalysis.rebalancing.ETFDataPlus
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, ShouldMatchers}
import com.eigenroute.portfolioanalysis.util.RichJoda._

class OverlappingDatesCalculatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  val oDC = new OverlappingDatesCalculator(portfolioDesign)

  "The overlapping dates creator" should "return the dates that are common to all datasets in the collection" in
  new InvestmentFixture {

    val actual = oDC.overlappingDates(sortedCommonDatesLessDatesToOmitPlusNonCommon)
    val expected =
      sortedCommonDatesETFData.filter(_.eTFCode == eTFA).map(eTFData => new DateTime(eTFData.asOfDate.getTime))
      .filterNot{ datesToOmit.contains }

    actual should contain theSameElementsAs expected

    val eTFDatas =
      portfolioDesign.eTFSelections.map { selection =>
        sortedCommonDatesLessDatesToOmitPlusNonCommon
        .filter(_.eTFCode == selection.eTFCode) }
      .filterNot{ datesToOmit.contains }

    oDC.overlappingDatesFromSplitETFData(eTFDatas) should contain theSameElementsInOrderAs expected

  }

  it should "return a dataset of ETFs that contain only entries with dates that are common to all ETFs" in
  new InvestmentFixture {
    val nonOverlappingDatesETFData: Seq[ETFDataPlus] = Seq(
      ETFDataPlus(startDate.minusDays(10), eTFC, "1", 20, 0, 0, 0),
      ETFDataPlus(startDate, eTFA, "1", 20, 0, 0, 0),
      ETFDataPlus(startDate, eTFB, "1", 20, 0, 0, 0),
      ETFDataPlus(startDate, eTFC, "1", 20, 0, 0, 0),
      ETFDataPlus(startDate, eTFD, "1", 20, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(3), eTFA, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(3), eTFB, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(3), eTFC, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(3), eTFD, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(37), eTFA, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(37), eTFB, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(36), eTFC, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(37), eTFD, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(47), eTFA, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(47), eTFB, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(47), eTFC, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(47), eTFD, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(50), eTFB, "1", 21, 0, 0, 0)
    )

    val expected: Seq[ETFDataPlus] = Seq(
      ETFDataPlus(startDate, eTFA, "1", 20, 0, 0, 0),
      ETFDataPlus(startDate, eTFB, "1", 20, 0, 0, 0),
      ETFDataPlus(startDate, eTFC, "1", 20, 0, 0, 0),
      ETFDataPlus(startDate, eTFD, "1", 20, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(3), eTFA, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(3), eTFB, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(3), eTFC, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(3), eTFD, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(47), eTFA, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(47), eTFB, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(47), eTFC, "1", 21, 0, 0, 0),
      ETFDataPlus(startDate.plusDays(47), eTFD, "1", 21, 0, 0, 0)
    )

    oDC.eTFDataOnlyWithEntriesHavingOverlappingDates(
      nonOverlappingDatesETFData) should contain theSameElementsInOrderAs expected
  }


}
