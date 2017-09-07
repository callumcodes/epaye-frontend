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
import play.api.libs.json.JsValue
import Code._

sealed trait Charge

sealed trait NonRtiCharge extends Charge {
  def outstanding: BigDecimal
}

sealed trait SingleNonRtiCharge extends NonRtiCharge {
  def code: Code
}

sealed trait RtiCharge extends Charge

case class AmountBreakdown(appLevy: BigDecimal, interest: BigDecimal)

case class RtiMonthCredit(taxYear:Int, taxMonth: Int, amount: BigDecimal) extends RtiCharge
case class RtiMonthDebit(taxYear:Int, taxMonth: Int, amount: BigDecimal, amountBreakdown: AmountBreakdown, dueDate: LocalDate) extends RtiCharge
case class RtiMonthOverdue(taxYear:Int, taxMonth: Int, amount: BigDecimal, amountBreakdown: AmountBreakdown) extends RtiCharge

case class SpecifiedRtiMonthCredit(taxYear:Int, taxMonth: Int, amount: BigDecimal) extends RtiCharge
case class SpecifiedRtiMonthDebit(taxYear:Int, taxMonth: Int, amount: BigDecimal, interest: BigDecimal, dueDate: LocalDate) extends RtiCharge
case class SpecifiedRtiMonthOverdue(taxYear:Int, taxMonth: Int, amount: BigDecimal, interest: BigDecimal) extends RtiCharge

case class EarlierYearUpdateCredit(taxYear: Int, amount: BigDecimal) extends RtiCharge
case class EarlierYearUpdateDebit(taxYear: Int, amount: BigDecimal, interest: BigDecimal, dueDate: LocalDate) extends RtiCharge
case class EarlierYearUpdateOverdue(taxYear: Int, amount: BigDecimal, interest: BigDecimal) extends RtiCharge

sealed trait WithReference {
  def reference: ChargeReference
}

/**
  * YTA-2794: Class 1A National Insurance charges:
  *
  * https://jira.tools.tax.service.gov.uk/browse/YTA-2794
  */
sealed trait Class1ACharge extends SingleNonRtiCharge
case class Class1ABill(outstanding: BigDecimal, totalOutstanding: BigDecimal, interest: Interest, dueDate: DueDate, reference: ChargeReference) extends WithCode(Class1ABillCode) with Class1ACharge with WithDueDate with WithInterest with WithReference
case class Class1AInterestOnBill(outstanding: BigDecimal, dueDate: DueDate, reference: ChargeReference) extends WithCode(Class1AInterestOnBillCode) with Class1ACharge with WithDueDate with WithReference
case class Class1APenalty(outstanding: BigDecimal, totalOutstanding: BigDecimal, interest: Interest, dueDate: DueDate, reference: ChargeReference) extends WithCode(Class1APenaltyCode) with Class1ACharge with WithInterest with WithDueDate with WithReference
case class Class1AInterestOnPenalty(outstanding: BigDecimal, dueDate: DueDate, reference: ChargeReference) extends WithCode(Class1AInterestOnPenaltyCode) with Class1ACharge with WithDueDate with WithReference
case class Class1ACredit(outstanding: BigDecimal) extends WithCode(Class1ABillCode) with Class1ACharge

sealed trait AdditionalInterestCharge extends NonRtiCharge
case class InterestOnPenalty(code: Code, outstanding: BigDecimal, dueDate: DueDate, reference: ChargeReference) extends AdditionalInterestCharge with WithDueDate with WithReference
case class InterestOnP11DPenalty(code: Code, outstanding: BigDecimal, dueDate: DueDate, reference: ChargeReference) extends AdditionalInterestCharge with WithDueDate with WithReference
case class InterestOnCisBill(code: Code, outstanding: BigDecimal, taxMonth: TaxMonth, dueDate: DueDate, reference: ChargeReference) extends AdditionalInterestCharge with WithDueDate with WithReference
case class InterestOnEmploymentAllowance(code: Code, outstanding: BigDecimal, taxMonth: TaxMonth, dueDate: DueDate, reference: ChargeReference) extends AdditionalInterestCharge with WithDueDate with WithReference
case class InterestOnReturnsAggregate(outstanding: BigDecimal, taxMonth: TaxMonth, dueDate: DueDate, reference: ChargeReference) extends AdditionalInterestCharge with WithDueDate with WithReference

case class NonRtiCredit(code: Code, outstanding: BigDecimal, reference: ChargeReference) extends SingleNonRtiCharge
case class NonRtiPenalty(code: Code, outstanding: BigDecimal, totalOutstanding: BigDecimal, interest: Interest, taxPeriod: TaxPeriod, dueDate: DueDate, reference: ChargeReference) extends SingleNonRtiCharge with WithInterest with WithTaxPeriod with WithDueDate
case class NonRtiEndOfYearAdjustmentCreditAggregate(outstanding: BigDecimal, taxYear: TaxYear) extends NonRtiCharge with WithTaxYear
case class NonRtiEndOfYearAdjustmentDebitAggregate(outstanding: BigDecimal, totalOutstanding: BigDecimal, interest: Interest, taxYear: TaxYear, dueDate: DueDate) extends NonRtiCharge with WithTaxYear with WithInterest with WithDueDate
case class NonRtiEndOfYearAdjustmentInterestAggregate(outstanding: BigDecimal, taxYear: TaxYear, dueDate: DueDate, reference: ChargeReference) extends NonRtiCharge with WithTaxYear with WithDueDate with WithReference

case class InterestOnEarlierYearUpdate(code: Code, outstanding: BigDecimal, taxPeriod: TaxPeriod, dueDate: DueDate, reference: ChargeReference) extends NonRtiCharge with WithDueDate with WithReference

case class UnallocatedCredit(total: BigDecimal) extends Charge

case class AppLevyInterest(outstanding: BigDecimal, taxMonth: TaxMonth, dueDate: DueDate, reference: ChargeReference) extends NonRtiCharge with WithDueDate with WithReference

case class InYearManualSpecifiedCreditAggregate(outstanding: BigDecimal) extends NonRtiCharge
case class InYearManualSpecifiedDebitAggregate(outstanding: BigDecimal, dueDate: DueDate, reference: ChargeReference) extends NonRtiCharge with WithDueDate with WithReference

case class UnknownCharge(detail: JsValue) extends Charge