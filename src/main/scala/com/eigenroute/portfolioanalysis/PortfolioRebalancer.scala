package com.eigenroute.portfolioanalysis

case class AddnlQty(eTFCode: ETFCode, quanitity: Int)

class PortfolioRebalancer extends PortfolioValueCalculation {

  def maxQuantitiesGenerator(
    firstEstimateQuantitiesToAcquire: Seq[PorfolioQuanitiesToAcquire],
    portfolioSnapshot: PortfolioSnapshot):Seq[AddnlQty] = {

    val initialCashRemaining: Double = -1 * firstEstimateQuantitiesToAcquire.map { initialQuantityToAcquire =>
      initialQuantityToAcquire.quantityToAcquire * initialQuantityToAcquire.effectivePrice}.sum

    firstEstimateQuantitiesToAcquire.map { fEQTA =>
      val maybeNAV = portfolioSnapshot.eTFDatas.find( eTFData => eTFData.eTFCode == fEQTA.eTFCode).map(_.nAV)
      val qty = maybeNAV.fold(0) { nAV =>
        if (fEQTA.quantityToAcquire <= 0) 0 else math.floor(initialCashRemaining / nAV).toInt }
      AddnlQty(fEQTA.eTFCode, qty)
    }

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

}
