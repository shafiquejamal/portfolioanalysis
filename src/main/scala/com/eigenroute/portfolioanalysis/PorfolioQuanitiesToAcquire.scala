package com.eigenroute.portfolioanalysis

case class PorfolioQuanitiesToAcquire(
  eTFCode: ETFCode, quantityToAcquire:Int, effectivePrice: Double, fractionalQuantity: Double)
