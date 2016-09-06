package com.eigenroute.portfolioanalysis

import org.scalatest.TryValues._
import org.scalatest.{FlatSpec, ShouldMatchers}

class InitialAllocationCalculatorUTest extends FlatSpec with ShouldMatchers {

  trait Fixture {
    val eTFA = ETFCode("AAA")
    val eTFB = ETFCode("BBB")
    val eTFC = ETFCode("CCC")
    val eTFD = ETFCode("DDD")
    val eTFSelectionA = ETFSelection(eTFA, 0.2)
    val eTFSelectionB = ETFSelection(eTFB, 0.05)
    val eTFSelectionC = ETFSelection(eTFC, 0.3)
    val eTFSelectionD = ETFSelection(eTFD, 0.45)
    val portfolioDesign = PortfolioDesign(Seq(eTFSelectionA, eTFSelectionB, eTFSelectionC, eTFSelectionD))
    val eTFDataA = ETFDataPlus(null, eTFA, "1", 9d, 0, 0)
    val eTFDataB = ETFDataPlus(null, eTFB, "2", 7d, 0, 0)
    val eTFDataC = ETFDataPlus(null, eTFC, "3", 21d, 0, 0)
    val eTFDataD = ETFDataPlus(null, eTFD, "4", 3d, 0, 0)
    val portfolioSnapshot = PortfolioSnapshot(Seq(eTFDataA, eTFDataB, eTFDataC, eTFDataD))
    val insufficientPortfolioSnapshot = PortfolioSnapshot(Seq(eTFDataA, eTFDataB, eTFDataD))

    val initialInvestment = 10000d
    val tradingCostPerETF = 10d
    val bidAskCostFractionOfNav = 0.001
  }

  "calculating the allocation" should "fail if a matching code from the portfolio design is not present in the portfolio " +
  "snapshot" in new Fixture {
    InitialAllocationCalculator
    .calculateAllocation(portfolioDesign, insufficientPortfolioSnapshot, initialInvestment, tradingCostPerETF,
      bidAskCostFractionOfNav)
    .failure.exception.getMessage shouldBe "No data for one of the codes"
  }

  it should "calculate the correct number of shares of each ETF to purchase" in new Fixture {
    val roundingScale = 8
    val roundingMode = BigDecimal.RoundingMode.HALF_UP
    val remainingFundsAfterTradingCosts = initialInvestment - tradingCostPerETF*portfolioDesign.eTFSelections.size
    val expected = portfolioDesign.eTFSelections.map { eTFSelection =>
      val price =
        portfolioSnapshot.eTFDatas.find(_.eTFCode == eTFSelection.eTFCode).map(_.nAV * (1 + bidAskCostFractionOfNav) ).get
      val roundedQuantity =
        BigDecimal(remainingFundsAfterTradingCosts * eTFSelection.desiredWeight / price)
        .setScale(roundingScale, roundingMode)
      PortfolioAllocation(eTFSelection.eTFCode, roundedQuantity)
    }

    InitialAllocationCalculator
    .calculateAllocation(portfolioDesign, portfolioSnapshot, initialInvestment, tradingCostPerETF, bidAskCostFractionOfNav)
      .success.value.map( allocation =>
        PortfolioAllocation(
          allocation.eTFCode, allocation.quantity.setScale(roundingScale, roundingMode))) should
          contain theSameElementsAs expected

 }

}
