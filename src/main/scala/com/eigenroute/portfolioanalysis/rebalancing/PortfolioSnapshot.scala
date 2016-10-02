package com.eigenroute.portfolioanalysis.rebalancing

case class PortfolioSnapshot(sameDateUniqueCodesETFDatas: Seq[ETFData]) {
  require(sameDateUniqueCodesETFDatas.map(_.eTFCode.code).distinct.size == sameDateUniqueCodesETFDatas.size)
}
