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
import play.api.mvc.Results._
import uk.gov.hmrc.epaye.FrontendAppConfig
import uk.gov.hmrc.epaye.config.ExternalUrls
import uk.gov.hmrc.epaye.util.RequestUtils
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Account, Accounts}
import uk.gov.hmrc.play.frontend.auth.{GovernmentGateway, TaxRegime}

import scala.concurrent.Future

trait GovernmentGatewayTaxRegime extends TaxRegime {
  def account(accounts: Accounts): Option[Account]

  def isAuthorised(accounts: Accounts) = account(accounts).isDefined
  val authenticationType = CompanyAuthGovernmentGateway
}


object EpayeRegime extends GovernmentGatewayTaxRegime {
  def account(accounts: Accounts) = accounts.epaye
}


object CompanyAuthGovernmentGateway extends GovernmentGateway {
  override def redirectToLogin(implicit request: Request[_]) = {
    val continueUri = RequestUtils.getRequestUri(request, !FrontendAppConfig.loginContinueRelativeUrls)
    val signInUri = ExternalUrls.signInWithContinue(continueUri)
    Future.successful(Redirect(signInUri))
  }

  override def continueURL: String = ???

  override def loginURL: String = ExternalUrls.signInWithoutContinue
}