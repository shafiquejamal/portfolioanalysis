package com.eigenroute.portfolioanalysis.investment

import com.eigenroute.portfolioanalysis.db.DBConfig
import com.eigenroute.portfolioanalysis.rebalancing.{ETFCode, ETFDataPlus}
import scalikejdbc._

class ETFDAO(dBConfig: DBConfig) {

  def by(eTFCodes: Seq[ETFCode]): Seq[ETFDataPlus] = {
    NamedDB(Symbol(dBConfig.dBName)) readOnly { implicit session =>
      val codes: Seq[String] = eTFCodes.map(_.code)
      sql"""SELECT code, xnumber, asofdate, nav, exdividend FROM historical WHERE code IN (${codes})"""
      .map(ETFDataPlus.converter).list.apply()
    }
  }

}
