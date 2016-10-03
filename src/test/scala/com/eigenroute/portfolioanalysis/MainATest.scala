package com.eigenroute.portfolioanalysis

import org.scalatest.{FlatSpec, ShouldMatchers}

import scala.util.{Success, Try}

class MainATest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  "The application" should "run the simulation based on the input parameters" in new PortfolioFiles {

    val args: Array[String] = Array(
      "5",
      "Quarterly",
      "100060",
      "9.99",
      "0.0011",
      "0.05",
      portfolioDesignPath.getAbsolutePath,
      tempOutputFile.getAbsolutePath
    )

    Try(Main.main(args)) shouldBe a[Success[_]]

  }

  it should "work with with a portfolio that contains a single ETF only" in new PortfolioFiles {

    val args: Array[String] = Array(
      "5",
      "Quarterly",
      "100060",
      "9.99",
      "0.0011",
      "0.5",
      portfolioDesignPathXSBOnly.getAbsolutePath,
      tempOutputFile.getAbsolutePath
    )

    Try(Main.main(args)) shouldBe a[Success[_]]

  }

}
