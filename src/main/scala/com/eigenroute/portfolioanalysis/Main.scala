package com.eigenroute.portfolioanalysis

import java.io.{FileOutputStream, File}

import com.eigenroute.portfolioanalysis.db.DevProdDBConfig
import com.eigenroute.portfolioanalysis.investment._
import com.eigenroute.portfolioanalysis.rebalancing._

import scala.util.Try

object Main {

  def main(args: Array[String]): Unit = {

    val dBConfig = new DevProdDBConfig()
    dBConfig.setUpAllDB()

    val investmentDurationYears: Int = Try(args(0).toInt).toOption.getOrElse(10)
    val rebalancingInterval: RebalancingInterval = RebalancingInterval.rebalancingInterval(args(1))
    val initialInvestment: BigDecimal = BigDecimal(args(2))
    val perTransactionTradingCost: BigDecimal = BigDecimal(args(3))
    val bidAskCostFractionOfNav: BigDecimal = BigDecimal(args(4))
    val maxAllowedDeviation: BigDecimal = BigDecimal(args(5))
    val portfolioDesignPath = new File(args(6))
    val outputFilePath = new File(args(7))
    val portfolioDesign = PortfolioDesign(portfolioDesignPath)
    val sortedCommonDatesETFData = new ETFDataFetcher(new ETFDAO(new DevProdDBConfig())).fetch(portfolioDesign)

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

    val wb =
      new PerformanceResultsRecorder(
        investmentDurationYears,
        rebalancingInterval,
        initialInvestment,
        perTransactionTradingCost,
        bidAskCostFractionOfNav,
        portfolioDesign,
        maxAllowedDeviation,
        simulationResults).write()

    wb.write(new FileOutputStream(outputFilePath))

    dBConfig.closeAll()

  }

}
