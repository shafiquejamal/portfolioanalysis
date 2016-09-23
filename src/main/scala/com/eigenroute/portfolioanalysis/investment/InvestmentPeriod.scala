package com.eigenroute.portfolioanalysis.investment

import org.joda.time.{DateTime, Months}

case class InvestmentPeriod(startDate: DateTime, endDate: DateTime) {
  require(startDate.isBefore(endDate))
}

object InvestmentPeriod {
  def lengthInMonths(investmentPeriod: InvestmentPeriod): Int =
    Months.monthsBetween(investmentPeriod.startDate.toLocalDate, investmentPeriod.endDate.toLocalDate).getMonths
}