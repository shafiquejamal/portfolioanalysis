package com.eigenroute.portfolioanalysis.investment

import java.sql.Date

import com.eigenroute.portfolioanalysis.Config
import com.eigenroute.portfolioanalysis.rebalancing.{PortfolioDesign, ETFCode, ETFDataRaw, ETFDataPlus}
import org.apache.spark.sql.SparkSession
import com.eigenroute.portfolioanalysis.util.RichJoda._

class ETFDataFetcher {

  def fetch(portfolioDesign: PortfolioDesign)(implicit sparkSession: SparkSession): Seq[ETFDataPlus] = {
    import sparkSession.implicits._

    val rawETFData = sparkSession.read.format("jdbc").options(Config.dBParams).load.as[ETFDataRaw]
    val eTFData: Seq[ETFDataPlus] =
      rawETFData.filter{ eTFData =>
        portfolioDesign.eTFSelections.map(_.eTFCode.code).contains(eTFData.code)
      }
      .map { filteredETFData =>
        ETFDataPlus(
          new Date(filteredETFData.asOfDate.getTime),
          ETFCode(filteredETFData.code),
          filteredETFData.xnumber,
          filteredETFData.nAV,
          filteredETFData.exDividend, 0, 0)
         }.collect().toSeq.sortWith( (eTFData1, eTFData2) => eTFData1.asOfDate.isBefore(eTFData2.asOfDate) )
    new OverlappingDatesCalculator(portfolioDesign).eTFDataOnlyWithEntriesHavingOverlappingDates(eTFData)
  }

}
