package com.eigenroute.portfolioanalysis.rebalancing

import java.sql.Date

import com.eigenroute.portfolioanalysis.{DatasetFixture, PortfolioFixture}
import org.joda.time.DateTime
import org.scalatest.TryValues._
import org.scalatest.{FlatSpec, ShouldMatchers}

import scala.util.Try

class PortfolioSnapshotUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  import DatasetFixture._

  "Creating an invalid portfolio snapshot" should "not be possible" in {
    val invalidData =
      Seq(
        eTFDataPlusA,
        eTFDataPlusB,
        eTFDataPlusC,
        eTFDataPlusD,
        ETFDataPlus(new Date(new DateTime(2016, 1, 2, 1, 1, 1).getMillis), eTFA, "", 20, 0, 50, 0d)
      )

    Try(PortfolioSnapshot(invalidData)).failure.exception shouldBe a[RuntimeException]
  }

  "Creating a portfolio snapshot from a portfolio design and portfolio dataset" should "succeed if the necessary data " +
  "exists" in {

    val expected =
      PortfolioSnapshot(
        portfolioSnapshot.sameDateUniqueCodesETFDatas.map(_.copy(
          quantity = 0d,
          asOfDate = new Date(new DateTime(2001, 1, 1, 0, 0, 0).getMillis))))
    val datasets = portfolioDesign.eTFSelections.map {selection =>
      sortedCommonDatesDataset.filter(_.eTFCode == selection.eTFCode)}
    val actual = PortfolioSnapshot(portfolioDesign, datasets)
    actual.copy(sameDateUniqueCodesETFDatas = actual.sameDateUniqueCodesETFDatas.map(data => data.copy(cash = 0d))) shouldEqual expected

  }

}
