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
import uk.gov.hmrc.time.TaxYearResolver

import scala.util.Try

case class TaxYear(yearFrom: Int) {
  def yearTo: Int =
    yearFrom + 1

  def asString =
    s"$yearFrom-${yearTo % 100}"

  def asShortString =
    s"${yearFrom % 100}${yearTo % 100}"

  def startingYearForTaxMonth(taxMonth: TaxMonth): Int = {
    if (taxMonth.startCalendarMonth >= 4) yearFrom else yearTo
  }
  def endYearForTaxMonth(taxMonth: TaxMonth): Int = {
    if (taxMonth.endCalendarMonth > 4) yearFrom else yearTo
  }
}

object TaxYear {

  def fromTaxPeriod(taxPeriod: TaxPeriod): TaxYear = {
    val taxCalendarMonth = TaxMonth.fromLocalDate(taxPeriod.taxFrom)

    val x = taxPeriod.taxFrom.minusMonths(taxCalendarMonth.month)

    TaxYear(x.getYear)
  }

  def fromLocalDate(localDate: LocalDate) =
    TaxYear(TaxYearResolver.taxYearFor(localDate))

  def fromUrlEncodedValue(value: String): TaxYear = {
    def throwIllegalArgument =
      throw new IllegalArgumentException("Tax year requires two numbers separated by a hyphen")

    val regex = """20(\d\d)-(\d\d)""".r
    value match {
      case regex(first, second) => {
        val taxYearOpt = for {
          fromYear <- Try(first.toInt).toOption
          toYear <- Try(second.toInt).toOption
          taxYear <- TaxYear.fromInts(2000 + fromYear, 2000 + toYear)
        } yield taxYear
        taxYearOpt.getOrElse(throwIllegalArgument)
      }
      case _ => throwIllegalArgument
    }
  }

  def toUrlEncodedValue(taxYear: TaxYear): String = taxYear.asString

  def fromInts(fromYear: Int, toYear: Int): Option[TaxYear] =
    if (fromYear + 1 == toYear) Some(TaxYear(fromYear))
    else None

}

trait WithTaxYear {
  def taxYear: TaxYear
}
