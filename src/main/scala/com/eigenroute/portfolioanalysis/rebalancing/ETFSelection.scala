package com.eigenroute.portfolioanalysis.rebalancing

case class ETFCode(code: String) {
  require(code.length == 3)
}

case class ETFSelection(eTFCode: ETFCode, desiredWeight: BigDecimal) {
  require(desiredWeight >= 0 & desiredWeight <= 1)
}