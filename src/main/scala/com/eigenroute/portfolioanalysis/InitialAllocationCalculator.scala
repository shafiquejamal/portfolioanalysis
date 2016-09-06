package com.eigenroute.portfolioanalysis

import scala.util.{Success, Try, Failure}

object InitialAllocationCalculator {

  def calculateAllocation(
    porfolioDesign: PortfolioDesign,
    portfolioSnapshot: PortfolioSnapshot,
    initialInvestment:BigDecimal,
    perTransTradingCost:BigDecimal,
    bidAskCostFractionOfNav:Double):Try[Seq[PortfolioAllocation]] = {

    val totalTradingCost = porfolioDesign.eTFSelections.size * perTransTradingCost
    val fundsRemainingForInvestment = initialInvestment - totalTradingCost

    val allocations = porfolioDesign.eTFSelections.flatMap { eTFSelection =>

      val idealValue = fundsRemainingForInvestment * eTFSelection.desiredWeight
      val maybeNav =
        portfolioSnapshot.eTFDatas.filter(_.eTFCode == eTFSelection.eTFCode).map(_.nAV).headOption
        .fold[Option[BigDecimal]](None) { nav =>
          Some(idealValue / (nav * (1 + bidAskCostFractionOfNav)) )
      }

      maybeNav.map(PortfolioAllocation(eTFSelection.eTFCode, _))

    }

    if (allocations.size == porfolioDesign.eTFSelections.size) {
      Success(allocations)
    } else {
      Failure(new RuntimeException("No data for one of the codes"))
    }

  }

}
