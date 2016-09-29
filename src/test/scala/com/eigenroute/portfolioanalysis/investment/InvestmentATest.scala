package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.PortfolioFixture
import com.eigenroute.portfolioanalysis.investment.RebalancingInterval.{Annually, Monthly, Quarterly, SemiAnnually}
import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, FinalPortfolioQuantityToHave}
import com.eigenroute.portfolioanalysis.util.RichJoda._
import org.apache.spark.sql.Dataset
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, ShouldMatchers}

class InvestmentATest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  import com.eigenroute.portfolioanalysis.DatasetFixture._

  "The number of rebalancing opportunities" should "be calculated as investment period in months divided by rebalancing " +
  "period on months" in new InvestmentFixture {
    val investmentMonthlyRebalancing =
      new Investment(investmentPeriod, Monthly, 10040, 10, 0.0011, portfolioDesign, 0.05, sortedCommonDatesDataset)

    val investmentQuarterlyRebalancing =
      new Investment(investmentPeriod, Quarterly, 10040, 10, 0.0011, portfolioDesign, 0.05, sortedCommonDatesDataset)

    val investmentSemiAnnuallyRebalancing =
      new Investment(investmentPeriod, SemiAnnually, 10040, 10, 0.0011, portfolioDesign, 0.05, sortedCommonDatesDataset)

    val investmentAnnualRebalancing =
      new Investment(investmentPeriod, Annually, 10040, 10, 0.0011, portfolioDesign, 0.05, sortedCommonDatesDataset)

    investmentMonthlyRebalancing.totalNumberOfRebalancingIntervals shouldEqual 36
    val datasetSplitIntoRebalancingIntervals =
      investmentMonthlyRebalancing.sortedDatasetsSplitByRebalancingPeriod.map(_.collect().toList)
    datasetSplitIntoRebalancingIntervals.length shouldEqual 36
    datasetSplitIntoRebalancingIntervals.foreach { ds => ds should not be empty }

    investmentQuarterlyRebalancing.totalNumberOfRebalancingIntervals shouldEqual 12
    investmentQuarterlyRebalancing.sortedDatasetsSplitByRebalancingPeriod.length shouldEqual 12

    investmentSemiAnnuallyRebalancing.totalNumberOfRebalancingIntervals shouldEqual 6
    investmentSemiAnnuallyRebalancing.sortedDatasetsSplitByRebalancingPeriod.length shouldEqual 6

    investmentAnnualRebalancing.totalNumberOfRebalancingIntervals shouldEqual 3
    investmentAnnualRebalancing.sortedDatasetsSplitByRebalancingPeriod.length shouldEqual 3
  }

  "The simulation" should "create a dataset with the correct quantities" in new InvestmentFixture {
    val investmentPeriodOneYear = InvestmentPeriod(startDate, startDate.plusYears(1))
    val investment =
      new Investment(
        investmentPeriodOneYear, Quarterly, 10040, 10, 0.0011, portfolioDesign, 0, investmentInputDatasetQuarterly)
    val expectedRebalancedData = filterAndRound(expectedRebalancedPortfolioQuarterly, startDatePlus12months)
    val rebalancedPortfolio = investment.rebalancePortfolio
    val actualRebalancedData = collectAndRound(rebalancedPortfolio.rebalancedDataset)

    actualRebalancedData should contain theSameElementsAs expectedRebalancedData
    rebalancedPortfolio.accumulatedExDiv shouldEqual 20
    round(rebalancedPortfolio.accumulatedCash) shouldEqual round(1.1541624213365298172010788133061)
    rebalancedPortfolio.newQuantitiesChosenForThisRebalancing should contain theSameElementsAs Seq(
      FinalPortfolioQuantityToHave(eTFA, 184),
      FinalPortfolioQuantityToHave(eTFB, 246),
      FinalPortfolioQuantityToHave(eTFC, 41),
      FinalPortfolioQuantityToHave(eTFD, 47)
    )
    round(rebalancedPortfolio.liquidatedValue, 11) shouldEqual round(23595.1787353910698231944860653281361, 11)
    round(rebalancedPortfolio.totalReturnFraction, 11) shouldEqual round(1.350117403923413329003434867064555, 11)
    round(rebalancedPortfolio.averageAnnualReturnFraction, 11) shouldEqual round(1.35011740392, 11)
  }

  it should "rebalance correctly when the period is longer than one year and the max allowed deviation is greater than " +
  "zero" in {

    val investmentPeriodThreeYears = InvestmentPeriod(startDate, startDate.plusYears(3))
    val investment =
      new Investment(
        investmentPeriodThreeYears, SemiAnnually, 10040, 10, 0.0011, portfolioDesign, 0.05,
        investmentInputDatasetSemiAnnually)
    val expectedRebalancedData = filterAndRound(expectedRebalancedPortfolioSemiAnnually, startDatePlus36months)
    val rebalancedPortfolio = investment.rebalancePortfolio
    val actualRebalancedData = collectAndRound(rebalancedPortfolio.rebalancedDataset)

    actualRebalancedData should contain theSameElementsAs expectedRebalancedData
    rebalancedPortfolio.accumulatedExDiv shouldEqual 1.75
    round(rebalancedPortfolio.accumulatedCash) shouldEqual round(2.51041084806712616122265507941400)
    rebalancedPortfolio.newQuantitiesChosenForThisRebalancing should contain theSameElementsAs Seq(
      FinalPortfolioQuantityToHave(eTFA, 94),
      FinalPortfolioQuantityToHave(eTFB, 301),
      FinalPortfolioQuantityToHave(eTFC, 272),
      FinalPortfolioQuantityToHave(eTFD, 57)
    )
    round(rebalancedPortfolio.liquidatedValue, 11) shouldEqual round(98415.96353740885026470882029767256200, 11)
    round(rebalancedPortfolio.totalReturnFraction, 11) shouldEqual round(8.802386806514825723576575726859817, 11)
    round(rebalancedPortfolio.averageAnnualReturnFraction, 11) shouldEqual round(1.140148678530547, 11)
  }

  private def roundCashValue(eTFData: ETFDataPlus) = eTFData.copy(cash = round(eTFData.cash))

  private def collectAndRound(dataset: Dataset[ETFDataPlus]) = dataset.collect().toList.map(roundCashValue)

  private def filterAndRound(dataset: Dataset[ETFDataPlus], endDate :DateTime) =
    dataset.collect().toList.filter { eTFData => eTFData.asOfDate.isBefore(endDate)}.map(roundCashValue)
}
