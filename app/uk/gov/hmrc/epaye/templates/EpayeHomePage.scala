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

package uk.gov.hmrc.epaye.templates

import org.joda.time.{DateTime, LocalDate}
import play.api.Logger
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.epaye.connectors.UserProfile
import uk.gov.hmrc.epaye.models.domain._
import uk.gov.hmrc.epaye.models.json.HomepageChargesJson
import uk.gov.hmrc.epaye.services.HomepageEpayeChargesError
import uk.gov.hmrc.epaye.views.html.charges.{unallocated_credit, you_owe_zero}
import uk.gov.hmrc.epaye.views.html.class1a._
import uk.gov.hmrc.epaye.views.html.epaye_non_rti_charges
import uk.gov.hmrc.epaye.views.html.interest._
import uk.gov.hmrc.epaye.views.html.landp.{epaye_error_message, epaye_subpage}
import uk.gov.hmrc.epaye.views.html.nonrti._
import uk.gov.hmrc.epaye.views.html.partials.account_summary.landp.monthly_rti


object EpayeHomepage {
  lazy val logger = Logger(getClass)

  def shouldRenderYouOweZero(charges: HomepageChargesJson, rtiCharges: Seq[RtiCharge]): Boolean =
    charges.isEmpty && rtiCharges.isEmpty

  def renderEpaye(epayeCharges: Either[HomepageEpayeChargesError, HomepageChargesJson], rtiCharges: Seq[RtiCharge], currentDateTime: DateTime)(implicit userProfile: UserProfile, m: Messages): Html = {
    epayeCharges match {
      case Right(charges) => showEpayeSubpage(charges, rtiCharges, currentDateTime)
      case Left(_) => showEpayeErrorMessage()
    }
  }

  def showEpayeSubpage(charges: HomepageChargesJson, rtiCharges: Seq[RtiCharge], currentDateTime: DateTime)(implicit userProfile: UserProfile, m: Messages): Html =
    epaye_subpage(charges, rtiCharges, currentDateTime)

  def showEpayeErrorMessage()(implicit m: Messages): Html =
    epaye_error_message()

  def showYouOweZero()(implicit m: Messages): Html = {
    you_owe_zero()
  }

  def renderUnallocatedCredit(response: HomepageChargesJson)(implicit m: Messages): Html = {
    logger.debug(s"Rendering unallocated credit: $response")
    response match {
      case charges =>
        charges.unallocatedCredit.fold(Html(""))(unallocated_credit(_))
      case _ =>
        Html("")
    }
  }

  def renderOneNonRtiCharge(nonRti: NonRtiCharge, localToday: LocalDate)(implicit m: Messages): Html = {
    nonRti match {
      case bill: Class1ABill => class1a_bill(bill)
      case interestOnBill: Class1AInterestOnBill => class1a_interest_on_bill(interestOnBill)
      case penalty: Class1APenalty => class1a_penalty(penalty)
      case interestOnPenalty: Class1AInterestOnPenalty => class1a_interest_on_penalty(interestOnPenalty)
      case interest: InterestOnCisBill => additional_interest_on_cis_bill(interest)
      case interest: InterestOnEmploymentAllowance => additional_interest_on_employment_allowance(interest)
      case interest: InterestOnP11DPenalty => additional_interest_on_p11d_penalty(interest)
      case interest: InterestOnPenalty => additional_interest_on_penalty(interest)
      case interest: InterestOnReturnsAggregate => interest_on_returns(interest)
      case interest: InterestOnEarlierYearUpdate => interest_on_earlier_year_update(interest)
      case credit: Class1ACredit => class1a_credit(credit)

      case credit: NonRtiCredit => nonrti_credit(credit)
      case penalty: NonRtiPenalty => nonrti_penalty(penalty)
      case credit: NonRtiEndOfYearAdjustmentCreditAggregate => nonrti_end_of_year_adjustment_credit(credit)
      case debit: NonRtiEndOfYearAdjustmentDebitAggregate => nonrti_end_of_year_adjustment_debit(debit)
      case interest: NonRtiEndOfYearAdjustmentInterestAggregate => nonrti_end_of_year_adjustment_interest(interest)
      case interest: AppLevyInterest => nonrti_app_levy_interest(interest)

      case credit: InYearManualSpecifiedCreditAggregate => in_year_manual_specified_credit(credit)
      case debit: InYearManualSpecifiedDebitAggregate => in_year_manual_specified_debit(debit)
    }
  }

  def renderRtiCharges(response: HomepageChargesJson, today: LocalDate)(implicit userProfile: UserProfile, m: Messages): Html = {
    logger.debug(s"Rendering charges: $response")
    response match {
      case charges => charges.getRtiCharges match {
        case Nil => Html("")
        case rtiCharges => monthly_rti(rtiCharges)
      }
      case _ =>
        Html("")
    }
  }

  def renderNonRtiCharges(response: HomepageChargesJson, today: LocalDate)(implicit m: Messages): Html = {
    logger.debug(s"Rendering charges: $response")
    response match {
      case charges => charges.getNonRtiCharges match {
        case Nil => Html("")
        case nonRtiCharges => epaye_non_rti_charges(nonRtiCharges, today)
      }
      case _ =>
        Html("")
    }
  }
}

object MessageHasTaxPeriod {
  def unapply(code: Code): Boolean = Code.NonRtiMultiBillPenaltyCodes contains code
}

