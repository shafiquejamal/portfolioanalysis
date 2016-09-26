package com.eigenroute.portfolioanalysis.rebalancing

class PortfolioRebalancer(
    portfolioDesign: PortfolioDesign,
    portfolioSnapshot: PortfolioSnapshot,
    bidAskCostFractionOfNAV: Double,
    maxAllowedDeviation: Double,
    perETFTradingCost: Double,
    accumulatedExDividends: Double,
    accumulatedCash: Double,
    firstEstimateQuantitiesToAcquireCalculator: FirstEstimateQuantitiesToAcquireCalculator =
      new FirstEstimateQuantitiesToAcquireCalculator)
  extends PortfolioValueCalculation {

  private val firstEstimateQuantitiesToAcquire =
    firstEstimateQuantitiesToAcquireCalculator
    .firstEstimateQuantitiesToAcquire(
        portfolioDesign,
        portfolioSnapshot,
        bidAskCostFractionOfNAV,
        maxAllowedDeviation,
        perETFTradingCost,
        accumulatedExDividends,
        accumulatedCash
    )

  def maxQuantities:Seq[AddnlQty] = {

    firstEstimateQuantitiesToAcquire.map { fEQTA =>
      val maybeNAV =
        portfolioSnapshot.sameDateUniqueCodesETFDatas.find(eTFData => eTFData.eTFCode == fEQTA.eTFCode).map(_.nAV)
      val qty = maybeNAV.fold(0) { nAV =>
        if (fEQTA.quantityToAcquire <= 0)
          0
        else
          math
          .floor(
            (cashRemaining(firstEstimateQuantitiesToAcquire) + accumulatedExDividends + accumulatedCash) / nAV).toInt
      }
      AddnlQty(fEQTA.eTFCode, qty)
    }
  }

  def additionalQuantities:Seq[Seq[AddnlQty]] =
    maxQuantities.foldLeft[Seq[Seq[AddnlQty]]](Seq()) { case (acc, maxQ) =>
      if (acc.isEmpty) {
        (0 to maxQ.quanitity map { qty => Seq(AddnlQty(maxQ.eTFCode, qty)) }).toSeq
      } else
        (0 to maxQ.quanitity map { qty => AddnlQty(maxQ.eTFCode, qty) }).toSeq.flatMap { subsequent =>
          acc.map { accumulated => accumulated :+ subsequent }
        }
    }

  def maxAbsDeviation(
    portfolioDesign: PortfolioDesign,
    portfolioSnapshot: PortfolioSnapshot,
    portfolioQuantitiesToAcquire: PortfolioQuantitiesToAcquire): Double = {

    val newPortfolioValue = portfolioSnapshot.sameDateUniqueCodesETFDatas.map { eTFData =>
      val price = eTFData.nAV
      val quantity =
        portfolioQuantitiesToAcquire.quantitiesToAcquire
        .find(_.eTFCode == eTFData.eTFCode).fold(0d)(_.quantityToAcquire) + eTFData.quantity
      price * quantity
    }.sum

    portfolioDesign.eTFSelections.map { selection =>
      val eTFSnapshot = portfolioSnapshot.sameDateUniqueCodesETFDatas.find(_.eTFCode == selection.eTFCode)
      val quantityToAcquire =
        portfolioQuantitiesToAcquire.quantitiesToAcquire.find(_.eTFCode == selection.eTFCode).fold(0)(_.quantityToAcquire)
      val nAV = eTFSnapshot.map(_.nAV).getOrElse(0d)
      val newValue = (quantityToAcquire + eTFSnapshot.map(_.quantity).getOrElse(0d)) * nAV
      if (newPortfolioValue <= 0) 1d else math.abs(selection.desiredWeight - newValue / newPortfolioValue)
    }.max

   }

  def finalQuantities:FinalPortfolioQuantitiesToHave = {

    lazy val finalPortfolioQuantitiesToHaveDefault = portfolioSnapshot.sameDateUniqueCodesETFDatas.map { eTFData =>
      FinalPortfolioQuantityToHave(eTFData.eTFCode, eTFData.quantity.toInt)
    }

    val initialCashRemaining =
      cashRemaining(firstEstimateQuantitiesToAcquire) + accumulatedCash + accumulatedExDividends

    additionalQuantities.map { additionalQuantities =>
      val newQuantitiesToAcquire = PortfolioQuantitiesToAcquire(firstEstimateQuantitiesToAcquire) + additionalQuantities
      val newCashRemaining =
        cashRemaining(newQuantitiesToAcquire.quantitiesToAcquire) + accumulatedCash + accumulatedExDividends
      val maxActualDeviation = maxAbsDeviation(portfolioDesign, portfolioSnapshot, newQuantitiesToAcquire)
      val finalPortfolioQuantitiesToHave = additionalQuantities.map { additionalQuantity =>
        val initialQuantity =
          portfolioSnapshot.sameDateUniqueCodesETFDatas.find(_.eTFCode == additionalQuantity.eTFCode).fold(0)(_.quantity.toInt)
        val firstEstimatedQuantityToAcquire =
          firstEstimateQuantitiesToAcquire.find(_.eTFCode == additionalQuantity.eTFCode).fold(0)(_.quantityToAcquire)
        val finalQuantity = initialQuantity + firstEstimatedQuantityToAcquire + additionalQuantity.quanitity
        FinalPortfolioQuantityToHave(additionalQuantity.eTFCode, finalQuantity)
      }
      FinalPortfolioQuantitiesToHave(
        finalPortfolioQuantitiesToHave,
        newCashRemaining,
        maxActualDeviation,
        additionalQuantities)
    }
    .filterNot(_.cashRemaining < 0)
    .sortBy( finalPortfolio => (finalPortfolio.cashRemaining, finalPortfolio.maxActualDeviation))
    .headOption
    .getOrElse(FinalPortfolioQuantitiesToHave(finalPortfolioQuantitiesToHaveDefault, initialCashRemaining, 0, Seq()))
  }

  def cashRemaining(quantitiesToAcquire: Seq[PortfolioQuantityToAcquire]): Double =
    -1 * quantitiesToAcquire.map { initialQuantityToAcquire =>
      initialQuantityToAcquire.quantityToAcquire * initialQuantityToAcquire.effectivePrice}.sum
}
