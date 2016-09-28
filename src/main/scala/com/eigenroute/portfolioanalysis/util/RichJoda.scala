package com.eigenroute.portfolioanalysis.util

import java.sql.Date

import org.joda.time.DateTime

import scala.language.implicitConversions

object RichJoda {

  implicit def dateTimeToJavaSQLDate(dateTime: DateTime): Date = new Date(dateTime.getMillis)

  implicit def javaSQLDateToDateTime(date: Date): DateTime = new DateTime(date.getTime)

}
