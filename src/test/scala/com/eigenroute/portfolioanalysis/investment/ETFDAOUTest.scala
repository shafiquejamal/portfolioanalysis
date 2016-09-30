package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.db.{InitialMigration, ScalikeJDBCTestDBConfig}
import com.eigenroute.portfolioanalysis.rebalancing.ETFCode
import org.scalatest.{BeforeAndAfterEach, FlatSpec, ShouldMatchers}
import scalikejdbc.NamedAutoSession

class ETFDAOUTest
  extends FlatSpec
  with ShouldMatchers
  with ETFDataFixture
  with InitialMigration
  with BeforeAndAfterEach
  {

  val dbConfig = new ScalikeJDBCTestDBConfig()

  override def beforeEach(): Unit = {
    implicit val session = NamedAutoSession(Symbol(dbConfig.dBName))
    dbConfig.setUpAllDB()
    migrate(dbConfig)
    eTFDataToAdd.foreach(_.update().apply())
    session.close()
    super.beforeEach()
  }

  override def afterEach(): Unit = {
    dbConfig.closeAll()
    super.afterEach()
  }

  val eTFDAO = new ETFDAO(dbConfig)

  "Querrying by ETF codes" should "return the data for the given ETF codes" in {

    val eTFData = eTFDAO.by(Seq(ETFCode("AAA"), ETFCode("CCC")))
    eTFData should contain theSameElementsAs expectedFetchResult
  }

}
