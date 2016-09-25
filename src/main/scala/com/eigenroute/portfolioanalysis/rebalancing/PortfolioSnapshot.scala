package com.eigenroute.portfolioanalysis.rebalancing

import org.apache.spark.sql.Dataset

case class PortfolioSnapshot(sameDateUniqueCodesETFDatas: Seq[ETFDataPlus]) {
  require(sameDateUniqueCodesETFDatas.map(_.eTFCode.code).distinct.size == sameDateUniqueCodesETFDatas.size)
}

object PortfolioSnapshot {
  def apply(portfolioDesign: PortfolioDesign, commonDatesDatasets: Seq[Dataset[ETFDataPlus]]): PortfolioSnapshot = {
    val earliestDateUniqueCodesETFData =
      commonDatesDatasets.map { ds =>
        ds.head }
      .filter { eTFData =>
        portfolioDesign.eTFSelections.map(_.eTFCode).contains(eTFData.eTFCode)}
    PortfolioSnapshot(earliestDateUniqueCodesETFData)
  }
}