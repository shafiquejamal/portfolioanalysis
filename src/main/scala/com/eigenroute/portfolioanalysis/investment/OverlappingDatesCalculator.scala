package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, PortfolioDesign}
import org.apache.spark.sql.Dataset
import org.joda.time.DateTime

class OverlappingDatesCalculator(portfolioDesign: PortfolioDesign, ds:Dataset[ETFDataPlus]) {

  private val datasets =
    portfolioDesign.eTFSelections.map { selection => ds.filter(_.eTFCode == selection.eTFCode).orderBy("asOfDate") }
  private val eTFDatas = datasets.map(_.collect().toSeq)
  private val datesForFirstETF: Seq[DateTime] =
    eTFDatas.headOption.map{eTFDatas => eTFDatas.map( eTFData => new DateTime(eTFData.asOfDate))}.toSeq.flatten

  def overlappingDates: Seq[DateTime] =
    eTFDatas.foldLeft(datesForFirstETF){ case (accumulatedCommonDates, eTFData) =>
      accumulatedCommonDates.intersect(eTFData.map( eTFData => new DateTime(eTFData.asOfDate.getTime)))
    }.sortBy(_.getMillis)

}
