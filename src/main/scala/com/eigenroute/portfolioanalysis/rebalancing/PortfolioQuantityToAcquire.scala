package com.eigenroute.portfolioanalysis.rebalancing

case class PortfolioQuantityToAcquire(
    eTFCode: ETFCode,
    quantityToAcquire:Int,
    effectivePrice: Double,
    fractionalQuantity: Double)

case class PortfolioQuantitiesToAcquire(quantitiesToAcquire: Seq[PortfolioQuantityToAcquire]) {

  def +(additionalQuantities:Seq[AddnlQty]):PortfolioQuantitiesToAcquire = {
    PortfolioQuantitiesToAcquire(this.quantitiesToAcquire.map { quantityToHave =>
      val quantityToAdd:Int = additionalQuantities.find(_.eTFCode == quantityToHave.eTFCode).map(_.quanitity).getOrElse(0)
      quantityToHave.copy(quantityToAcquire = quantityToHave.quantityToAcquire + quantityToAdd)
    })
  }

}

case class FinalPortfolioQuantitiesToAcquire(quantities: PortfolioQuantitiesToAcquire, cashRemaining: Double)

case class FinalPortfolioQuantityToHave(eTFCode: ETFCode, quantity: Int)

case class FinalPortfolioQuantitiesToHave(
    quantitiesToHave: Seq[FinalPortfolioQuantityToHave],
    cashRemaining: Double,
    maxActualDeviation: Double,
    additionalQuantities: Seq[AddnlQty])