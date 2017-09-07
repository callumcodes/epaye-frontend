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

import play.api.i18n.Messages

case class Code(main: String, sub: String)

object Code {
  val Class1ABillCode: Code = Code("2060", "1020")
  val Class1AInterestOnBillCode: Code = Code("2065", "2020")
  val Class1APenaltyCode: Code = Code("2530", "1090")
  val Class1AInterestOnPenaltyCode: Code = Code("2535", "2090")

  val NonRtiMultiBillPenaltyCodes = Set(
    Code("2500", "1090"),
    Code("2590", "1090")
  )

  val nonRtiPenaltyCodes: Set[Code] = NonRtiMultiBillPenaltyCodes ++ Set(
    Code("2510", "1090"),
    Code("2520", "1090"),
    Code("2760", "1090"),
    Code("2762", "1090"),
    Code("2764", "1090"),
    Code("2766", "1090"),
    Code("2768", "1090"),
    Code("2540", "1090"),
    Code("2550", "1090"),
    Code("1025", "1090"),
    Code("1030", "1090"),
    Code("1035", "1090"),
    Code("1040", "1090"),
    Code("1045", "1090"),
    Code("2560", "1090"),
    Code("2570", "1090"),
    Code("2580", "1090"),
    Code("2110", "1090"),
    Code("2115", "1090"),
    Code("2120", "1090"),
    Code("2125", "1090"),
    Code("1481", "1090"),
    Code("1482", "1090")
  )

  object NonRtiPenaltyCode {
    def unapply(code: Code): Boolean = {
      nonRtiPenaltyCodes contains code
    }
  }

  val InterestOnEarlierYearUpdateCodes: Seq[Code] = Seq(
    Code("2105", "2000"),
    Code("2105", "2023"),
    Code("2105", "2026"),
    Code("2105", "2100")
  )

  def codeToName(code: Code)(implicit messages: Messages): String =
    Messages(s"epaye.charge.name.${code.main}.${code.sub}")

  implicit val codeOrdering = Ordering.by[Code, (String, String)](code => (code.main, code.sub))
}
