package com.eigenroute.portfolioanalysis.investment

case class PortfolioPerformance(
  investmentPeriod: InvestmentPeriod,
  averageAnnualReturnFraction: BigDecimal = 0
)
