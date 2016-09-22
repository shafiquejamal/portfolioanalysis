package com.eigenroute.portfolioanalysis.rebalancing

class PortfolioRebalancer extends PortfolioValueCalculation {

  def maxQuantitiesGenerator(
      firstEstimateQuantitiesToAcquire: Seq[PortfolioQuantityToAcquire],
      portfolioSnapshot: PortfolioSnapshot):Seq[AddnlQty] =
    firstEstimateQuantitiesToAcquire.map { fEQTA =>
      val maybeNAV = portfolioSnapshot.eTFDatas.find( eTFData => eTFData.eTFCode == fEQTA.eTFCode).map(_.nAV)
      val qty = maybeNAV.fold(0) { nAV =>
        if (fEQTA.quantityToAcquire <= 0)
          0
        else
          math.floor(cashRemaining(firstEstimateQuantitiesToAcquire) / nAV).toInt }
      AddnlQty(fEQTA.eTFCode, qty)
    }

  def additionalQuanititiesGenerator(maxQuantities: Seq[AddnlQty]):Seq[Seq[AddnlQty]] =
    maxQuantities.foldLeft[Seq[Seq[AddnlQty]]](Seq()) { case (acc, maxQ) =>
      if (acc.isEmpty) {
        (0 to maxQ.quanitity map { qty => Seq(AddnlQty(maxQ.eTFCode, qty)) }).toSeq
      } else
        (0 to maxQ.quanitity map { qty => AddnlQty(maxQ.eTFCode, qty) }).toSeq.flatMap { subsequent =>
          acc.map { accumulated => accumulated :+ subsequent
          }
        }
    }

  def maxAbsDeviation(
    portfolioDesign: PortfolioDesign,
    portfolioSnapshot: PortfolioSnapshot,
    portfolioQuantitiesToAcquire: PortfolioQuantitiesToAcquire): Double = {

    val newPortfolioValue = portfolioSnapshot.eTFDatas.map { eTFData =>
      val price = eTFData.nAV
      val quantity =
        portfolioQuantitiesToAcquire.quantitiesToAcquire
        .find(_.eTFCode == eTFData.eTFCode).fold(0d)(_.quantityToAcquire) + eTFData.quantity
      price * quantity
    }.sum

    portfolioDesign.eTFSelections.map { selection =>
      val eTFSnapshot = portfolioSnapshot.eTFDatas.find(_.eTFCode == selection.eTFCode)
      val quantityToAcquire =
        portfolioQuantitiesToAcquire.quantitiesToAcquire.find(_.eTFCode == selection.eTFCode).fold(0)(_.quantityToAcquire)
      val nAV = eTFSnapshot.map(_.nAV).getOrElse(0d)
      val newValue = (quantityToAcquire + eTFSnapshot.map(_.quantity).getOrElse(0d)) * nAV
      val newWeight =  newValue / newPortfolioValue
      math.abs(selection.desiredWeight - newWeight)
    }.max

   }

  def finalQuantitiesChooser(
    portfolioDesign: PortfolioDesign,
    portfolioSnapshot: PortfolioSnapshot,
    firstEstimateQuantitiesToAcquire: PortfolioQuantitiesToAcquire,
    additionalQuantities: Seq[Seq[AddnlQty]]):FinalPortfolioQuantitiesToHave = {

    lazy val finalPortfolioQuantitiesToHaveDefault = portfolioSnapshot.eTFDatas.map { eTFData =>
      FinalPortfolioQuantityToHave(eTFData.eTFCode, eTFData.quantity.toInt)
    }

    val initialCashRemaining = cashRemaining(firstEstimateQuantitiesToAcquire.quantitiesToAcquire)

    additionalQuantities.map { additionalQuantities =>
      val newQuantitiesToAcquire = firstEstimateQuantitiesToAcquire + additionalQuantities
      val newCashRemaining = cashRemaining(newQuantitiesToAcquire.quantitiesToAcquire)
      val maxActualDeviation = maxAbsDeviation(portfolioDesign, portfolioSnapshot, newQuantitiesToAcquire)
      val finalPortfolioQuantitiesToHave = additionalQuantities.map { additionalQuantity =>
        val initialQuantity =
          portfolioSnapshot.eTFDatas.find(_.eTFCode == additionalQuantity.eTFCode).fold(0)(_.quantity.toInt)
        val firstEstimatedQuantityToAcquire =
          firstEstimateQuantitiesToAcquire.quantitiesToAcquire
          .find(_.eTFCode == additionalQuantity.eTFCode).fold(0)(_.quantityToAcquire)
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