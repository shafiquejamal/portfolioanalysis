package com.eigenroute.portfolioanalysis

import java.io.File
import java.sql.Date

import com.eigenroute.portfolioanalysis.investment.{ETFDataFetcher, OverlappingDatesCalculator, PortfolioSimulation,
RebalancingInterval}
import com.eigenroute.portfolioanalysis.rebalancing._
import com.eigenroute.portfolioanalysis.util.RichJoda._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.util.Try

object Main {

  def main(args: Array[String]): Unit = {

    val conf =
      new SparkConf()
      .setAppName("Simple Application").setMaster("local")
      .set("spark.rpc.netty.dispatcher.numThreads","2")
      .set("spark.driver.allowMultipleContexts", "true")
    implicit val sparkSession = SparkSession.builder().appName("financial_data").master("local").config(conf).getOrCreate()

    val investmentDurationYears: Int = Try(args(0).toInt).toOption.getOrElse(10)
    val rebalancingInterval: RebalancingInterval = RebalancingInterval.rebalancingInterval(args(1))
    val initialInvestment: BigDecimal = BigDecimal(args(2))
    val perTransactionTradingCost: BigDecimal = BigDecimal(args(3))
    val bidAskCostFractionOfNav: BigDecimal = BigDecimal(args(4))
    val maxAllowedDeviation: BigDecimal = BigDecimal(args(5))
    val portfolioDesignPath = new File(args(6))
    val portfolioDesign = PortfolioDesign(portfolioDesignPath)
    val sortedCommonDatesETFData = new ETFDataFetcher().fetch(portfolioDesign)

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
