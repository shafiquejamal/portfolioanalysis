package com.eigenroute.portfolioanalysis

import java.sql.Date

import com.eigenroute.portfolioanalysis.investment.InvestmentPeriod
import com.eigenroute.portfolioanalysis.rebalancing._
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

  def round(double: Double, n: Int = 5): Double = BigDecimal(double).setScale(n, BigDecimal.RoundingMode.HALF_UP).toDouble

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

    val expectedValueDifferenceOneTrade = Seq(
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

    val expectedFirstEstimateQuantitiesOneTrade = Seq(
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
      Seq(AddnlQty(eTFA, 3), AddnlQty(eTFB, 2), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))

    val additionalQuantitiesChosenOneTrade =
      Seq(AddnlQty(eTFA, 1), AddnlQty(eTFB, 1), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))

    val expectedFinalQuantitiesToAcquireAllTrades = Seq(
      PortfolioQuantityToAcquire(eTFA, 77, round(20 * (1 + 0.0011)), 74.91759),
      PortfolioQuantityToAcquire(eTFB, 68, round(30 * (1 + 0.0011)), 66.59341),
      PortfolioQuantityToAcquire(eTFC, -76, round(40 / (1 + 0.0011)), -75.0825),
      PortfolioQuantityToAcquire(eTFD, -11, round(50 / (1 + 0.0011)), -10.011)
    )

    val expectedFinalQuantitiesAllTrades = Seq(
      FinalPortfolioQuantityToHave(eTFA, 127),
      FinalPortfolioQuantityToHave(eTFB, 168),
      FinalPortfolioQuantityToHave(eTFC, 24),
      FinalPortfolioQuantityToHave(eTFD, 29)
    )

    val expectedFinalQuantitiesOneTrade = Seq(
      FinalPortfolioQuantityToHave(eTFA, 118),
      FinalPortfolioQuantityToHave(eTFB, 157),
      FinalPortfolioQuantityToHave(eTFC, 23),
      FinalPortfolioQuantityToHave(eTFD, 40)
    )
  }

  trait InvestmentFixture {
    val startDate = new DateTime(2010, 1, 1, 0, 0, 0)
    val investmentPeriod = InvestmentPeriod(startDate, startDate.plusYears(3))
  }

}
