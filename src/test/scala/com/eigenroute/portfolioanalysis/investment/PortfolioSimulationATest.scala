package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.PortfolioFixture
import com.eigenroute.portfolioanalysis.investment.RebalancingInterval.SemiAnnually
import org.scalatest.{FlatSpec, ShouldMatchers}

class PortfolioSimulationATest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  import com.eigenroute.portfolioanalysis.DatasetFixture._

  "The portfolio simulator" should "simulate rebalanced portfolios for multiple investment periods" in {

    val simulationResults =
      new PortfolioSimulation(2, SemiAnnually, 10040, 10, 0.0011, portfolioDesign, 0.05, investmentInputDatasetSemiAnnually)
      .simulate

    simulationResults

  }

}
