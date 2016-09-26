package com.eigenroute.portfolioanalysis.rebalancing

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, ShouldMatchers}

class PortfolioRebalancerUTest extends FlatSpec with ShouldMatchers with PortfolioFixture with MockFactory {

  trait Fixture {
    val mockFirstEstimatesCalculator = mock[FirstEstimateQuantitiesToAcquireCalculator]
  }

  "The max quantities generator" should "calculate the maximum quantity of each ETF that can be purchased with the " +
  "remaining cash" in new EstimatedQuantitiesToAcquire with AdditionalQuantitiesFixture with Fixture {

    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshot, BigDecimal(0.0011), BigDecimal(0), BigDecimal(10), BigDecimal(0), BigDecimal(0))
    .returning(expectedFirstEstimateQuantitiesAllTrades)
    val prAllMatch =
      new PortfolioRebalancer(portfolioDesign, portfolioSnapshot, 0.0011, 0d, 10d, 0d, 0d, mockFirstEstimatesCalculator)

    val firstEstimateQuantitiesAllTradesWithNonMatch = Seq(
      PortfolioQuantityToAcquire(eTFNotInSnapshot, 74, round(20 * (1 + 0.0011)), 74.91759),
      PortfolioQuantityToAcquire(eTFB, 66, round(30 * (1 + 0.0011)), 66.59341),
      PortfolioQuantityToAcquire(eTFC, -76, round(40 / (1 + 0.0011)), -75.0825),
      PortfolioQuantityToAcquire(eTFD, -11, round(50 / (1 + 0.0011)), -10.011)
    )
    val expectedOneNotMatched = Seq(AddnlQty(eTFNotInSnapshot, 0), AddnlQty(eTFB, 3), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))

    prAllMatch.maxQuantities should contain theSameElementsAs expectedAdditionalQuantitiesAllMatch

    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshot, BigDecimal(0.0011), BigDecimal(0), BigDecimal(10), BigDecimal(0), BigDecimal(0))
    .returning(firstEstimateQuantitiesAllTradesWithNonMatch)

    val prOneNotMatched =
      new PortfolioRebalancer(portfolioDesign, portfolioSnapshot, 0.0011, 0d, 10d, 0d, 0d, mockFirstEstimatesCalculator)
    prOneNotMatched.maxQuantities should contain theSameElementsAs expectedOneNotMatched
  }

  it should "calculate the correct max quantities for the first trades" in new EstimatedQuantitiesToAcquire with Fixture
  with AdditionalQuantitiesFixture {
    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshotZeroQuantity, BigDecimal(0.0011), BigDecimal(0), BigDecimal(10), BigDecimal(0), BigDecimal(10000))
    .returning(expectedFirstEstimateQuantitiesFirstTrades)
    val pr =
      new PortfolioRebalancer(portfolioDesign, portfolioSnapshotZeroQuantity, 0.0011, 0d, 10d, 0d, 10000d,
        mockFirstEstimatesCalculator)

    pr.maxQuantities should contain theSameElementsAs expectedAdditionalQuantitiesFirstTrades
  }

  "The additional quantities to acquire generator" should "generate and exhaustive list of possible additional " +
  "quantities" in new AdditionalQuantitiesFixture with Fixture with EstimatedQuantitiesToAcquire {

    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshot, BigDecimal(0.0011), BigDecimal(0), BigDecimal(10), BigDecimal(0), BigDecimal(0))
    .returning(expectedFirstEstimateQuantitiesAllTrades)
    val pr =
      new PortfolioRebalancer(portfolioDesign, portfolioSnapshot, 0.0011, 0d, 10d, 0d, 0d,
                              mockFirstEstimatesCalculator)
    pr.additionalQuantities should contain theSameElementsAs expectedAdditionalQuantitiesFull
  }

  "The max absolute deviation from desired weights calculator" should "return the maximum deviation from the desired " +
  "weights of the given quantities to purchase" in new AdditionalQuantitiesFixture with Fixture
  with EstimatedQuantitiesToAcquire{

    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshot, BigDecimal(0.0011), BigDecimal(0), BigDecimal(10), BigDecimal(0), BigDecimal(0))
    .returning(expectedFirstEstimateQuantitiesAllTrades)
    val pr =
      new PortfolioRebalancer(portfolioDesign, portfolioSnapshot, 0.0011, 0d, 10d, 0d, 0d, mockFirstEstimatesCalculator)

    round(pr.maxAbsDeviation(PortfolioQuantitiesToAcquire(expectedFinalQuantitiesToAcquireAllTrades))) shouldEqual 0.00485

    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshotZeroQuantity, BigDecimal(0.0011), BigDecimal(0), BigDecimal(10), BigDecimal(0), BigDecimal(0))
    .returning(expectedFirstEstimateQuantitiesAllTrades)
    val prZeroQuantity =
      new PortfolioRebalancer(portfolioDesign, portfolioSnapshotZeroQuantity, 0.0011, 0d, 10d, 0d, 0d,
        mockFirstEstimatesCalculator)

    round(
      prZeroQuantity.maxAbsDeviation(PortfolioQuantitiesToAcquire(expectedFinalQuantitiesToAcquireAllTrades))) shouldEqual 1

  }

  "The final quantities chooser" should "choose the additional quantity combination that results in the least, " +
  "non-negative cash remaining and within that set the least deviation from the desired weights" in new Fixture
  with EstimatedQuantitiesToAcquire with AdditionalQuantitiesFixture {

    val expectedChosenAllTrades =
      FinalPortfolioQuantitiesToHave(expectedFinalQuantitiesAllTrades, 2.16133913, 0.00653,
        additionalQuantitiesChosenAllTrades)

    val expectedChosenOneTrade =
      FinalPortfolioQuantitiesToHave(expectedFinalQuantitiesOneNotTraded, 3.27172, 0.05080,
        additionalQuantitiesChosenOneNotTraded)

    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshot,  BigDecimal(0.0011), BigDecimal(0), BigDecimal(10), BigDecimal(0),
      BigDecimal(0))
    .returning(expectedFirstEstimateQuantitiesAllTrades)
    val prAllTrades =
      new PortfolioRebalancer(portfolioDesign, portfolioSnapshot, 0.0011, 0d, 10d, 0d, 0d, mockFirstEstimatesCalculator)

    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshot, BigDecimal(0.0011), BigDecimal(0.05), BigDecimal(10), BigDecimal(0),
      BigDecimal(0))
    .returning(expectedFirstEstimateQuantitiesOneNotTraded)
    val prOneTrade =
      new PortfolioRebalancer(portfolioDesign, portfolioSnapshot, 0.0011, 0.05, 10d, 0d, 0d, mockFirstEstimatesCalculator)

    val actualChosenAllTrades = prAllTrades.finalQuantities
    val actualChosenOneTrade = prOneTrade.finalQuantities

    actualChosenAllTrades.copy(
      cashRemaining = round(actualChosenAllTrades.cashRemaining, 2),
      maxActualDeviation = round(actualChosenAllTrades.maxActualDeviation)) shouldEqual
      expectedChosenAllTrades.copy(cashRemaining = round(expectedChosenAllTrades.cashRemaining, 2))

    actualChosenOneTrade.copy(
      cashRemaining = round(actualChosenOneTrade.cashRemaining, 2),
      maxActualDeviation = round(actualChosenOneTrade.maxActualDeviation)) shouldEqual
      expectedChosenOneTrade.copy(cashRemaining = round(expectedChosenOneTrade.cashRemaining, 2))

  }

  it should "choose the correct final quantities for the first trades" in new EstimatedQuantitiesToAcquire with Fixture
  with AdditionalQuantitiesFixture {

    val expectedFinalQuantitiesToHave = Seq(
      FinalPortfolioQuantityToHave(eTFA, 125),
      FinalPortfolioQuantityToHave(eTFB, 166),
      FinalPortfolioQuantityToHave(eTFC, 25),
      FinalPortfolioQuantityToHave(eTFD, 30)
    )

    val expectedAdditionalQuantities = Seq(
      AddnlQty(eTFA, 1),
      AddnlQty(eTFB, 0),
      AddnlQty(eTFC, 1),
      AddnlQty(eTFD, 1)
    )

    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshotZeroQuantity, BigDecimal(0.0011), BigDecimal(0), BigDecimal(10),
      BigDecimal(0), BigDecimal(10040))
    .returning(expectedFirstEstimateQuantitiesFirstTrades)

    val pr =
      new PortfolioRebalancer(portfolioDesign, portfolioSnapshotZeroQuantity, 0.0011, 0d, 10d, 0d, 10040d,
        mockFirstEstimatesCalculator)

    val additionalQuantities = pr.additionalQuantities
    val notRounded = pr.finalQuantities
    notRounded.copy(
      cashRemaining = round(notRounded.cashRemaining),
      maxActualDeviation = round(notRounded.maxActualDeviation)) shouldEqual
        FinalPortfolioQuantitiesToHave(expectedFinalQuantitiesToHave, 9.0220, 0.00100, expectedAdditionalQuantities)
  }

  "The cash remaining calculator" should "correctly calculate the cash remaining after trades" in
  new EstimatedQuantitiesToAcquire with Fixture {
    val expected = 53.29385
    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshot, BigDecimal(0.0011), BigDecimal(0), BigDecimal(10), BigDecimal(0),
      BigDecimal(0))
    .returning(expectedFirstEstimateQuantitiesAllTrades)
    val pr =
      new PortfolioRebalancer(portfolioDesign, portfolioSnapshot, 0.0011, 0d, 10d, 0d, 0d, mockFirstEstimatesCalculator)
    round(pr.
      cashRemaining(
        PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesOneNotTraded).quantitiesToAcquire)) shouldEqual expected
  }

}
