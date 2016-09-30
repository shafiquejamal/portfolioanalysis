package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, PortfolioDesign}
import org.joda.time.DateTime
import com.eigenroute.portfolioanalysis.util.RichJoda._

class OverlappingDatesCalculator(portfolioDesign: PortfolioDesign) {

  def overlappingDates(eTFData: Seq[ETFDataPlus]): Seq[DateTime] = {
    val datasets =
      portfolioDesign.eTFSelections.map { selection => eTFData.filter(_.eTFCode == selection.eTFCode) }
    overlappingDatesFromSplitETFData(datasets)
  }

  def overlappingDatesFromSplitETFData(eTFDatas: Seq[Seq[ETFDataPlus]]): Seq[DateTime] = {
    val datesForFirstETF: Seq[DateTime] =
      eTFDatas.headOption.map { eTFDatas => eTFDatas.map(eTFData => new DateTime(eTFData.asOfDate)) }.toSeq.flatten

    eTFDatas.foldLeft(datesForFirstETF) { case (accumulatedCommonDates, eTFData) =>
      accumulatedCommonDates.intersect(eTFData.map(eTFData => new DateTime(eTFData.asOfDate.getTime)))
    }
  }

  def eTFDataOnlyWithEntriesHavingOverlappingDates(eTFData: Seq[ETFDataPlus]): Seq[ETFDataPlus] = {
    val datesToRetain = overlappingDates(eTFData).map(dateTimeToJavaSQLDate)
    eTFData.filter ( data => datesToRetain.contains(data.asOfDate) )
  }


}
