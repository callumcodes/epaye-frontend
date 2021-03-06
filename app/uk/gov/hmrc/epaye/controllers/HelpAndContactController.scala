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

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.epaye.connectors.UserProfileAuthConnector
import uk.gov.hmrc.epaye.services.UserSessionAuditor
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

//TODO
trait HelpAndContactController extends FrontendController with EnrolmentValidation with ProfileConnectivity {
  def authenticatedActionProvider: AuthenticatedActionProvider

  def helpAndContactWithSubpage(content: String): Action[AnyContent] = authenticatedActionProvider.authenticate().async {
    implicit user =>
      implicit request =>
        Future.successful(Ok)
  }
}

object HelpAndContactController extends HelpAndContactController {
  override def authenticatedActionProvider = AuthenticatedActionProvider

  override val userProfileConnector = UserProfileAuthConnector
  override val userSessionAuditor = UserSessionAuditor
}

