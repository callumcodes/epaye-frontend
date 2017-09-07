/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.epaye.models.domain

import org.joda.time.LocalDate

import scala.util.Try

case class TaxMonth(month: Int) {
  def firstDay: Int = 6
  def lastDay: Int = 5
  def startCalendarMonth: Int = toCalendarMonth(month)
  def endCalendarMonth: Int = toCalendarMonth((month + 1) % 12)
  def asString: String = month.toString

  private def toCalendarMonth(taxMonth: Int): Int =
    (taxMonth + 2) % 12 + 1
}

object TaxMonth {
  def fromLocalDate(date: LocalDate) = {
    val month = if (date.getDayOfMonth < 6) date.minusMonths(1).getMonthOfYear
    else date.getMonthOfYear
    TaxMonth(((month + 8) % 12) + 1)
  }

  def fromUrlEncodedValue(value: String): TaxMonth = {
    for {
      month <- Try(value.toInt).toOption if 1 <= month && month <= 12
    } yield TaxMonth(month)
  }.getOrElse {
    throw new IllegalArgumentException("Tax month requires an integer between 1 and 12 inclusive")
  }

  def toUrlEncodedValue(taxMonth: TaxMonth): String =
    taxMonth.month.toString

}

