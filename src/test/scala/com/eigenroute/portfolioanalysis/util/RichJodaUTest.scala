package com.eigenroute.portfolioanalysis.util

import java.sql.Date

import com.eigenroute.portfolioanalysis.util.RichJoda._
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, ShouldMatchers}

class RichJodaUTest extends FlatSpec with ShouldMatchers {

  val jodaTime = new DateTime(2016, 1, 2, 0, 0, 0)
  val javaSqlDate = new Date(jodaTime.getMillis)

  "converting from java sql date to joda datetime" should "work" in {
    javaSQLDateToDateTime(javaSqlDate) shouldEqual jodaTime
  }

  "converting from joda datetime to java sql date " should "work" in {
    dateTimeToJavaSQLDate(jodaTime) shouldEqual javaSqlDate
  }

}
