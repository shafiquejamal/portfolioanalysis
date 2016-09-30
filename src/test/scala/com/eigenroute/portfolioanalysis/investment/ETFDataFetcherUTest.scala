package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.PortfolioFixture
import com.eigenroute.portfolioanalysis.rebalancing.ETFCode
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ShouldMatchers, FlatSpec}

class ETFDataFetcherUTest extends FlatSpec with ShouldMatchers with MockFactory with PortfolioFixture {

  class NoArgDAO extends ETFDAO(null)
  val mockDAO = mock[NoArgDAO]
  val fetcher = new ETFDataFetcher(mockDAO)

  "The data fetcher" should "return data that have only entrie s that have common dates for all ETFs specified" in
  new ETFDataFixture {

    val expected = expectedFetchResult.reverse ++ Seq(eTFDataBBB1, eTFDataBBB2, eTFDataDDD1, eTFDataDDD2)
    val eTFSs = Seq(ETFCode("AAA"), ETFCode("BBB"), ETFCode("CCC"), ETFCode("DDD"))
    (mockDAO.by _).expects(eTFSs).returning(expected)

    fetcher.fetch(portfolioDesign) should contain theSameElementsInOrderAs expected.sortWith( (d1, d2) =>
      d1.asOfDate.isBefore(d2.asOfDate))
  }

}
