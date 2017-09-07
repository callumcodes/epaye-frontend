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

import play.api.i18n.{Lang, Messages}
import play.api.mvc.{Request, Results}
import play.twirl.api.Html
import uk.gov.hmrc.epaye.connectors.UserProfile
import uk.gov.hmrc.epaye.views.html.not_enrolled
import uk.gov.hmrc.play.frontend.auth.AuthContext
import play.api.i18n.Messages.Implicits._
import play.api.Play.current



trait EnrolmentValidation {
  this: Results =>

  private def checkContextForEnrolmentAndPresentPage[T](htmlPage: => Html, hasEnrolment: Boolean, enrolmentName: String)(implicit userProfile: UserProfile, authContext: AuthContext, request: Request[_], lang: Lang) = {
    Ok(if (hasEnrolment) htmlPage else not_enrolled(Some(enrolmentName)))
  }

  def isEnrolledForEpaye(implicit userProfile: UserProfile): Boolean = userProfile.epayeEnrolment.isDefined


  def checkEpayeEnrolmentAndPresentPage(htmlPage: => Html)(implicit userProfile: UserProfile, authContext: AuthContext, request: Request[_], lang: Lang) = {
    checkContextForEnrolmentAndPresentPage(htmlPage, isEnrolledForEpaye, Messages("service.name.ir-paye"))
  }


  def checkEnrolmentAndPresentPageForTax(htmlPage: => Html, taxName: Option[String])(implicit userProfile: UserProfile, authContext: AuthContext, request: Request[_], lang: Lang) = taxName
  match {
    case Some("epaye") => checkEpayeEnrolmentAndPresentPage{htmlPage}
    case _ => Ok(htmlPage)
  }

}