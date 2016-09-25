package com.eigenroute.portfolioanalysis.rebalancing

trait PortfolioValueCalculation {

  def portfolioValue(portfolioSnapshot: PortfolioSnapshot): Double =
    portfolioValueFromETFDatas(portfolioSnapshot.sameDateUniqueCodesETFDatas)

  def portfolioValueFromETFDatas(eTFDatas: Seq[ETFDataPlus]): Double =
    eTFDatas.map { eTFData => eTFData.nAV * eTFData.quantity }.sum

  def actualValue(portfolioSnapshot: PortfolioSnapshot, eTFCode: ETFCode): Double =
    portfolioSnapshot.sameDateUniqueCodesETFDatas.find(_.eTFCode == eTFCode)
    .map(eTFDATA => eTFDATA.nAV * eTFDATA.quantity).getOrElse(0d)

}
