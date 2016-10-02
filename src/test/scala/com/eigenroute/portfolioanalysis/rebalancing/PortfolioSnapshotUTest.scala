package com.eigenroute.portfolioanalysis.rebalancing

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.joda.time.DateTime
import org.scalatest.TryValues._
import org.scalatest.{FlatSpec, ShouldMatchers}

import scala.util.Try

class PortfolioSnapshotUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  "Creating an invalid portfolio snapshot" should "not be possible" in {
    val invalidData =
      Seq(
        eTFDataPlusA,
        eTFDataPlusB,
        eTFDataPlusC,
        eTFDataPlusD,
        ETFData(new DateTime(2016, 1, 2, 1, 1, 1), eTFA, "", 20, 0, 50)
      )

    Try(PortfolioSnapshot(invalidData)).failure.exception shouldBe a[RuntimeException]
  }

}
