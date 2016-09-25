package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.PortfolioFixture
import com.eigenroute.portfolioanalysis.investment.RebalancingInterval.{Annually, Monthly, Quarterly, SemiAnnually}
import org.scalatest.{FlatSpec, ShouldMatchers}

class InvestmentUTest extends FlatSpec with ShouldMatchers with PortfolioFixture {

  import com.eigenroute.portfolioanalysis.DatasetFixture._

  "The number of rebalancing opportunities" should "be calculated as investment period in months divided by rebalancing " +
  "period on months" in new InvestmentFixture {
    val investmentMonthlyRebalancing =
      new Investment(investmentPeriod, Monthly, 10040d, 10d, 0.0011, portfolioDesign, 0d, sortedCommonDatesDataset)

    val investmentQuarterlyRebalancing =
      new Investment(investmentPeriod, Quarterly, 10040d, 10d, 0.0011, portfolioDesign, 0d, sortedCommonDatesDataset)

    val investmentSemiAnnuallyRebalancing =
      new Investment(investmentPeriod, SemiAnnually, 10040d, 10d, 0.0011, portfolioDesign, 0d, sortedCommonDatesDataset)

    val investmentAnnualRebalancing =
      new Investment(investmentPeriod, Annually, 10040d, 10d, 0.0011, portfolioDesign, 0d, sortedCommonDatesDataset)

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

}
