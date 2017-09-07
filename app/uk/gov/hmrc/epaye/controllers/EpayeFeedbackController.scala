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

//@Singleton
//class EpayeFeedbackController @Inject() (
//  auditConnector: AuditConnector,
//  val authConnector: AuthConnector,
//  val messagesApi: MessagesApi
//)
//  extends FrontendController
//  with Actions
//  with I18nSupport {
//
//  def getFeedbackPage(surveyName: String) = Action { implicit request =>
//    Ok(epaye_feedback(EpayeFeedbackForm.form, surveyName))
//  }
//
//  def submitFeedback(surveyName: String) = Action.async { implicit request =>
//    Future.successful {
//      EpayeFeedbackForm.form.bindFromRequest().fold(
//        errors => {
//          BadRequest(epaye_feedback(errors, surveyName))
//        },
//        data => {
//          auditConnector.sendEvent(buildAuditEvent(data, surveyName))
//          Redirect(routes.EpayeFeedbackController.thankYou())
//        }
//      )
//    }
//  }
//
//  def thankYou() = Action { implicit request =>
//    Ok(epaye_feedback_thankyou())
//  }
//
//  private def buildAuditEvent(data: EpayeFeedbackForm, surveyName: String)(implicit request: Request[_], hc: HeaderCarrier) =
//    DataEvent(
//      auditSource = "business-tax-account",
//      auditType = if (surveyName.startsWith(ExitSurvey)) ExitSurveyAuditType else BetaSurveyAuditType,
//      tags = hc.headers.toMap,
//      detail = data.toMap
//    )
//}
