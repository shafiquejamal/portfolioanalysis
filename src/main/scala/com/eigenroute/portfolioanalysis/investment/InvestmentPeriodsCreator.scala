package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, PortfolioDesign}
import com.eigenroute.portfolioanalysis.util.RichJoda._
import org.joda.time.{DateTime, Days}

class InvestmentPeriodsCreator(
  portfolioDesign: PortfolioDesign,
  sortedCommonDatesETFData:Seq[ETFDataPlus],
  investmentDurationYears: Int) {

  val allDates: List[DateTime] =
    sortedCommonDatesETFData.toList.map(eTFData => javaSQLDateToDateTime(eTFData.asOfDate)).distinct
  val maybeEarliestDate: Option[DateTime] = allDates.headOption
  val maybeLatestDate: Option[DateTime] = allDates.reverse.headOption

  def create:Seq[InvestmentPeriod] =
    (for {
      earliestDate <- maybeEarliestDate.toSeq
      latestDate <- maybeLatestDate.toSeq
      daysBetween =
        Days.daysBetween(earliestDate.toLocalDate, latestDate.minusYears(investmentDurationYears)
        .plusDays(1).toLocalDate).getDays
      day <- 0.to(daysBetween)
      if !earliestDate.plusYears(investmentDurationYears).isAfter(latestDate)
      startOfPeriod = earliestDate.plusDays(day)
      endOfPeriod = earliestDate.plusYears(investmentDurationYears).plusDays(day)
    } yield {
      InvestmentPeriod(startOfPeriod, endOfPeriod)
    }).filter( iP => allDates.contains(iP.startDate))

}
