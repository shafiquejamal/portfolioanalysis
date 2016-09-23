package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.{DatasetFixture, PortfolioFixture}
import org.joda.time.DateTime
import org.scalatest.{ShouldMatchers, FlatSpec}

class OverlappingDatesCalculatorUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  import DatasetFixture._

  "The overlapping dates creator" should "return the datesnetf that are common to all datasets in the collection" in {

    val actual = new OverlappingDatesCalculator(portfolioDesign, commonDatesDataset).overlappingDates
    val expected =
      commonDatesETFData.filter(_.eTFCode == eTFA).map(eTFData => new DateTime(eTFData.asOfDate.getTime)).reverse
    actual should contain theSameElementsInOrderAs expected

  }


}
