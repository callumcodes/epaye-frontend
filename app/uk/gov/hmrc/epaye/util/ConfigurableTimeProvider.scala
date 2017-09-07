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

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.Logger
import play.api.mvc.Request
import uk.gov.hmrc.epaye.FrontendAppConfig

import scala.util.Try


trait ConfigurableTimeProvider {
  val formatter = DateTimeFormat.forPattern("dd/MM/yyy-HH:mm")

  def currentDateTime(implicit request: Request[_]) = {
    val dateInCookie  = for {
      c <- request.cookies.get(ConfigurableTimeHeaderCarrier.cookieName) if FrontendAppConfig.isTimeManipulationEnabled
      d <- Try(formatter.parseDateTime(c.value)).toOption
    } yield d

    Logger.debug(s"ConfigurableTimeProvider: [EPAYE config enabled: ${FrontendAppConfig.isTimeManipulationEnabled}], [Cookie: ${request.cookies.get(ConfigurableTimeHeaderCarrier.cookieName)}]")
    dateInCookie.getOrElse(DateTime.now)
  }
}

object ConfigurableTimeProvider extends ConfigurableTimeProvider
