package com.eigenroute.portfolioanalysis.rebalancing

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, ShouldMatchers}

class PortfolioRebalancerUTest extends FlatSpec with ShouldMatchers with PortfolioFixture with MockFactory {

  trait Fixture {
    val mockFirstEstimatesCalculator = mock[FirstEstimateQuantitiesToAcquireCalculator]
    val pr = new PortfolioRebalancer(mockFirstEstimatesCalculator)
  }

  "The max quantities generator" should "calculate the maximum quantity of each ETF that can be purchased with the" +
  "remaining cash" in new EstimatedQuantitiesToAcquire with AdditionalQuantitiesFixture with Fixture {
    val firstEstimateQuantitiesAllTradesWithNonMatch = Seq(
      PortfolioQuantityToAcquire(eTFNotInSnapshot, 74, round(20 * (1 + 0.0011)), 74.91759),
      PortfolioQuantityToAcquire(eTFB, 66, round(30 * (1 + 0.0011)), 66.59341),
      PortfolioQuantityToAcquire(eTFC, -76, round(40 / (1 + 0.0011)), -75.0825),
      PortfolioQuantityToAcquire(eTFD, -11, round(50 / (1 + 0.0011)), -10.011)
    )
    val expectedOneNotMatched = Seq(AddnlQty(eTFNotInSnapshot, 0), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))

    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshot, 0.0011, 0d, 10d, 0d, 0d)
    .returning(expectedFirstEstimateQuantitiesAllTrades)
    pr.maxQuantities(portfolioDesign, portfolioSnapshot, 0.0011, 0d, 10d, 0d, 0d) should
      contain theSameElementsAs expectedAdditionalQuantitiesAllMatch

    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshot, 0.0011, 0d, 10d, 0d, 0d)
    .returning(firstEstimateQuantitiesAllTradesWithNonMatch)
    pr.maxQuantities(portfolioDesign, portfolioSnapshot, 0.0011, 0d, 10d, 0d, 0d) should
      contain theSameElementsAs expectedOneNotMatched
  }

  it should "calculate the correct max quantities for the first trades" in new EstimatedQuantitiesToAcquire with Fixture
  with AdditionalQuantitiesFixture {
    (mockFirstEstimatesCalculator.firstEstimateQuantitiesToAcquire _)
    .expects(portfolioDesign, portfolioSnapshotZeroQuantity, 0.0011, 0d, 10d, 0d, 10000d)
    .returning(expectedFirstEstimateQuantitiesFirstTrades)
    pr.maxQuantities(portfolioDesign, portfolioSnapshotZeroQuantity, 0.0011, 0d, 10d, 0d, 10000d) should
      contain theSameElementsAs expectedAdditionalQuantitiesFirstTrades
  }

  "The additional quantities to acquire generator" should "generate and exhaustive list of possible additional " +
  "quantities" in new AdditionalQuantitiesFixture with Fixture {

    val allMatchReverse = Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 2), AddnlQty(eTFA, 3))
    val expectedReverse = Seq(
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 2), AddnlQty(eTFA, 3)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 1), AddnlQty(eTFA, 3)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 0), AddnlQty(eTFA, 3)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 2), AddnlQty(eTFA, 2)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 1), AddnlQty(eTFA, 2)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 0), AddnlQty(eTFA, 2)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 2), AddnlQty(eTFA, 1)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 1), AddnlQty(eTFA, 1)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 0), AddnlQty(eTFA, 1)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 2), AddnlQty(eTFA, 0)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 1), AddnlQty(eTFA, 0)),
      Seq(AddnlQty(eTFD, 0), AddnlQty(eTFC, 0), AddnlQty(eTFB, 0), AddnlQty(eTFA, 0))
    )

    pr.additionalQuantities(expectedAdditionalQuantitiesAllMatch) should
      contain theSameElementsAs expectedAdditionalQuantitiesFull
    pr.additionalQuantities(allMatchReverse) should contain theSameElementsAs expectedReverse
  }

  "The max absolute deviation from desired weights calculator" should "return the maximum deviation from the desired " +
  "weights of the given quantities to purchase" in new AdditionalQuantitiesFixture with Fixture {

    round(pr.maxAbsDeviation(
      portfolioDesign,
      portfolioSnapshot,
      PortfolioQuantitiesToAcquire(
        expectedFinalQuantitiesToAcquireAllTrades))) shouldEqual 0.00485

    round(pr.maxAbsDeviation(
      portfolioDesign,
      portfolioSnapshotZeroQuantity,
      PortfolioQuantitiesToAcquire(
        expectedFinalQuantitiesToAcquireAllTrades))) shouldEqual 1

  }

  "The final quantities chooser" should "choose the additional quantity combination that results in the least, " +
  "non-negative cash remaining and within that set the least deviation from the desired weights" in new Fixture
  with EstimatedQuantitiesToAcquire with AdditionalQuantitiesFixture {

    val expectedChosenAllTrades =
      FinalPortfolioQuantitiesToHave(expectedFinalQuantitiesAllTrades, 2.11734, 0.00485, additionalQuantitiesChosenAllTrades)

    val expectedChosenOneTrade =
      FinalPortfolioQuantitiesToHave(expectedFinalQuantitiesOneTrade, 3.23872, 0.05020, additionalQuantitiesChosenOneTrade)

    val actualChosenAllTrades = pr.finalQuantities(
      portfolioDesign,
      portfolioSnapshot,
      PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesAllTrades),
      Seq(additionalQuantitiesChosenAllTrades), 0d, 0d)

    val actualChosenOneTrade = pr.finalQuantities(
      portfolioDesign,
      portfolioSnapshot,
      PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesOneTrade),
      Seq(additionalQuantitiesChosenOneTrade), 0d, 0d)

    actualChosenAllTrades.copy(
      cashRemaining = round(actualChosenAllTrades.cashRemaining, 2),
      maxActualDeviation = round(actualChosenAllTrades.maxActualDeviation)) shouldEqual
      expectedChosenAllTrades.copy(cashRemaining = round(expectedChosenAllTrades.cashRemaining, 2))

    actualChosenOneTrade.copy(
      cashRemaining = round(actualChosenOneTrade.cashRemaining, 2),
      maxActualDeviation = round(actualChosenOneTrade.maxActualDeviation)) shouldEqual
      expectedChosenOneTrade.copy(cashRemaining = round(expectedChosenOneTrade.cashRemaining, 2))

    pr.finalQuantities(
      portfolioDesign,
      portfolioSnapshot,
      PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesOneTrade),
      Seq(), 0d, 0d) shouldEqual FinalPortfolioQuantitiesToHave(portfolioSnapshot.sameDateUniqueCodesETFDatas.map { eTFData =>
          FinalPortfolioQuantityToHave(eTFData.eTFCode, eTFData.quantity.toInt)
        }, pr.cashRemaining(PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesOneTrade).quantitiesToAcquire),
          0, Seq())

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

    val additionalQuantities = pr.additionalQuantities(expectedAdditionalQuantitiesFirstTrades)
    val notRounded = pr.finalQuantities(
      portfolioDesign,
      portfolioSnapshotZeroQuantity,
      PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesFirstTrades),
      additionalQuantities, 0d, 10000d)
    notRounded.copy(
      cashRemaining = round(notRounded.cashRemaining),
      maxActualDeviation = round(notRounded.maxActualDeviation)) shouldEqual
        FinalPortfolioQuantitiesToHave(expectedFinalQuantitiesToHave, 9.0220, 0.00100, expectedAdditionalQuantities)

  }

  "The cash remaining calculator" should "correctly calculate the cash remaining after trades" in
  new EstimatedQuantitiesToAcquire with Fixture {
    val expected = 53.29385
    round(pr.
      cashRemaining(
        PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesOneTrade).quantitiesToAcquire)) shouldEqual expected
  }

}
