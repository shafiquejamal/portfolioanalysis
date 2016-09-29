package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.PortfolioFixture
import com.eigenroute.portfolioanalysis.investment.RebalancingInterval.SemiAnnually
import org.scalatest.{FlatSpec, ShouldMatchers}

class PortfolioSimulationATest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  "The portfolio simulator" should "simulate rebalanced portfolios for multiple investment periods" in
  new InvestmentFixture {

    val expectedAverageAnnualReturns: Seq[BigDecimal] =
      Seq(
        1.330468920295469,
        0.0973553653897836,
        1.0287482857153942,
        -0.726561882616373,
        3.23328227281166,
        -0.5255899232806065,
        2.955323253380445,
        -0.5347424130020722,
        2.564018737053584,
        1.575562015542863
      )

    val simulationResults =
      new PortfolioSimulation(2, SemiAnnually, 10040, 10, 0.0011, portfolioDesign, 0.05, investmentInputDataSemiAnnually)
      .simulate
    val actualAverageAnnualReturns = simulationResults.map(_.averageAnnualReturnFraction)

    actualAverageAnnualReturns.map(avgAnnualReturn => round(avgAnnualReturn)) should contain theSameElementsAs
      expectedAverageAnnualReturns.map(avgAnnualReturn => round(avgAnnualReturn))

  }

}
