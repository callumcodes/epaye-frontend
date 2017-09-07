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

package uk.gov.hmrc.epaye.services

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.epaye.config.FrontendAuditConnector
import uk.gov.hmrc.epaye.connectors.UserProfile
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext

case class UserSessionAuditEvent(saUtr: Option[String], ctUtr: Option[String], vrn: Option[String], empRef: Option[String])

object UserSessionAuditEvent {
  implicit val formats = Json.format[UserSessionAuditEvent]
}

trait UserSessionAuditor extends AuditingTags {
  val auditConnector: AuditConnector
  val sessionKey = "userSessionMetricAudited"
  val auditSource = "business-tax-account"
  val auditType = "user-session-visit"

  def auditOncePerSession(userProfile: UserProfile, result: Result)(implicit request: Request[_], executionContext: ExecutionContext, hc: HeaderCarrier): Result = {
    val requestMarker = request.session.get(sessionKey)
    val resultMarker = result.session.get(sessionKey)
    (requestMarker, resultMarker) match {
      case (None, None) =>
        val saUtr = userProfile.saEnrolment map { _.utr }
        val ctUtr = userProfile.ctEnrolment map { _.utr }
        val vrn = userProfile.vatEnrolment map { _.vrn }
        val empRef = userProfile.epayeEnrolment map { _.empRef }
        val eventDetail = UserSessionAuditEvent(saUtr, ctUtr, vrn, empRef)
        FrontendAuditConnector.sendEvent(ExtendedDataEvent(auditSource = auditSource, auditType = auditType, detail = Json.toJson(eventDetail), tags = buildTags()))
        result.withSession(request.session + (sessionKey -> "true"))
      case (None, Some(_)) =>
        Logger.warn("Request passed to oncePerSession method more than once: uri: " + request.uri)
        result
      case _ =>
        result
    }
  }
}

object UserSessionAuditor extends UserSessionAuditor {
  override val auditConnector = FrontendAuditConnector
}