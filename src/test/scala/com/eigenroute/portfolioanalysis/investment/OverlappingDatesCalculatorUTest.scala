package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.{DatasetFixture, PortfolioFixture}
import org.joda.time.DateTime
import org.scalatest.{ShouldMatchers, FlatSpec}

class OverlappingDatesCalculatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  import DatasetFixture._
  val oDC = new OverlappingDatesCalculator(portfolioDesign)

  "The overlapping dates creator" should "return the dates that are common to all datasets in the collection" in {

    val actual = oDC.overlappingDates(sortedCommonDatesDataset)
    val expected =
      commonDatesETFData.filter(_.eTFCode == eTFA).map(eTFData => new DateTime(eTFData.asOfDate.getTime))
    actual should contain theSameElementsInOrderAs expected

    val datasets =
      portfolioDesign.eTFSelections.map { selection =>
        sortedCommonDatesDataset.filter(_.eTFCode == selection.eTFCode) }
    oDC.overlappingDates(datasets) should contain theSameElementsInOrderAs expected

  }


}
