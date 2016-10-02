package com.eigenroute.portfolioanalysis.rebalancing

trait PortfolioValueCalculation {

  def portfolioValueETFsOnly(portfolioSnapshot: PortfolioSnapshot): BigDecimal =
    portfolioValueFromETFDatas(portfolioSnapshot.sameDateUniqueCodesETFDatas)

  def portfolioValueFromETFDatas(eTFDatas: Seq[ETFData]): BigDecimal =
    eTFDatas.map { eTFData => eTFData.nAV * eTFData.quantity }.sum

  def actualValue(portfolioSnapshot: PortfolioSnapshot, eTFCode: ETFCode): BigDecimal =
    portfolioSnapshot.sameDateUniqueCodesETFDatas.find(_.eTFCode == eTFCode)
    .map(eTFDATA => eTFDATA.nAV * eTFDATA.quantity).getOrElse(0d)

}
