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

package uk.gov.hmrc.epaye.models.json

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.epaye.models.domain._

import scala.PartialFunction.condOpt

case class EpayeResponseJson(
                              rtiCharges: Seq[RtiChargeJson],
                              appLevyCharges: Option[AppLevyChargesJson],
                              appLevyInterestCharges: Seq[AppLevyInterestChargeJson]
                            )

object EpayeResponseJson {
  implicit val rtiChargeJsonFormat: Format[RtiChargeJson] = Json.format[RtiChargeJson]
  implicit val appLevyChargesJsonFormat: Format[AppLevyChargesJson] = Json.format[AppLevyChargesJson]
  implicit val appLevyInterestChargeJsonFormat: Format[AppLevyInterestChargeJson] = Json.format[AppLevyInterestChargeJson]
  implicit val epayeResponseJsonFormat: Format[EpayeResponseJson] = Json.format[EpayeResponseJson]
}

case class RtiChargeJson(`type`: String, amount: BigDecimal, appLevy: Option[BigDecimal], interest: Option[BigDecimal], taxYear: Option[Int], taxMonth: Option[Int], dueDate: Option[LocalDate]) {

  import RtiChargeJson._

  def toRtiCharge: Option[RtiCharge] =
    condOpt((`type`, taxYear, taxMonth, dueDate)) {
      case (CreditType, Some(year), Some(month), None) =>
        RtiMonthCredit(year, month, amount)
      case (DebitType, Some(year), Some(month), Some(dueDateValue)) =>
        val interestValue = interest.getOrElse(Zero)
        val appLevyValue = appLevy.getOrElse(Zero)
        val breakdown = AmountBreakdown(appLevyValue, interestValue)
        RtiMonthDebit(year, month, amount, breakdown, dueDateValue)
      case (OverdueType, Some(year), Some(month), None) =>
        val interestValue = interest.getOrElse(Zero)
        val appLevyValue = appLevy.getOrElse(Zero)
        val breakdown = AmountBreakdown(appLevyValue, interestValue)
        RtiMonthOverdue(year, month, amount, breakdown)
      case (SpecifiedCreditType, Some(year), Some(month), None) =>
        SpecifiedRtiMonthCredit(year, month, amount)
      case (SpecifiedDebitType, Some(year), Some(month), Some(dueDateValue)) =>
        val interestValue = interest.getOrElse(Zero)
        SpecifiedRtiMonthDebit(year, month, amount, interestValue, dueDateValue)
      case (SpecifiedOverdueType, Some(year), Some(month), None) =>
        val interestValue = interest.getOrElse(Zero)
        SpecifiedRtiMonthOverdue(year, month, amount, interestValue)
      case (EyuCreditType, Some(year), None, None) =>
        EarlierYearUpdateCredit(year, amount)
      case (EyuDebitType, Some(year), None, Some(dueDate)) =>
        val interestValue = interest.getOrElse(Zero)
        EarlierYearUpdateDebit(year, amount, interestValue, dueDate)
      case (EyuOverdueType, Some(year), None, None) =>
        val interestValue = interest.getOrElse(Zero)
        EarlierYearUpdateOverdue(year, amount, interestValue)
    }
}

object RtiChargeJson {
  val Zero = BigDecimal(0)
  val CreditType = "credit"
  val DebitType = "debit"
  val OverdueType = "overdue"
  val SpecifiedCreditType = "specified-credit"
  val SpecifiedDebitType = "specified-debit"
  val SpecifiedOverdueType = "specified-overdue"
  val EyuCreditType = "eyu-credit"
  val EyuDebitType = "eyu-debit"
  val EyuOverdueType = "eyu-overdue"
}

case class AppLevyChargesJson(aggregatedOutstandingAmount: BigDecimal, aggregatedAccruedInterest: BigDecimal) {
  def toAppLevyCharges: AppLevyCharges = AppLevyCharges(aggregatedOutstandingAmount, aggregatedAccruedInterest)
}

case class AppLevyInterestChargeJson(amount: BigDecimal, dueDate: LocalDate, reference: String) {
  def toAppLevyInterestCharge: AppLevyInterestCharge = AppLevyInterestCharge(amount, dueDate, reference)
}