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

package uk.gov.hmrc.epaye.util

import play.api.Logger
import play.api.mvc.Request
import uk.gov.hmrc.epaye.FrontendAppConfig
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.language.implicitConversions

case class ConfigurableTimeHeaderCarrier(headerCarrier: HeaderCarrier)

object ConfigurableTimeHeaderCarrier {
  val cookieName = "configurable_date_time"
  val headerName = "configurable-date-time"

  implicit def fromHeaderCarrier(implicit request: Request[_], headerCarrier: HeaderCarrier): ConfigurableTimeHeaderCarrier = {
    val hc = request.cookies.get(cookieName) match {
      case Some(cookie) if FrontendAppConfig.isTimeManipulationEnabled =>
        Logger.debug(s"Adding '$headerName' header to header carrier: [${cookie.value}]")
        headerCarrier.withExtraHeaders(headerName -> cookie.value)
      case _ => headerCarrier
    }
    ConfigurableTimeHeaderCarrier(hc)
  }

  implicit def toHeaderCarrier(implicit configurableTimeHeaderCarrier: ConfigurableTimeHeaderCarrier): HeaderCarrier = configurableTimeHeaderCarrier.headerCarrier
}
