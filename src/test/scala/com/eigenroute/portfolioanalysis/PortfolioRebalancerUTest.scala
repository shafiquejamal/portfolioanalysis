package com.eigenroute.portfolioanalysis

import java.sql.Timestamp

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, ShouldMatchers}

class PortfolioRebalancerUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  val pr = new PortfolioRebalancer

  "The weight difference calculator" should "accurately calculate the weight difference" in {
    pr.weightDifferences(portfolioDesign, portfolioSnapshot).map { portfolioWeightDifference =>
      PortfolioWeightDifference(portfolioWeightDifference.eTFCode, round(portfolioWeightDifference.weightDifference))
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

  private def checkNewDesiredValue(
    maxAllowedDeviation: Double,
    expected: Seq[ETFDesiredValue],
    perETFTradingCost: Double,
    accExDiv: Double,
    accCash: Double): Unit = {
    pr.newDesiredValues(
        portfolioDesign, weightDifferences, portfolioSnapshot, maxAllowedDeviation, perETFTradingCost, accExDiv, accCash)
        .map { dV => ETFDesiredValue(dV.eTFCode, round(dV.value), dV.isToTrade)
    } should contain theSameElementsAs expected
  }

  "The value difference calculator" should "calculate the difference in value as non-zero for ETFs to be traded, and zero " +
  "for ETFs not to be traded, and should get the signs correct" in new DesiredValueFixture {

    pr.valueDifferences(expectedDesiredValuesOneToBeTraded, portfolioSnapshot).map { vDiff =>
      PortfolioValueDifference(vDiff.eTFCode, round(vDiff.valueDifference))
    } should contain theSameElementsAs expectedValueDifferenceOneTrade

    pr.valueDifferences(expectedDesiredValuesNoTrades, portfolioSnapshot) should
      contain theSameElementsAs expectedValueDifferenceNoTrades

    pr.valueDifferences(expectedDesiredValuesAllToBeTraded, portfolioSnapshot) should
      contain theSameElementsAs expectedValueDifferenceAllTrades
  }

  "The portfolio value calculator" should "calculate the value of the portfolio using the nav and quantity from the " +
  "snapshot" in {

    pr.portfolioValue(portfolioSnapshot) shouldEqual 10000d
    pr.portfolioValueFromETFDatas(portfolioSnapshot.eTFDatas) shouldEqual 10000d

  }

  "The quantity difference calculator" should "calculate the new quantities to be purchased such that as much free cash as" +
  "possible is invested, with the minimum deviation from the desired weights" in new DesiredValueFixture {

    val expectedFirstEstimateQuantitiesAllTrades = Seq(
      PorfolioQuanitiesToAcquire(eTFA, 74, round(20*(1 + 0.0011)), 74.91759),
      PorfolioQuanitiesToAcquire(eTFB, 66, round(30*(1 + 0.0011)), 66.59341),
      PorfolioQuanitiesToAcquire(eTFC, -76, round(40/(1 + 0.0011)), -75.0825),
      PorfolioQuanitiesToAcquire(eTFD, -11, round(50/(1 + 0.0011)), -10.011)
    )

    val expectedFirstEstimateQuantitiesAllTradesExpensive = Seq(
      PorfolioQuanitiesToAcquire(eTFA, 74, round(20*(1 + 0.0025)), 74.81297),
      PorfolioQuanitiesToAcquire(eTFB, 66, round(30*(1 + 0.0025)), 66.50042),
      PorfolioQuanitiesToAcquire(eTFC, -76, round(40/(1 + 0.0025)), -75.1875),
      PorfolioQuanitiesToAcquire(eTFD, -11, round(50/(1 + 0.0025)), -10.025)
    )

    val expectedFirstEstimateQuantitiesOneTrade = Seq(
      PorfolioQuanitiesToAcquire(eTFA, 67, round(20*(1 + 0.0011)), 67.71963),
      PorfolioQuanitiesToAcquire(eTFB, 56, round(30*(1 + 0.0011)), 56.99613),
      PorfolioQuanitiesToAcquire(eTFC, -77, round(40/(1 + 0.0011)), -76.52526),
      PorfolioQuanitiesToAcquire(eTFD, 0, round(50/(1 + 0.0011)), 0)
    )

    val expectedFirstEstimateQuantitiesNoTrades = Seq(
      PorfolioQuanitiesToAcquire(eTFA, 0, round(20/(1 + 0.0011)), 0),
      PorfolioQuanitiesToAcquire(eTFB, 0, round(30/(1 + 0.0011)), 0),
      PorfolioQuanitiesToAcquire(eTFC, 0, round(40/(1 + 0.0011)), 0),
      PorfolioQuanitiesToAcquire(eTFD, 0, round(50/(1 + 0.0011)), 0)
    )

    checkFirstEstimateQuantitiesToAcquire(expectedValueDifferenceAllTrades, expectedFirstEstimateQuantitiesAllTrades)
    checkFirstEstimateQuantitiesToAcquire(expectedValueDifferenceAllTrades, expectedFirstEstimateQuantitiesAllTradesExpensive, 0.0025)
    checkFirstEstimateQuantitiesToAcquire(expectedValueDifferenceOneTrade, expectedFirstEstimateQuantitiesOneTrade)
    checkFirstEstimateQuantitiesToAcquire(expectedValueDifferenceNoTrades, expectedFirstEstimateQuantitiesNoTrades)

  }

  private def checkFirstEstimateQuantitiesToAcquire(
      expectedValueDifference: Seq[PortfolioValueDifference],
      expectedFirstEstimateQuantities: Seq[PorfolioQuanitiesToAcquire],
      bidAskCost: Double = 0.0011) {

    pr.firstEstimateQuantitiesToAcquire(portfolioDesign, portfolioSnapshot, expectedValueDifference, bidAskCost)
    .map { est =>
      PorfolioQuanitiesToAcquire(
        est.eTFCode, est.quantityToAcquire, round(est.effectivePrice), round(est.fractionalQuantity) ) } should
    contain theSameElementsAs expectedFirstEstimateQuantities

  }

  def round(double: Double): Double = BigDecimal(double).setScale(5, BigDecimal.RoundingMode.HALF_UP).toDouble
}
