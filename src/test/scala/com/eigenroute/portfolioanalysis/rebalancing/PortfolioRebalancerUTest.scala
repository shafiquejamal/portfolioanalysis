package com.eigenroute.portfolioanalysis.rebalancing

import com.eigenroute.portfolioanalysis.PortfolioFixture
import org.scalatest.{FlatSpec, ShouldMatchers}

class PortfolioRebalancerUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  val pr = new PortfolioRebalancer

  "The max quantities generator" should "calculate the maximum quantity of each ETF that can be purchased with the" +
  "remaining cash" in new EstimatedQuantitiesToAcquire with AdditionalQuantitiesFixture {
    val firstEstimateQuantitiesAllTradesWithNonMatch = Seq(
      PortfolioQuantityToAcquire(eTFNotInSnapshot, 74, round(20 * (1 + 0.0011)), 74.91759),
      PortfolioQuantityToAcquire(eTFB, 66, round(30 * (1 + 0.0011)), 66.59341),
      PortfolioQuantityToAcquire(eTFC, -76, round(40 / (1 + 0.0011)), -75.0825),
      PortfolioQuantityToAcquire(eTFD, -11, round(50 / (1 + 0.0011)), -10.011)
    )
    val expectedOneNotMatched = Seq(AddnlQty(eTFNotInSnapshot, 0), AddnlQty(eTFB, 4), AddnlQty(eTFC, 0), AddnlQty(eTFD, 0))

    pr.maxQuantitiesGenerator(expectedFirstEstimateQuantitiesAllTrades, portfolioSnapshot, 0d, 2.11734) should
      contain theSameElementsAs expectedAdditionalQuantitiesAllMatch
    pr.maxQuantitiesGenerator(expectedFirstEstimateQuantitiesAllTrades, portfolioSnapshot, 2.11734, 0d) should
      contain theSameElementsAs expectedAdditionalQuantitiesAllMatch
    pr.maxQuantitiesGenerator(firstEstimateQuantitiesAllTradesWithNonMatch, portfolioSnapshot, 0d, 2.11734) should
      contain theSameElementsAs expectedOneNotMatched
  }

  it should "calculate the correct max quantities for the first trades" in new EstimatedQuantitiesToAcquire
  with AdditionalQuantitiesFixture {
    pr.maxQuantitiesGenerator(
      expectedFirstEstimateQuantitiesFirstTrades, portfolioSnapshotZeroQuantity, 0d, 10000d) should
      contain theSameElementsAs expectedAdditionalQuantitiesFirstTrades
  }

  "The additional quantities to acquire generator" should "generate and exhaustive list of possible additional " +
  "quantities" in new AdditionalQuantitiesFixture {

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

    pr.additionalQuanititiesGenerator(expectedAdditionalQuantitiesAllMatch) should
      contain theSameElementsAs expectedAdditionalQuantitiesFull
    pr.additionalQuanititiesGenerator(allMatchReverse) should contain theSameElementsAs expectedReverse
  }

  "The max absolute deviation from desired weights calculator" should "return the maximum deviation from the desired " +
  "weights of the given quantities to purchase" in new AdditionalQuantitiesFixture {

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
  "non-negative cash remaining and within that set the least deviation from the desired weights" in
  new EstimatedQuantitiesToAcquire with AdditionalQuantitiesFixture {

    val expectedChosenAllTrades =
      FinalPortfolioQuantitiesToHave(expectedFinalQuantitiesAllTrades, 2.11734, 0.00485, additionalQuantitiesChosenAllTrades)

    val expectedChosenOneTrade =
      FinalPortfolioQuantitiesToHave(expectedFinalQuantitiesOneTrade, 3.23872, 0.05020, additionalQuantitiesChosenOneTrade)

    val actualChosenAllTrades = pr.finalQuantitiesChooser(
      portfolioDesign,
      portfolioSnapshot,
      PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesAllTrades),
      Seq(additionalQuantitiesChosenAllTrades))

    val actualChosenOneTrade = pr.finalQuantitiesChooser(
      portfolioDesign,
      portfolioSnapshot,
      PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesOneTrade),
      Seq(additionalQuantitiesChosenOneTrade))

    actualChosenAllTrades.copy(
      cashRemaining = round(actualChosenAllTrades.cashRemaining, 2),
      maxActualDeviation = round(actualChosenAllTrades.maxActualDeviation)) shouldEqual
      expectedChosenAllTrades.copy(cashRemaining = round(expectedChosenAllTrades.cashRemaining, 2))

    actualChosenOneTrade.copy(
      cashRemaining = round(actualChosenOneTrade.cashRemaining, 2),
      maxActualDeviation = round(actualChosenOneTrade.maxActualDeviation)) shouldEqual
      expectedChosenOneTrade.copy(cashRemaining = round(expectedChosenOneTrade.cashRemaining, 2))

    pr.finalQuantitiesChooser(
      portfolioDesign,
      portfolioSnapshot,
      PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesOneTrade),
      Seq()) shouldEqual FinalPortfolioQuantitiesToHave(portfolioSnapshot.eTFDatas.map { eTFData =>
          FinalPortfolioQuantityToHave(eTFData.eTFCode, eTFData.quantity.toInt)
        }, pr.cashRemaining(PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesOneTrade).quantitiesToAcquire),
          0, Seq())

  }

  "The cash remaining calculator" should "correctly calculate the cash remaining after trades" in
  new EstimatedQuantitiesToAcquire {
    val expected = 53.29385
    round(pr.
      cashRemaining(
        PortfolioQuantitiesToAcquire(expectedFirstEstimateQuantitiesOneTrade).quantitiesToAcquire)) shouldEqual expected
  }

}
