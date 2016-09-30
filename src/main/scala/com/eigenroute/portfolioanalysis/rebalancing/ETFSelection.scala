package com.eigenroute.portfolioanalysis.rebalancing

import java.sql.{Timestamp, Date}

case class ETFCode(code: String) {
  require(code.length == 3)
}

case class ETFSelection(eTFCode: ETFCode, desiredWeight: BigDecimal) {
  require(desiredWeight >= 0 & desiredWeight <= 1)
}

case class ETFDataRaw(asOfDate: Timestamp, code: String, xnumber: String, nAV: Double, exDividend: Double)

case class ETFDataPlus(
    asOfDate: Date,
    eTFCode: ETFCode,
    xnumber: String,
    nAV: BigDecimal,
    exDividend: BigDecimal,
    quantity: BigDecimal,
    cash: BigDecimal = 0)

object ETFDataPlus {
  def reverseOrder[A <: ETFDataPlus]: Ordering[A] = new Ordering[A] {
    override def compare(x: A, y: A): Int = {
      -1*x.asOfDate.compareTo(y.asOfDate)
    }
  }
}