package com.eigenroute.portfolioanalysis

import java.io.File
import java.sql.Date

import com.eigenroute.portfolioanalysis.investment.{OverlappingDatesCalculator, PortfolioSimulation, RebalancingInterval}
import com.eigenroute.portfolioanalysis.rebalancing._
import com.eigenroute.portfolioanalysis.util.RichJoda._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.util.Try

object Main {

  def main(args: Array[String]): Unit = {

    val investmentDurationYears: Int = Try(args(0).toInt).toOption.getOrElse(10)
    val rebalancingInterval: RebalancingInterval = RebalancingInterval.rebalancingInterval(args(1))
    val initialInvestment: BigDecimal = BigDecimal(args(2))
    val perTransactionTradingCost: BigDecimal = BigDecimal(args(3))
    val bidAskCostFractionOfNav: BigDecimal = BigDecimal(args(4))
    val maxAllowedDeviation: BigDecimal = BigDecimal(args(5))
    val portfolioDesignPath = new File(args(6))

    val rawPortfolioDesignData = scala.io.Source.fromFile(portfolioDesignPath).mkString.split("\n").toSeq

    val eTFSelections: Seq[ETFSelection] = rawPortfolioDesignData.map { cSVLine =>
        val codeAndWeight = cSVLine.split(",")
        ETFSelection(ETFCode(codeAndWeight(0)), BigDecimal(codeAndWeight(1)))
    }

    val portfolioDesign = PortfolioDesign(eTFSelections)

    val conf =
      new SparkConf()
      .setAppName("Simple Application").setMaster("local")
      .set("spark.rpc.netty.dispatcher.numThreads","2")
      .set("spark.driver.allowMultipleContexts", "true")
    val spark = SparkSession.builder().appName("financial_data").master("local").config(conf).getOrCreate()
    import spark.implicits._

    val rawETFData = spark.read.format("jdbc").options(Config.dBParams).load.as[ETFDataRaw]
    val eTFData: Seq[ETFDataPlus] = rawETFData.filter{ eTFData =>
        portfolioDesign.eTFSelections.map(_.eTFCode.code).contains(eTFData.code)
      }
      .map { filteredETFData =>
      ETFDataPlus(
       new Date(
        filteredETFData.asOfDate.getTime),
        ETFCode(filteredETFData.code),
        filteredETFData.xnumber,
        filteredETFData.nAV,
        filteredETFData.exDividend, 0, 0)
      }.collect().toSeq.sortWith( (eTFData1, eTFData2) => eTFData1.asOfDate.isBefore(eTFData2.asOfDate) )
    val sortedCommonDatesETFData =
      new OverlappingDatesCalculator(portfolioDesign).eTFDataOnlyWithEntriesHavingOverlappingDates(eTFData)

    val simulationResults =
      new PortfolioSimulation(
        investmentDurationYears,
        rebalancingInterval,
        initialInvestment,
        perTransactionTradingCost,
        bidAskCostFractionOfNav,
        portfolioDesign,
        maxAllowedDeviation,
        sortedCommonDatesETFData
      ).simulate

    simulationResults.foreach(println)
    println(simulationResults.length)
    println(simulationResults.map(_.averageAnnualReturnFraction).sum/simulationResults.length)

  }

}
