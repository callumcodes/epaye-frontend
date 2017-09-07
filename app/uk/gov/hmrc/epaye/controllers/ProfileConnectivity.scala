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

import play.api.mvc.{Request, Result}
import uk.gov.hmrc.epaye.connectors.{UserProfile, UserProfileAuthConnector}
import uk.gov.hmrc.epaye.services.UserSessionAuditor
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait ProfileConnectivity {
  val userProfileConnector: UserProfileAuthConnector
  val userSessionAuditor: UserSessionAuditor

  def withProfile(func: UserProfile => Future[Result])(implicit hc: HeaderCarrier, executionContext: ExecutionContext, request: Request[_]): Future[Result] = {
    val futureProfileResponse = userProfileConnector.getUserProfile()
    for {
      profile <- futureProfileResponse
      result <- func(profile)
    } yield {
      userSessionAuditor.auditOncePerSession(profile, result)
    }
  }
}
