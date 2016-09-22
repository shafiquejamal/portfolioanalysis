package com.eigenroute.portfolioanalysis.investment

import org.joda.time.DateTime

case class InvestmentPeriod(startDate: DateTime, endDate: DateTime) {
  require(startDate.isBefore(endDate))
}
