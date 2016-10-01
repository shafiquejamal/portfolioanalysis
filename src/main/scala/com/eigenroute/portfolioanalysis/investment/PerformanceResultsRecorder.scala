package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.rebalancing.PortfolioDesign
import org.apache.poi.xssf.usermodel.{XSSFSheet, XSSFWorkbook}
import org.joda.time.format.DateTimeFormat

import scala.util.{Failure, Success, Try}

class PerformanceResultsRecorder(
    investmentDurationInYears: Int,
    rebalancingInterval: RebalancingInterval,
    initialInvestment: BigDecimal,
    perTransactionTradingCost: BigDecimal,
    bidAskCostFractionOfNav: BigDecimal,
    portfolioDesign: PortfolioDesign,
    maxAllowedDeviation: BigDecimal,
    performances: Seq[PortfolioPerformance]) {

  def write(pattern: String = "yyyy-MM-dd"): XSSFWorkbook = {
    val dateTimeFormatter = DateTimeFormat.forPattern(pattern)
    val wb = new XSSFWorkbook
    val paramsSheet = wb.createSheet("Parameters")

    addKeyValue(paramsSheet, 0, "Investment duration (years)", investmentDurationInYears.toString)
    addKeyValue(paramsSheet, 1, "Rebalancing interval", rebalancingInterval.toString)
    addKeyValue(paramsSheet, 2, "Initial Investment", initialInvestment.toString)
    addKeyValue(paramsSheet, 3, "Cost to trade ETF", perTransactionTradingCost.toString)
    addKeyValue(paramsSheet, 4, "Bid-Ask cost as a fraction of NAV", bidAskCostFractionOfNav.toString)
    addKeyValue(paramsSheet, 5, "Maximum allowed deviation from desired weights (percentage points/100)",
      maxAllowedDeviation.toString)
    addKeyValue(paramsSheet, 7, "Portfolio Design", "")
    portfolioDesign.eTFSelections.zipWithIndex.foreach { case (selection, index) =>
      addKeyValue(paramsSheet, 8 + index, selection.eTFCode.code, selection.desiredWeight.toString)
    }

    val resultsSheet = wb.createSheet("Results")

    addRow(resultsSheet, 0, Seq("Start Date", "End Date", "Average Annual Return"))
    performances.zipWithIndex.foreach { case (performance, index) =>
      addRow(resultsSheet, 1 + index,
        Seq(
          dateTimeFormatter.print(performance.investmentPeriod.startDate),
          dateTimeFormatter.print(performance.investmentPeriod.endDate),
          performance.averageAnnualReturnFraction.toString)
        )
    }

    wb
  }

  private def addKeyValue(sheet: XSSFSheet, rowNumber: Int, key: String, value: String): Unit =
    addRow(sheet, rowNumber, Seq(key, value))

  private def addRow(sheet: XSSFSheet, rowNumber: Int, contents:Seq[String]): Unit = {
    val row = sheet.createRow(rowNumber)
    contents.zipWithIndex.foreach { case (content, index) =>
      val cell = row.createCell(index)
      Try(content.toDouble) match {
        case Success(success) =>
          cell.setCellValue(success)
        case Failure(_) =>
          cell.setCellValue(content)
      }
    }
  }

}
