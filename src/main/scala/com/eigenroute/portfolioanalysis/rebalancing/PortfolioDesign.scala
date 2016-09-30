package com.eigenroute.portfolioanalysis.rebalancing

import java.io.File

case class PortfolioDesign(eTFSelections: Seq[ETFSelection], tolerance: BigDecimal = 0.00001) {
  require(math.pow((eTFSelections.map(_.desiredWeight).sum - 1).toDouble, 2) <= tolerance)
}

object PortfolioDesign {
  def apply(filePath: File): PortfolioDesign = {
    val rawPortfolioDesignData = scala.io.Source.fromFile(filePath).mkString.split("\n").toSeq

    val eTFSelections: Seq[ETFSelection] = rawPortfolioDesignData.map { cSVLine =>
      val codeAndWeight = cSVLine.split(",")
      ETFSelection(ETFCode(codeAndWeight(0)), BigDecimal(codeAndWeight(1)))
    }
    PortfolioDesign(eTFSelections)
  }
}
