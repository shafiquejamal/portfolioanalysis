package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.scalatest.{ShouldMatchers, FlatSpec}

class PerformanceResultsRecorderUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  "The results recorder" should "create a workbook with the parameters in the first sheet and the performances results in " +
  "the second sheet" in {

    val performances: Seq[PortfolioPerformance] =
      Seq(
        PortfolioPerformance(InvestmentPeriod(now, now.plusYears(6)), 0.12),
        PortfolioPerformance(InvestmentPeriod(now.plusDays(1), now.plusYears(6).plusDays(1)), 0.12),
        PortfolioPerformance(InvestmentPeriod(now.plusDays(2), now.plusYears(6).plusDays(2)), 1.52),
        PortfolioPerformance(InvestmentPeriod(now.plusDays(3), now.plusYears(6).plusDays(4)), 2.03)
      )
    val recorder =
      new PerformanceResultsRecorder(
        6, RebalancingInterval.Monthly, 234000, 9.99, 0.0014, portfolioDesign, 0.04, performances)

    val wb = recorder.write("yy,M,d")

    wb.getSheetAt(0).getRow(2).getCell(0).getStringCellValue shouldEqual "Initial Investment"
    wb.getSheetAt(0).getRow(2).getCell(1).getStringCellValue shouldEqual "234000"

    wb.getSheetAt(1).getRow(3).getCell(0).getStringCellValue shouldEqual "16,1,3"
    wb.getSheetAt(1).getRow(3).getCell(1).getStringCellValue shouldEqual "22,1,3"
    wb.getSheetAt(1).getRow(3).getCell(2).getStringCellValue shouldEqual "1.52"

    recorder.write().getSheetAt(1).getRow(3).getCell(1).getStringCellValue shouldEqual "2022-01-03"
  }


}
