package com.eigenroute.portfolioanalysis

import java.sql.Timestamp

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, ShouldMatchers}

class PortfolioRebalancerUTest extends FlatSpec with ShouldMatchers {

  val now = new Timestamp(new DateTime(2016, 1, 1, 1, 1, 1).getMillis)

  val eTFA = ETFCode("AAA")
  val eTFB = ETFCode("BBB")
  val eTFC = ETFCode("CCC")
  val eTFD = ETFCode("DDD")
  val eTFASelection = ETFSelection(eTFA, 0.25)
  val eTFBSelection = ETFSelection(eTFB, 0.5)
  val eTFCSelection = ETFSelection(eTFC, 0.1)
  val eTFDSelection = ETFSelection(eTFD, 0.15)
  val portfolioDesign = PortfolioDesign(Seq(eTFASelection, eTFBSelection, eTFCSelection, eTFDSelection))

  val eTFDataPlusA = ETFDataPlus(now, eTFA, "", 20, 0, 50, 0d)
  val eTFDataPlusB = ETFDataPlus(now, eTFB, "", 30, 0, 100, 0d)
  val eTFDataPlusC = ETFDataPlus(now, eTFC, "", 40, 0, 100, 0d)
  val eTFDataPlusD = ETFDataPlus(now, eTFD, "", 50, 0, 40, 0d)
  val portfolioSnapshot = PortfolioSnapshot(Seq(eTFDataPlusA, eTFDataPlusB, eTFDataPlusC, eTFDataPlusD))

  val weightDifferences = Seq(
     PortfolioWeightDifference(eTFA, 0.15),
     PortfolioWeightDifference(eTFB, 0.2),
     PortfolioWeightDifference(eTFC, -0.3),
     PortfolioWeightDifference(eTFD, -0.05)
  )

  val pm = new PortfolioRebalancer

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
  }

  "The weight difference calculator" should "accurately calculate the weight difference" in {
    pm.weightDifference(portfolioDesign, portfolioSnapshot).map { portfolioWeightDifference =>
      PortfolioWeightDifference(portfolioWeightDifference.eTFCode,
        BigDecimal(portfolioWeightDifference.weightDifference).setScale(5, BigDecimal.RoundingMode.HALF_UP).toDouble)
    } should contain theSameElementsAs weightDifferences
  }

  "The new desired value calculator" should "calculate the new desired value when the max dev is low but not " +
  "zero" in new DesiredValueFixture {
    checkNewDesiredValue(0.05, expectedDesiredValuesOneToBeTraded, 10d, 20d, 20d)
  }

  it should "calculate the new desired value when the max dev is zero" in new DesiredValueFixture {
    checkNewDesiredValue(0d, expectedDesiredValuesAllToBeTraded, 10d, 0d, 40d)
    checkNewDesiredValue(0d, expectedDesiredValuesAllToBeTradedcost15ExDivCash100, 15d, 60d, 40d)
  }

  it should "calculate the new desired value when the max dev is one" in new DesiredValueFixture {
    checkNewDesiredValue(1d, expectedDesiredValuesNoTrades, 10d, 20d, 20d)
  }

  def checkNewDesiredValue(
    maxAllowedDeviation: Double,
    expected: Seq[ETFDesiredValue],
    perETFTradingCost: Double,
    accExDiv: Double,
    accCash: Double): Unit = {
    pm.newDesiredValue(
        portfolioDesign, weightDifferences, portfolioSnapshot, maxAllowedDeviation, perETFTradingCost, accExDiv, accCash
      ).map { dV =>
        ETFDesiredValue(dV.eTFCode, BigDecimal(dV.value).setScale(5, BigDecimal.RoundingMode.HALF_UP).toDouble, dV.isToTrade)
    } should contain theSameElementsAs expected
  }

  "The value difference calculator" should "calculate the difference in value as non-zero for ETFs to be traded, and zero " +
  "for ETFs not to be traded, and should get the signs correct" in new DesiredValueFixture {

    pm.valueDifference(expectedDesiredValuesOneToBeTraded, portfolioSnapshot).map { vDiff =>
      PortfolioValueDifference(vDiff.eTFCode,
        BigDecimal(vDiff.valueDifference).setScale(5, BigDecimal.RoundingMode.HALF_UP).toDouble)
    } should contain theSameElementsAs expectedValueDifferenceOneTrade

    pm.valueDifference(expectedDesiredValuesNoTrades, portfolioSnapshot) should
      contain theSameElementsAs expectedValueDifferenceNoTrades

    pm.valueDifference(expectedDesiredValuesAllToBeTraded, portfolioSnapshot) should
      contain theSameElementsAs expectedValueDifferenceAllTrades
  }

  "The portfolio value calculator" should "calculate the value of the portfolio using the nav and quantity from the " +
  "snapshot" in {

    pm.portfolioValue(portfolioSnapshot) shouldEqual 10000d
    pm.portfolioValueFromETFDatas(portfolioSnapshot.eTFDatas) shouldEqual 10000d

  }
}
