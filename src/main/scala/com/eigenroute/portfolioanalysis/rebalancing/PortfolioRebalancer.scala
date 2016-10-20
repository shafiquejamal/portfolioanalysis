package com.eigenroute.portfolioanalysis.rebalancing

class PortfolioRebalancer(
    portfolioDesign: PortfolioDesign,
    portfolioSnapshot: PortfolioSnapshot,
    bidAskCostFractionOfNAV: BigDecimal,
    maxAllowedDeviation: BigDecimal,
    perETFTradingCost: BigDecimal,
    accumulatedExDividends: BigDecimal,
    accumulatedCash: BigDecimal,
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

  def maxQuantities:Seq[AddnlQty] =
    firstEstimateQuantitiesToAcquire.map { fEQTA =>
      val maybeNAV =
        portfolioSnapshot.sameDateUniqueCodesETFDatas.find(eTFData => eTFData.eTFCode == fEQTA.eTFCode).map(_.nAV)
      val qty = maybeNAV.fold(0) { nAV =>
        if (fEQTA.quantityToAcquire <= 0)
          0
        else {
          val cashRemainingAfterPurchFirstEst = cashRemaining(firstEstimateQuantitiesToAcquire)
          math
          .floor(
            ((cashRemainingAfterPurchFirstEst + accumulatedExDividends + accumulatedCash -
              1*perETFTradingCost) / nAV).toDouble).toInt
        }
      }
      AddnlQty(fEQTA.eTFCode, qty)
    }


  def additionalQuantities:Seq[Seq[AddnlQty]] =
    maxQuantities.foldLeft[Seq[Seq[AddnlQty]]](Seq()) { case (acc, maxQ) =>
      if (acc.isEmpty)
        (0 to maxQ.quantity map { qty => Seq(AddnlQty(maxQ.eTFCode, qty)) }).toSeq
      else
        (0 to maxQ.quantity map { qty => AddnlQty(maxQ.eTFCode, qty) }).toSeq.flatMap { subsequent =>
          acc.map { accumulated => accumulated :+ subsequent }
        }
    }

  def maxAbsDeviation(portfolioQuantitiesToAcquire: PortfolioQuantitiesToAcquire): Double = {

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
      val nAV: BigDecimal = eTFSnapshot.map(_.nAV).getOrElse(0)
      val newValue = (quantityToAcquire + eTFSnapshot.map(_.quantity.toDouble).getOrElse(0d)) * nAV
      if (newPortfolioValue <= 0) 1d else math.abs((selection.desiredWeight - newValue / newPortfolioValue).toDouble)
    }.max

   }

  def finalQuantities:FinalPortfolioQuantitiesToHave = {

    lazy val finalPortfolioQuantitiesToHaveDefault = portfolioSnapshot.sameDateUniqueCodesETFDatas.map { eTFData =>
      FinalPortfolioQuantityToHave(eTFData.eTFCode, eTFData.quantity.toInt)
    }

    val initialCashRemaining =
      cashRemaining(firstEstimateQuantitiesToAcquire) + accumulatedCash + accumulatedExDividends -
      perETFTradingCost*firstEstimateQuantitiesToAcquire.filterNot(_.quantityToAcquire == 0).length

    additionalQuantities.map { additionalQtys =>
      val newQuantitiesToAcquire = PortfolioQuantitiesToAcquire(firstEstimateQuantitiesToAcquire) + additionalQtys
      val newCashRemaining =
        cashRemaining(newQuantitiesToAcquire.quantitiesToAcquire) + accumulatedCash + accumulatedExDividends -
        perETFTradingCost*newQuantitiesToAcquire.quantitiesToAcquire.filterNot(_.quantityToAcquire == 0).length
      val maxActualDeviation = maxAbsDeviation(newQuantitiesToAcquire)
      val finalPortfolioQuantitiesToHave = additionalQtys.map { additionalQuantity =>
        val initialQuantity =
          portfolioSnapshot.sameDateUniqueCodesETFDatas
          .find(_.eTFCode == additionalQuantity.eTFCode).fold(0)(_.quantity.toInt)
        val firstEstimatedQuantityToAcquire =
          firstEstimateQuantitiesToAcquire.find(_.eTFCode == additionalQuantity.eTFCode).fold(0)(_.quantityToAcquire)
        val finalQuantity = initialQuantity + firstEstimatedQuantityToAcquire + additionalQuantity.quantity
        FinalPortfolioQuantityToHave(additionalQuantity.eTFCode, finalQuantity)
      }
      FinalPortfolioQuantitiesToHave(
        finalPortfolioQuantitiesToHave,
        newCashRemaining,
        maxActualDeviation,
        additionalQtys)
    }
    .filterNot(_.cashRemaining < 0)
    .sortBy( finalPortfolio => (finalPortfolio.cashRemaining, finalPortfolio.maxActualDeviation))
    .headOption
    .getOrElse(FinalPortfolioQuantitiesToHave(finalPortfolioQuantitiesToHaveDefault, initialCashRemaining, 0, Seq()))
  }

  def cashRemaining(quantitiesToAcquire: Seq[PortfolioQuantityToAcquire]): BigDecimal = {
   val cashNeededForEachTransaction =
    quantitiesToAcquire.map { initialQuantityToAcquire =>
      initialQuantityToAcquire.quantityToAcquire * initialQuantityToAcquire.effectivePrice
    }
    -1*cashNeededForEachTransaction.sum
  }
}
