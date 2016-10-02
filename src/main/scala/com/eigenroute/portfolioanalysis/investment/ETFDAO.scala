package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.db.DBConfig
import com.eigenroute.portfolioanalysis.rebalancing.{ETFCode, ETFData}
import scalikejdbc._

class ETFDAO(dBConfig: DBConfig) {

  def by(eTFCodes: Seq[ETFCode]): Seq[ETFData] = {
    NamedDB(Symbol(dBConfig.dBName)) readOnly { implicit session =>
      val codes: Seq[String] = eTFCodes.map(_.code)
      sql"""SELECT code, xnumber, asofdate, nav, exdividend FROM historical WHERE code IN (${codes})"""
      .map(ETFData.converter).list.apply()
    }
  }

}
