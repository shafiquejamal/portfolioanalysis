package com.eigenroute.portfolioanalysis.rebalancing

import org.joda.time.DateTime
import scalikejdbc.WrappedResultSet

case class ETFCode(code: String) {
  require(code.length == 3)
}

case class ETFSelection(eTFCode: ETFCode, desiredWeight: BigDecimal) {
  require(desiredWeight >= 0 & desiredWeight <= 1)
}

case class ETFDataPlus(
    asOfDate: DateTime,
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

  def converter(rs:WrappedResultSet): ETFDataPlus = ETFDataPlus(
    rs.jodaDateTime("asofdate"), ETFCode(rs.string("code")), rs.string("xnumber"), rs.double("nav"), rs.bigDecimal("exdividend"), 0, 0)
}