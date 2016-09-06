package com.eigenroute.portfolioanalysis

import java.sql.Timestamp

import org.joda.time.DateTime

case class ETFCode(code:String) {
  require(code.length == 3)
}

case class ETFSelection(eTFCode:ETFCode, desiredWeight: Double) {
  require(desiredWeight >= 0 & desiredWeight <= 1)
}

case class PortfolioDesign(eTFSelections:Seq[ETFSelection], tolerance:Double = 0.00001) {
  require( math.pow(eTFSelections.map(_.desiredWeight).sum - 1, 2) <= tolerance)
}

case class ETFData(asOfDate:Timestamp, code:String, xnumber:String, nAV:Double, exDividend:Double)

case class ETFDataPlus(asOfDate:Timestamp, eTFCode:ETFCode, xnumber:String, nAV:Double, exDividend:Double, quantity:Double, cash:BigDecimal = 0)

case class PortfolioSnapshot(eTFDatas:Seq[ETFDataPlus]) {
  require(eTFDatas.map(_.eTFCode.code).distinct.size == eTFDatas.size)
}

case class PortfolioAllocation(eTFCode:ETFCode, quantity: BigDecimal)