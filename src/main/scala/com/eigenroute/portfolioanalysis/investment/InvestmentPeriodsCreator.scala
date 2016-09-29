package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, PortfolioDesign}
import org.apache.spark.sql.Dataset
import org.joda.time.{DateTime, Days}
import com.eigenroute.portfolioanalysis.util.RichJoda._

class InvestmentPeriodsCreator(
    portfolioDesign: PortfolioDesign,
    sortedCommonDatesDataset:Dataset[ETFDataPlus],
    investmentDurationYears: Int) {

  val allDates: List[DateTime] =
    sortedCommonDatesDataset.collect().toList.map(eTFData => javaSQLDateToDateTime(eTFData.asOfDate)).distinct
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
