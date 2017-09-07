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

package uk.gov.hmrc.epaye.controllers

import play.api.mvc.Request
import uk.gov.hmrc.play.frontend.auth.{Actions, AuthContext}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.epaye.connectors.{UserProfile, UserProfileAuthConnector}
import uk.gov.hmrc.epaye.models.EpayeModel
import uk.gov.hmrc.epaye.services.{EpayeConnectivity, EpayeLandpModel, UserSessionAuditor}
import uk.gov.hmrc.epaye.util.ConfigurableTimeProvider
import uk.gov.hmrc.epaye.views.html._
import uk.gov.hmrc.epaye.views.html.landp.{epaye_breakdown_no_levy, epaye_pay_charge, epaye_pay_charge_bank_transfer}

import scala.concurrent.Future

trait EpayeController extends FrontendController with Actions with EnrolmentValidation with ProfileConnectivity {

  val authenticatedActionProvider: AuthenticatedActionProvider
  val userProfileConnector: UserProfileAuthConnector

  val epayeConnectivity: EpayeConnectivity

  def endOfYear() = authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
    withProfile { implicit profile =>
      Future(checkEpayeEnrolmentAndPresentPage { epaye_end_of_tax_year() })
    }
  }

  def taxMonthCodes() = authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
    withProfile(implicit profile => Future(checkEpayeEnrolmentAndPresentPage { epaye_tax_month_codes() }))
  }

  def getStarted() = authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
    withProfile(implicit profile => Future(checkEpayeEnrolmentAndPresentPage { epaye_get_started() }))
  }

  def messages() = authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
    withProfile(implicit profile => Future(checkEpayeEnrolmentAndPresentPage { epaye_messages() }))
  }

  def benefitsAndExpenses() = authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
    withProfile(implicit profile => Future(checkEpayeEnrolmentAndPresentPage { epaye_benefits_and_expenses() }))
  }

  def remove() = authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
    withProfile(implicit profile => Future(checkEpayeEnrolmentAndPresentPage { epaye_remove() }))
  }

  def subpage() = authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
    withProfile(implicit profile => {
      profile.epayeRelease3 match {
        case true =>  Future.successful(Redirect("/business-account"))
        case false => renderSubpage()
      }

    })
  }

  def renderSubpage()(implicit userProfile: UserProfile, authContext: AuthContext, request: Request[AnyRef]) = {
    val currentDateTime = ConfigurableTimeProvider.currentDateTime(request)

    val epayeModel: Future[EpayeModel] = epayeConnectivity.fetchEpayeModel(userProfile.epayeEnrolment)
    val epayeLModel: Future[Option[EpayeLandpModel]] = epayeConnectivity.fetchEpayeLandpModel(userProfile.epayeEnrolment)

    for {
      model <- epayeModel
      landPModel <- epayeLModel
    } yield {
      checkEpayeEnrolmentAndPresentPage(epaye_subpage(landPModel, model, currentDateTime))
    }
  }

  def payCharge(xref: String) = authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
    withProfile(implicit profile => Future(Ok(epaye_pay_charge(xref))))
  }

  def payChargeBankTransfer(xref: String) = authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
    withProfile(implicit profile => Future(Ok(epaye_pay_charge_bank_transfer(xref))))
  }

  def breakdownNoLevy() = authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
    withProfile(implicit profile => Future(Ok(epaye_breakdown_no_levy())))
  }

  def changeInEmployeeCircumstances() =
    authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
      withProfile { implicit profile =>
        Future(Ok(epaye_change_in_employee_circumstances()))
      }
    }
}

object EpayeController extends EpayeController {
  override lazy val authenticatedActionProvider = AuthenticatedActionProvider
  implicit override val authConnector = UserProfileAuthConnector
  override val epayeConnectivity: EpayeConnectivity = EpayeConnectivity
  override val userProfileConnector = UserProfileAuthConnector
  override val userSessionAuditor = UserSessionAuditor
}
