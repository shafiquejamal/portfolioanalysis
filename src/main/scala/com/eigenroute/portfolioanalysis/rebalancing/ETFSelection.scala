package com.eigenroute.portfolioanalysis.rebalancing

import java.sql.Date

case class ETFCode(code: String) {
  require(code.length == 3)
}

case class ETFSelection(eTFCode: ETFCode, desiredWeight: Double) {
  require(desiredWeight >= 0 & desiredWeight <= 1)
}

case class PortfolioDesign(eTFSelections: Seq[ETFSelection], tolerance: Double = 0.00001) {
  require(math.pow(eTFSelections.map(_.desiredWeight).sum - 1, 2) <= tolerance)
}

case class ETFData(asOfDate: Date, code: String, xnumber: String, nAV: Double, exDividend: Double)

case class ETFDataPlus(
    asOfDate: Date,
    eTFCode: ETFCode,
    xnumber: String,
    nAV: Double,
    exDividend: Double,
    quantity: Double,
    cash: BigDecimal = 0)

object ETFDataPlus {
  def reverseOrder[A <: ETFDataPlus]: Ordering[A] = new Ordering[A] {
    override def compare(x: A, y: A): Int = {
      -1*x.asOfDate.compareTo(y.asOfDate)
    }
  }
}

case class ETFQuantity(eTFCode: ETFCode, quantity: BigDecimal)
