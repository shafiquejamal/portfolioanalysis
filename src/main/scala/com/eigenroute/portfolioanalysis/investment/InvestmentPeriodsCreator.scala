package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, PortfolioDesign}
import org.apache.spark.sql.Dataset
import org.joda.time.{DateTime, Days}

class InvestmentPeriodsCreator(
    portfolioDesign: PortfolioDesign,
    sortedCommonDatesDataset:Dataset[ETFDataPlus],
    investmentDurationYears: Int) {

  val earliestDate:DateTime = new DateTime(sortedCommonDatesDataset.head.asOfDate.getTime)
  val maybeLatestDate:Option[DateTime] =
    sortedCommonDatesDataset.rdd.takeOrdered(1)(ETFDataPlus.reverseOrder)
    .headOption.map(eTFData => new DateTime(eTFData.asOfDate))

  def create:Seq[InvestmentPeriod] =
    for {
      latestDate <- maybeLatestDate.toSeq
      daysBetween =
        Days.daysBetween(earliestDate.toLocalDate, latestDate.minusYears(investmentDurationYears)
        .plusDays(1).toLocalDate).getDays
      day <- 0.to(daysBetween)
      if !earliestDate.plusYears(investmentDurationYears).isAfter(latestDate)
    } yield {
      InvestmentPeriod(earliestDate.plusDays(day), earliestDate.plusYears(investmentDurationYears).plusDays(day))
    }

}
