package com.eigenroute.portfolioanalysis.rebalancing

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.scalatest.{ShouldMatchers, FlatSpec}

class PortfolioDesignUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  "Given a path to a valid file, the apply method" should "create the expected Portfolio Design object" in
  new PortfolioFiles {

    val expectedETFSelections =
      Seq(
        ETFSelection(ETFCode("XRE"), 0.05),
        ETFSelection(ETFCode("XIU"), 0.15),
        ETFSelection(ETFCode("XSP"), 0.20),
        ETFSelection(ETFCode("XEM"), 0.20),
        ETFSelection(ETFCode("XSB"), 0.20),
        ETFSelection(ETFCode("XPF"), 0.20)
      )
    PortfolioDesign(portfolioDesignPath) shouldEqual PortfolioDesign(expectedETFSelections)

  }

}
