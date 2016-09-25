package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, PortfolioDesign}
import org.apache.spark.sql.Dataset
import org.joda.time.DateTime

class OverlappingDatesCalculator(portfolioDesign: PortfolioDesign) {

  def overlappingDates(ds:Dataset[ETFDataPlus]): Seq[DateTime] = {
    val datasets =
      portfolioDesign.eTFSelections.map { selection => ds.filter(_.eTFCode == selection.eTFCode) }
    overlappingDates(datasets)
  }

  def overlappingDates(datasets:Seq[Dataset[ETFDataPlus]]): Seq[DateTime] = {
    val eTFDatas = datasets.map(_.collect().toSeq)
    val datesForFirstETF: Seq[DateTime] =
      eTFDatas.headOption.map{eTFDatas => eTFDatas.map( eTFData => new DateTime(eTFData.asOfDate))}.toSeq.flatten

    eTFDatas.foldLeft(datesForFirstETF) { case (accumulatedCommonDates, eTFData) =>
      accumulatedCommonDates.intersect(eTFData.map(eTFData => new DateTime(eTFData.asOfDate.getTime)))
    }
  }


}
