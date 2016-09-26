package com.eigenroute.portfolioanalysis.rebalancing

case class ETFDesiredValue(eTFCode: ETFCode, value: BigDecimal, isToTrade: Boolean)