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

import play.api.Play
import uk.gov.hmrc.play.config.RunMode


object ExternalUrls extends RunMode {
  import play.api.Play.current

  val companyAuthHost = s"${Play.configuration.getString(s"govuk-tax.$env.company-auth.host").getOrElse("")}"

  val businessTaxAccountHost = s"${Play.configuration.getString(s"govuk-tax.$env.business-tax-account.host").getOrElse("")}"

  val signInWithoutContinue = s"$companyAuthHost/gg/sign-in"

  def signInWithContinue(url: String) = s"$signInWithoutContinue?continue=$url"

  def signOutWithContinue(url: String) = s"$companyAuthHost/gg/sign-out?continue=$url"

  val businessTaxAccountHome= s"$businessTaxAccountHost/business-account"

  val businessTaxAccountManageAccount: String = s"$businessTaxAccountHost/manage-account"

  val businessTaxAccountMessages: String = s"$businessTaxAccountHost/messages"

  val signout: String = s"$businessTaxAccountHost/signout"

}

class ConfigNotFoundException(val configKey: String) extends RuntimeException(s"Config $configKey not found.")