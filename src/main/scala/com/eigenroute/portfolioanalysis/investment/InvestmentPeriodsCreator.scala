package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, PortfolioDesign}
import org.apache.spark.sql.Dataset
import org.joda.time.{DateTime, Days}

class InvestmentPeriodsCreator(portfolioDesign: PortfolioDesign, ds:Dataset[ETFDataPlus]) {

  val overlappingDates = new OverlappingDatesCalculator(portfolioDesign, ds).overlappingDates
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
