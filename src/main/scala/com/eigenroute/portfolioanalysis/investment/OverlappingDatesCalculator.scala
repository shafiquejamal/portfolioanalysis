package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFData, PortfolioDesign}
import org.joda.time.DateTime

class OverlappingDatesCalculator(portfolioDesign: PortfolioDesign) {

  def overlappingDates(eTFData: Seq[ETFData]): Seq[DateTime] = {
    val eTFDatas =
      portfolioDesign.eTFSelections.map { selection => eTFData.filter(_.eTFCode == selection.eTFCode) }
    overlappingDatesFromSplitETFData(eTFDatas)
  }

  def overlappingDatesFromSplitETFData(eTFDatas: Seq[Seq[ETFData]]): Seq[DateTime] = {
    val datesForFirstETF: Seq[DateTime] =
      eTFDatas.headOption.map { eTFDatas => eTFDatas.map(eTFData => new DateTime(eTFData.asOfDate)) }.toSeq.flatten

    eTFDatas.foldLeft(datesForFirstETF) { case (accumulatedCommonDates, eTFData) =>
      accumulatedCommonDates.intersect(eTFData.map(_.asOfDate))
    }
  }

  def eTFDataOnlyWithEntriesHavingOverlappingDates(eTFData: Seq[ETFData]): Seq[ETFData] = {
    val datesToRetain = overlappingDates(eTFData)
    eTFData.filter ( data => datesToRetain.contains(data.asOfDate) )
  }


}
