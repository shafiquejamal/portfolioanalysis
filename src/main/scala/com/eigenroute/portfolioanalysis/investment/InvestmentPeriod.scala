package com.eigenroute.portfolioanalysis.investment

import org.joda.time.{Years, DateTime, Months}

case class InvestmentPeriod(startDate: DateTime, endDate: DateTime) {
  require(startDate.isBefore(endDate))
  require(InvestmentPeriod.lengthInYears(this) == InvestmentPeriod.lengthInMonths(this)/12)
}

object InvestmentPeriod {
  def lengthInMonths(investmentPeriod: InvestmentPeriod): Int =
    Months.monthsBetween(investmentPeriod.startDate.toLocalDate, investmentPeriod.endDate.toLocalDate).getMonths
  def lengthInYears(investmentPeriod: InvestmentPeriod): Int =
    Years.yearsBetween(investmentPeriod.startDate.toLocalDate, investmentPeriod.endDate.toLocalDate).getYears
}