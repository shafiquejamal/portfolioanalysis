package com.eigenroute.portfolioanalysis.rebalancing

import org.joda.time.DateTime
import scalikejdbc.WrappedResultSet

case class ETFData(
    asOfDate: DateTime,
    eTFCode: ETFCode,
    xnumber: String,
    nAV: BigDecimal,
    exDividend: BigDecimal,
    quantity: BigDecimal)

object ETFData {
  def reverseOrder[A <: ETFData]: Ordering[A] = new Ordering[A] {
    override def compare(x: A, y: A): Int = {
      -1*x.asOfDate.compareTo(y.asOfDate)
    }
  }

  def converter(rs:WrappedResultSet): ETFData = ETFData(
    rs.jodaDateTime("asofdate"),
    ETFCode(rs.string("code")),
    rs.string("xnumber"),
    rs.double("nav"),
    rs.bigDecimal("exdividend"), 0)
}
