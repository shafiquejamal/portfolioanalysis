package com.eigenroute.portfolioanalysis.investment

import scala.language.implicitConversions

sealed trait RebalancingInterval {
  def months:Int
}

object RebalancingInterval extends {

  case object Monthly extends RebalancingInterval { override val months:Int = 1 }
  case object Quarterly extends RebalancingInterval { override val months:Int = 3 }
  case object SemiAnnually extends RebalancingInterval { override val months:Int = 6 }
  case object Annually extends RebalancingInterval { override val months:Int = 12 }

  implicit def rebalancingIntervalToNumberOfMonths(rebalancingPeriod: RebalancingInterval):Int = rebalancingPeriod.months

}
