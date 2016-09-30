package com.eigenroute.portfolioanalysis

import org.scalatest.{FlatSpec, ShouldMatchers}

import scala.util.{Success, Try}

class MainATest extends FlatSpec with ShouldMatchers {

  "The application" should "run the simulation based on the input parameters" in {

    val args: Array[String] = Array(
      "5",
      "Quarterly",
      "100060",
      "9.99",
      "0.0011",
      "0.05",
      "src/test/scala/com/eigenroute/portfolioanalysis/portfolioDesign.csv"
    )

    Try(Main.main(args)) shouldBe a[Success[_]]

  }

}
