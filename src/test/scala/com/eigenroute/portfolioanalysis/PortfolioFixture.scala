package com.eigenroute.portfolioanalysis

import java.sql.Date

import com.eigenroute.portfolioanalysis.investment.InvestmentPeriod
import com.eigenroute.portfolioanalysis.rebalancing._
import com.eigenroute.portfolioanalysis.util.RichJoda._
import org.joda.time.DateTime

trait PortfolioFixture {

  val now = new Date(new DateTime(2016, 1, 1, 1, 1, 1).getMillis)

  val eTFA = ETFCode("AAA")
  val eTFB = ETFCode("BBB")
  val eTFC = ETFCode("CCC")
  val eTFD = ETFCode("DDD")
  val eTFNotInSnapshot = ETFCode("BAD")
  val eTFASelection = ETFSelection(eTFA, 0.25)
  val eTFBSelection = ETFSelection(eTFB, 0.5)
  val eTFCSelection = ETFSelection(eTFC, 0.1)
  val eTFDSelection = ETFSelection(eTFD, 0.15)
  val portfolioDesign = PortfolioDesign(Seq(eTFASelection, eTFBSelection, eTFCSelection, eTFDSelection))

  val eTFDataPlusA = ETFDataPlus(now, eTFA, "1", 20, 0, 50, 0d)
  val eTFDataPlusB = ETFDataPlus(now, eTFB, "1", 30, 0, 100, 0d)
  val eTFDataPlusC = ETFDataPlus(now, eTFC, "1", 40, 0, 100, 0d)
  val eTFDataPlusD = ETFDataPlus(now, eTFD, "1", 50, 0, 40, 0d)
  val portfolioSnapshot = PortfolioSnapshot(Seq(eTFDataPlusA, eTFDataPlusB, eTFDataPlusC, eTFDataPlusD))
  val portfolioSnapshotZeroQuantity =
    PortfolioSnapshot(portfolioSnapshot.sameDateUniqueCodesETFDatas.map(_.copy(quantity = 0d)))

  val weightDifferences = Seq(
    PortfolioWeightDifference(eTFA, 0.15),
    PortfolioWeightDifference(eTFB, 0.2),
    PortfolioWeightDifference(eTFC, -0.3),
    PortfolioWeightDifference(eTFD, -0.05)
  )

  def round(numberToRound: BigDecimal, n: Int = 5): BigDecimal = numberToRound.setScale(n, BigDecimal.RoundingMode.HALF_UP)

  trait DesiredValueFixture {
    val expectedDesiredValuesOneToBeTraded = Seq(
      ETFDesiredValue(eTFA, 2355.88235, isToTrade = true),
      ETFDesiredValue(eTFB, 4711.76471, isToTrade = true),
      ETFDesiredValue(eTFC, 942.35294, isToTrade = true),
      ETFDesiredValue(eTFD, 2000.00000, isToTrade = false)
    )

    val expectedDesiredValuesAllToBeTraded = Seq(
      ETFDesiredValue(eTFA, 2500d, isToTrade = true),
      ETFDesiredValue(eTFB, 5000d, isToTrade = true),
      ETFDesiredValue(eTFC, 1000d, isToTrade = true),
      ETFDesiredValue(eTFD, 1500d, isToTrade = true)
    )

    val expectedDesiredValuesAllToBeTradedcost15ExDivCash100 = Seq(
      ETFDesiredValue(eTFA, 2510d, isToTrade = true),
      ETFDesiredValue(eTFB, 5020d, isToTrade = true),
      ETFDesiredValue(eTFC, 1004d, isToTrade = true),
      ETFDesiredValue(eTFD, 1506d, isToTrade = true)
    )

    val expectedDesiredValuesNoTrades = Seq(
      ETFDesiredValue(eTFA, 1000d, isToTrade = false),
      ETFDesiredValue(eTFB, 3000d, isToTrade = false),
      ETFDesiredValue(eTFC, 4000d, isToTrade = false),
      ETFDesiredValue(eTFD, 2000d, isToTrade = false)
    )

    val expectedDesiredValuesFirstTrades = Seq(
      ETFDesiredValue(eTFA, 2500d, isToTrade = true),
      ETFDesiredValue(eTFB, 5000d, isToTrade = true),
      ETFDesiredValue(eTFC, 1000d, isToTrade = true),
      ETFDesiredValue(eTFD, 1500d, isToTrade = true)
    )

    val expectedValueDifferenceOneNotTraded = Seq(
      PortfolioValueDifference(eTFA, 1355.88235),
      PortfolioValueDifference(eTFB, 1711.76471),
      PortfolioValueDifference(eTFC, -3057.64706),
      PortfolioValueDifference(eTFD, 0.00000)
                                                 )

    val expectedValueDifferenceNoTrades = Seq(
      PortfolioValueDifference(eTFA, 0d),
      PortfolioValueDifference(eTFB, 0d),
      PortfolioValueDifference(eTFC, 0d),
      PortfolioValueDifference(eTFD, 0d)
    )

    val expectedValueDifferenceAllTrades = Seq(
      PortfolioValueDifference(eTFA, 1500d),
      PortfolioValueDifference(eTFB, 2000d),
      PortfolioValueDifference(eTFC, -3000d),
      PortfolioValueDifference(eTFD, -500d)
    )

    val expectedValueDifferenceFirstTrades = Seq(
      PortfolioValueDifference(eTFA, 2500d),
      PortfolioValueDifference(eTFB, 5000d),
      PortfolioValueDifference(eTFC, 1000d),
      PortfolioValueDifference(eTFD, 1500d)
    )
  }

  trait EstimatedQuantitiesToAcquire {
    val expectedFirstEstimateQuantitiesAllTrades = Seq(
      PortfolioQuantityToAcquire(eTFA, 74, round(20 * (1 + 0.0011)), 74.91759),
      PortfolioQuantityToAcquire(eTFB, 66, round(30 * (1 + 0.0011)), 66.59341),
      PortfolioQuantityToAcquire(eTFC, -76, round(40 / (1 + 0.0011)), -75.0825),
      PortfolioQuantityToAcquire(eTFD, -11, round(50 / (1 + 0.0011)), -10.011)
    )

    val expectedFirstEstimateQuantitiesAllTradesExpensive = Seq(
      PortfolioQuantityToAcquire(eTFA, 74, round(20 * (1 + 0.0025)), 74.81297),
      PortfolioQuantityToAcquire(eTFB, 66, round(30 * (1 + 0.0025)), 66.50042),
      PortfolioQuantityToAcquire(eTFC, -76, round(40 / (1 + 0.0025)), -75.1875),
      PortfolioQuantityToAcquire(eTFD, -11, round(50 / (1 + 0.0025)), -10.025)
    )

    val expectedFirstEstimateQuantitiesOneNotTraded = Seq(
      PortfolioQuantityToAcquire(eTFA, 67, round(20 * (1 + 0.0011)), 67.71963),
      PortfolioQuantityToAcquire(eTFB, 56, round(30 * (1 + 0.0011)), 56.99613),
      PortfolioQuantityToAcquire(eTFC, -77, round(40 / (1 + 0.0011)), -76.52526),
      PortfolioQuantityToAcquire(eTFD, 0, round(50 / (1 + 0.0011)), 0)
    )

    val expectedFirstEstimateQuantitiesNoTrades = Seq(
      PortfolioQuantityToAcquire(eTFA, 0, round(20 / (1 + 0.0011)), 0),
      PortfolioQuantityToAcquire(eTFB, 0, round(30 / (1 + 0.0011)), 0),
      PortfolioQuantityToAcquire(eTFC, 0, round(40 / (1 + 0.0011)), 0),
      PortfolioQuantityToAcquire(eTFD, 0, round(50 / (1 + 0.0011)), 0)
    )

    val expectedFirstEstimateQuantitiesFirstTrades = Seq(
      PortfolioQuantityToAcquire(eTFA, 124, round(20 * (1 + 0.0011)), 124.86265),
      PortfolioQuantityToAcquire(eTFB, 166, round(30 * (1 + 0.0011)), 166.48353),
      PortfolioQuantityToAcquire(eTFC, 24, round(40 * (1 + 0.0011)), 24.97253),
      PortfolioQuantityToAcquire(eTFD, 29, round(50 * (1 + 0.0011)), 29.96704)
    )
  }

  trait AdditionalQuantitiesFixture {
    val expectedAdditionalQuantitiesAllMatch =
      Seq(AddnlQty(eTFA, 5), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))

    val expectedAdditionalQuantitiesFirstTrades =
      Seq(AddnlQty(eTFA, 5), AddnlQty(eTFB, 3), AddnlQty(eTFC, 2), AddnlQty(eTFD, 2))

    val expectedAdditionalQuantitiesFull = Seq(
      Seq(AddnlQty(eTFA, 5), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 4), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 3), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 2), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 0), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 5), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 4), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 3), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 2), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 0), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 5), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 4), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 3), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 2), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 0), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 5), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 4), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 3), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 2), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0)),
      Seq(AddnlQty(eTFA, 0), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))
    )

    val additionalQuantitiesChosenAllTrades =
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))

    val additionalQuantitiesChosenOneNotTraded =
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 0), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))

    val expectedFinalQuantitiesToAcquireAllTrades = Seq(
      PortfolioQuantityToAcquire(eTFA, 77, round(20 * (1 + 0.0011)), 74.91759),
      PortfolioQuantityToAcquire(eTFB, 68, round(30 * (1 + 0.0011)), 66.59341),
      PortfolioQuantityToAcquire(eTFC, -76, round(40 / (1 + 0.0011)), -75.0825),
      PortfolioQuantityToAcquire(eTFD, -11, round(50 / (1 + 0.0011)), -10.011)
    )

    val expectedFinalQuantitiesAllTrades = Seq(
      FinalPortfolioQuantityToHave(eTFA, 125),
      FinalPortfolioQuantityToHave(eTFB, 168),
      FinalPortfolioQuantityToHave(eTFC, 24),
      FinalPortfolioQuantityToHave(eTFD, 29)
    )

    val expectedFinalQuantitiesOneNotTraded = Seq(
      FinalPortfolioQuantityToHave(eTFA, 118),
      FinalPortfolioQuantityToHave(eTFB, 156),
      FinalPortfolioQuantityToHave(eTFC, 23),
      FinalPortfolioQuantityToHave(eTFD, 40)
    )
  }

  trait InvestmentFixture {
    val startDate = new DateTime(2010, 1, 1, 0, 0, 0)
    val investmentPeriod = InvestmentPeriod(startDate, startDate.plusYears(3))

    val commonStartDate = new DateTime(2001, 1, 1, 0, 0, 0)
    val maxDaysToAdd = 365 * 16
    val commonEndDate = commonStartDate.plusDays(maxDaysToAdd)
    val datesToOmit = Seq(
      new DateTime(commonStartDate.plusDays(905)),
      new DateTime(commonStartDate.plusDays(416)),
      new DateTime(commonStartDate.plusDays(202)),
      new DateTime(commonStartDate.plusDays(4201)),
      new DateTime(commonStartDate.plusDays(4202)),
      new DateTime(commonStartDate.plusDays(4203)),
      new DateTime(commonStartDate.plusDays(4159))
    )

    val eTFPrices: Map[ETFCode, Double] = Map(eTFA -> 20, eTFB -> 30, eTFC -> 40, eTFD -> 50)

    val sortedCommonDatesETFData = portfolioDesign.eTFSelections.flatMap { selection =>
      0.to(maxDaysToAdd).map { daysToAdd =>
        ETFDataPlus(
          commonStartDate.plusDays(daysToAdd),
          selection.eTFCode,
          "1",
          eTFPrices(selection.eTFCode), 0d, 0d, 0)
      }.toSeq
    }

    val startDatePlus1Day = startDate.plusDays(1)
    val startDatePlus3months = startDate.plusMonths(3)
    val startDatePlus6months = startDate.plusMonths(6)
    val startDatePlus9months = startDate.plusMonths(9)
    val startDatePlus12months = startDate.plusMonths(12)
    val startDatePlus18months = startDate.plusMonths(18)
    val startDatePlus24months = startDate.plusMonths(24)
    val startDatePlus30months = startDate.plusMonths(30)
    val startDatePlus36months = startDate.plusMonths(36)
    val startDatePlus3monthsPlus1Day = startDatePlus3months.plusDays(1)
    val startDatePlus6monthsPlus1Day = startDatePlus6months.plusDays(1)
    val startDatePlus9monthsPlus1Day = startDatePlus9months.plusDays(1)
    val startDatePlus12monthsPlus1Day = startDatePlus12months.plusDays(1)
    val startDatePlus12monthsMinus1Day = startDatePlus12months.minusDays(1)
    val startDatePlus24monthsMinus1Day = startDatePlus24months.minusDays(1)
    val startDatePlus36monthsMinus1Day = startDatePlus36months.minusDays(1)
    val investmentInputDataQuarterly: Seq[ETFDataPlus] =
      Seq[ETFDataPlus](
      ETFDataPlus(startDate, eTFA, "1", 20, 1, 0, 0),
      ETFDataPlus(startDate, eTFB, "1", 30, 2, 0, 0),
      ETFDataPlus(startDate, eTFC, "1", 40, 3, 0, 0),
      ETFDataPlus(startDate, eTFD, "1", 50, 4, 0, 0),
      ETFDataPlus(startDatePlus1Day, eTFA, "1", 120, 5, 0, 0),
      ETFDataPlus(startDatePlus1Day, eTFB, "1", 130, 6, 0, 0),
      ETFDataPlus(startDatePlus1Day, eTFC, "1", 140, 7, 0, 0),
      ETFDataPlus(startDatePlus1Day, eTFD, "1", 150, 8, 0, 0),
      ETFDataPlus(startDatePlus3months, eTFA, "1", 30, 0, 0, 0),
      ETFDataPlus(startDatePlus3months, eTFB, "1", 25, 0, 0, 0),
      ETFDataPlus(startDatePlus3months, eTFC, "1", 60, 0, 0, 0),
      ETFDataPlus(startDatePlus3months, eTFD, "1", 40, 0, 0, 0),
      ETFDataPlus(startDatePlus3monthsPlus1Day, eTFA, "1", 230, 6, 0, 0),
      ETFDataPlus(startDatePlus3monthsPlus1Day, eTFB, "1", 225, 7, 0, 0),
      ETFDataPlus(startDatePlus3monthsPlus1Day, eTFC, "1", 260, 8, 0, 0),
      ETFDataPlus(startDatePlus3monthsPlus1Day, eTFD, "1", 240, 9, 0, 0),
      ETFDataPlus(startDatePlus6months, eTFA, "1", 10, 0, 0, 0),
      ETFDataPlus(startDatePlus6months, eTFB, "1", 50, 0, 0, 0),
      ETFDataPlus(startDatePlus6months, eTFC, "1", 80, 0, 0, 0),
      ETFDataPlus(startDatePlus6months, eTFD, "1", 60, 0, 0, 0),
      ETFDataPlus(startDatePlus6monthsPlus1Day, eTFA, "1", 330, 0, 0, 0),
      ETFDataPlus(startDatePlus6monthsPlus1Day, eTFB, "1", 325, 0, 0, 0),
      ETFDataPlus(startDatePlus6monthsPlus1Day, eTFC, "1", 360, 0, 0, 0),
      ETFDataPlus(startDatePlus6monthsPlus1Day, eTFD, "1", 340, 0, 0, 0),
      ETFDataPlus(startDatePlus9months, eTFA, "1", 30, 0, 0, 0),
      ETFDataPlus(startDatePlus9months, eTFB, "1", 45, 0, 0, 0),
      ETFDataPlus(startDatePlus9months, eTFC, "1", 55, 0, 0, 0),
      ETFDataPlus(startDatePlus9months, eTFD, "1", 70, 0, 0, 0),
      ETFDataPlus(startDatePlus9monthsPlus1Day, eTFA, "1", 430, 0, 0, 0),
      ETFDataPlus(startDatePlus9monthsPlus1Day, eTFB, "1", 445, 0, 0, 0),
      ETFDataPlus(startDatePlus9monthsPlus1Day, eTFC, "1", 455, 0, 0, 0),
      ETFDataPlus(startDatePlus9monthsPlus1Day, eTFD, "1", 470, 0, 0, 0),
      ETFDataPlus(startDatePlus12monthsMinus1Day, eTFA, "1", 35, 2, 0, 0),
      ETFDataPlus(startDatePlus12monthsMinus1Day, eTFB, "1", 50, 4, 0, 0),
      ETFDataPlus(startDatePlus12monthsMinus1Day, eTFC, "1", 45, 6, 0, 0),
      ETFDataPlus(startDatePlus12monthsMinus1Day, eTFD, "1", 65, 8, 0, 0),
      ETFDataPlus(startDatePlus12months, eTFA, "1", 35, 0, 0, 0),
      ETFDataPlus(startDatePlus12months, eTFB, "1", 50, 0, 0, 0),
      ETFDataPlus(startDatePlus12months, eTFC, "1", 45, 0, 0, 0),
      ETFDataPlus(startDatePlus12months, eTFD, "1", 65, 0, 0, 0),
      ETFDataPlus(startDatePlus12monthsPlus1Day, eTFA, "1", 535, 0, 0, 0),
      ETFDataPlus(startDatePlus12monthsPlus1Day, eTFB, "1", 550, 0, 0, 0),
      ETFDataPlus(startDatePlus12monthsPlus1Day, eTFC, "1", 545, 0, 0, 0),
      ETFDataPlus(startDatePlus12monthsPlus1Day, eTFD, "1", 565, 0, 0, 0)
    )

    val investmentInputDataSemiAnnually = investmentInputDataQuarterly ++
      Seq(
        ETFDataPlus(startDatePlus18months, eTFA, "1", 40, 0, 0, 0),
        ETFDataPlus(startDatePlus18months, eTFB, "1", 60, 0, 0, 0),
        ETFDataPlus(startDatePlus18months, eTFC, "1", 80, 0, 0, 0),
        ETFDataPlus(startDatePlus18months, eTFD, "1", 100, 0, 0, 0),
        ETFDataPlus(startDatePlus24months, eTFA, "1", 150, 0, 0, 0),
        ETFDataPlus(startDatePlus24months, eTFB, "1", 120, 0, 0, 0),
        ETFDataPlus(startDatePlus24months, eTFC, "1", 90, 0, 0, 0),
        ETFDataPlus(startDatePlus24months, eTFD, "1", 60, 0, 0, 0),
        ETFDataPlus(startDatePlus24monthsMinus1Day, eTFA, "1", 85, 0, 0, 0),
        ETFDataPlus(startDatePlus24monthsMinus1Day, eTFB, "1", 125, 0, 0, 0),
        ETFDataPlus(startDatePlus24monthsMinus1Day, eTFC, "1", 165, 0, 0, 0),
        ETFDataPlus(startDatePlus24monthsMinus1Day, eTFD, "1", 205, 0, 0, 0),
        ETFDataPlus(startDatePlus30months, eTFA, "1", 110, 0, 0, 0),
        ETFDataPlus(startDatePlus30months, eTFB, "1", 90, 0, 0, 0),
        ETFDataPlus(startDatePlus30months, eTFC, "1", 20, 0, 0, 0),
        ETFDataPlus(startDatePlus30months, eTFD, "1", 140, 0, 0, 0),
        ETFDataPlus(startDatePlus36months, eTFA, "1", 75, 0, 0, 0),
        ETFDataPlus(startDatePlus36months, eTFB, "1", 115, 0, 0, 0),
        ETFDataPlus(startDatePlus36months, eTFC, "1", 155, 1.75, 0, 0),
        ETFDataPlus(startDatePlus36months, eTFD, "1", 195, 0, 0, 0),
        ETFDataPlus(startDatePlus36monthsMinus1Day, eTFA, "1", 80, 0, 0, 0),
        ETFDataPlus(startDatePlus36monthsMinus1Day, eTFB, "1", 120, 0, 0, 0),
        ETFDataPlus(startDatePlus36monthsMinus1Day, eTFC, "1", 160, 1.75, 0, 0),
        ETFDataPlus(startDatePlus36monthsMinus1Day, eTFD, "1", 200, 0, 0, 0)
      )
  }
}
