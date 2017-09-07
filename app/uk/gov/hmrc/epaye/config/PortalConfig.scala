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

package uk.gov.hmrc.epaye.config

import play.api.Configuration
import uk.gov.hmrc.play.config.RunMode._
import scala.collection.JavaConverters.asScalaBufferConverter


object PortalConfig {

  import play.api.Play.{configuration, current}

  def portalUrl() = configuration.getString(s"govuk-tax.$env.portal.url").getOrElse(throw new ConfigNotFoundException(s"govuk-tax.$env.portal.url"))

  def destinationUrl(pathKey: String): String = getPath(pathKey)

  def playLangToPortalLang: Map[String, String] = {
    val configs = configuration.getConfigList(s"govuk-tax.portal.lang.mappings").getOrElse(throw new ConfigNotFoundException("govuk-tax.portal.lang.mappings"))
    configs.asScala.map { parseLangMapping }.toMap
  }

  private def parseLangMapping(configuration: Configuration): (String, String) = {
    (configuration.getString("play.lang"), configuration.getString("portal.lang")) match {
      case (Some(playLang), Some(portalLang)) => playLang -> portalLang
      case _ => throw new IllegalStateException(s"Failed to parse play -> portal language mapping from: $configuration")
    }
  }

  private def getPath(pathKey: String): String = portalUrl + configuration.getString(s"govuk-tax.portal.destinationPath.$pathKey")
    .getOrElse(throw new ConfigNotFoundException(s"govuk-tax.portal.destinationPath.$pathKey"))
}

object ExternalUrlConfig {

  import play.api.Play.{configuration, current}

  def getGovUrl(key: String): String = getPath(s"govuk-tax.external.gov.$key")

  private def getPath(key: String): String = configuration.getString(key).getOrElse(throw new ConfigNotFoundException(key))

  def getYouTubeId(key: String): String = getPath(s"govuk-tax.external.youtube.$key")
  def getYouTubeUrl(key: String): String = s"https://www.youtube.com/watch?v=${getYouTubeId(key)}"
}