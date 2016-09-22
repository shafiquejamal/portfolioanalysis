package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, PortfolioDesign}
import org.apache.spark.sql.Dataset
import org.joda.time.{DateTime, Days}

case class InvestmentPeriod(startDate:DateTime, endDate:DateTime) {
  require(startDate.isBefore(endDate))
}

class InvestmentPeriodsCreator(portfolioDesign: PortfolioDesign, ds:Dataset[ETFDataPlus]) {

  private val datasets =
    portfolioDesign.eTFSelections.map { selection => ds.filter(_.eTFCode == selection.eTFCode).orderBy("asOfDate") }
  private val eTFDates = datasets.map(_.collect().toSeq)
  private val datesForFirstETF = eTFDates.headOption.map{eTFDatas => eTFDatas.map(_.asOfDate)}.toSeq.flatten
  private val overlappingDates:Seq[DateTime] =
    eTFDates.foldLeft(datesForFirstETF.map(date => new DateTime(date.getTime))){ case (accumulatedCommonDates, eTFDatas) =>
    accumulatedCommonDates.intersect(eTFDatas.map( eTFData => new DateTime(eTFData.asOfDate.getTime)))
  }.sortBy(_.getMillis)
  val earliestDateForAllETFs:Option[DateTime] = overlappingDates.headOption
  val latestDateForAllETFs:Option[DateTime] = overlappingDates.reverse.headOption

  def create(
    portfolioDesign: PortfolioDesign,
    investmentDurationYears: Int):Seq[InvestmentPeriod] =
    (for {
      earliestDate <- earliestDateForAllETFs.toSeq
      latestDate <- latestDateForAllETFs.toSeq
      if !earliestDate.plusYears(investmentDurationYears).isAfter(latestDate)
      daysBetween = Days.daysBetween(earliestDate.toLocalDate, latestDate.minusYears(investmentDurationYears).plusDays(1).toLocalDate).getDays
      day <- 0.to(daysBetween)
    } yield {
      InvestmentPeriod(earliestDate.plusDays(day), earliestDate.plusYears(investmentDurationYears).plusDays(day))
    }).filter { iP => overlappingDates.contains(iP.startDate)}
      .map { iP =>
        if (overlappingDates.contains(iP.endDate))
          iP
        else
          searchForLatestEarlierCommonDate(iP.endDate).fold(iP){ date => iP.copy(endDate = date)}
       }

  def searchForLatestEarlierCommonDate(date:DateTime):Option[DateTime] = {
    if (overlappingDates.contains(date))
      Some(date)
    else if (overlappingDates.headOption.fold(true)(_.isAfter(date)))
      None
    else
      searchForLatestEarlierCommonDate(date.minusDays(1))
  }

}
