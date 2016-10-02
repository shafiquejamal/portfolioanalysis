package com.eigenroute.portfolioanalysis.investment

import java.util.UUID

import com.eigenroute.portfolioanalysis.rebalancing.{ETFCode, ETFData}
import org.joda.time.DateTime
import scalikejdbc._

trait ETFDataFixture {

  val id1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
  val id2 = UUID.fromString("00000000-0000-0000-0000-000000000002")
  val id3 = UUID.fromString("00000000-0000-0000-0000-000000000003")
  val id4 = UUID.fromString("00000000-0000-0000-0000-000000000004")
  val id5 = UUID.fromString("00000000-0000-0000-0000-000000000005")
  val id6 = UUID.fromString("00000000-0000-0000-0000-000000000006")
  val date1 = new DateTime(2016, 3, 21, 0, 0, 0)
  val dateLater = new DateTime(2016, 3, 26, 0, 0, 0)

  val now = new DateTime(2016, 6, 1, 0, 0, 0)

  val eTFDataAAA1 = ETFData(date1, ETFCode("AAA"), "1", 110, 25, 0)
  val eTFDataAAA2 = ETFData(dateLater, ETFCode("AAA"), "1", 111, 26, 0)
  val eTFDataBBB1 = ETFData(date1, ETFCode("BBB"), "1", 120, 35, 0)
  val eTFDataBBB2 = ETFData(dateLater, ETFCode("BBB"), "1", 121, 36, 0)
  val eTFDataCCC1 = ETFData(date1, ETFCode("CCC"), "1", 130, 45, 0)
  val eTFDataCCC2 = ETFData(dateLater, ETFCode("CCC"), "1", 131, 46, 0)
  val eTFDataDDD1 = ETFData(date1, ETFCode("DDD"), "1", 130, 45, 0)
  val eTFDataDDD2 = ETFData(dateLater, ETFCode("DDD"), "1", 131, 46, 0)

  val eTFDataToAdd = Seq(
    sql"""INSERT INTO historical (id, code, brand, xnumber, indexreturn, nav, asofdate, exdividend, createdat) values
         (${id1}, 'AAA', 'iSharesblahblahblah', '1', 1, 110, ${date1}, 25, ${now})
       """,
    sql"""INSERT INTO historical (id, code, brand, xnumber, indexreturn, nav, asofdate, exdividend, createdat) values
         (${id2}, 'AAA', 'iSharesblahblahblah', '1', 1, 111, ${dateLater}, 26, ${now})
       """,
    sql"""INSERT INTO historical (id, code, brand, xnumber, indexreturn, nav, asofdate, exdividend, createdat) values
         (${id3}, 'BBB', 'iSharesblahblahblah', '1', 1, 120, ${date1}, 35, ${now})
       """,
    sql"""INSERT INTO historical (id, code, brand, xnumber, indexreturn, nav, asofdate, exdividend, createdat) values
         (${id4}, 'BBB', 'iSharesblahblahblah', '1', 1, 121, ${dateLater}, 36, ${now})
       """,
    sql"""INSERT INTO historical (id, code, brand, xnumber, indexreturn, nav, asofdate, exdividend, createdat) values
         (${id5}, 'CCC', 'iSharesblahblahblah', '1', 1, 130, ${date1}, 45, ${now})
       """,
    sql"""INSERT INTO historical (id, code, brand, xnumber, indexreturn, nav, asofdate, exdividend, createdat) values
         (${id6}, 'CCC', 'iSharesblahblahblah', '1', 1, 131, ${dateLater}, 46, ${now})"""
  )

  val expectedFetchResult = Seq(eTFDataAAA1, eTFDataAAA2, eTFDataCCC1, eTFDataCCC2)
}
