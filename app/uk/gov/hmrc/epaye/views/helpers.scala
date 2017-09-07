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

package uk.gov.hmrc.epaye.views

import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.epaye.connectors.UserProfile
import uk.gov.hmrc.epaye.util.PortalUrlBuilder
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.epaye.config.{ExternalUrlConfig, ExternalUrls}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.time.TaxYearResolver

 object helpers {
   val businessTaxAccountUrl: String = ExternalUrls.businessTaxAccountHome
   val businessTaxAccountUrlManage: String = ExternalUrls.businessTaxAccountManageAccount
   val businessTaxAccountUrlMessages: String = ExternalUrls.businessTaxAccountMessages
   val signoutUrl: String = ExternalUrls.signout

  def portalUrl(key: String, year: Int = TaxYearResolver.currentTaxYear)(implicit userProfile: UserProfile, authContext: AuthContext, lang: Lang): String =
    PortalUrlBuilder.buildPortalUrlForYear(key, year)

   def pageTitle(pageName: String)(implicit lang: Lang) = s"$pageName - ${Messages("bt.application.title")} - GOV.UK"

   def govUrl(key: String): String = ExternalUrlConfig.getGovUrl(key)
   def youTubeUrl(key: String): String = ExternalUrlConfig.getYouTubeUrl(key)
   def youTubeId(key: String): String = ExternalUrlConfig.getYouTubeId(key)


   def isEnrolledForSelfAssessment(implicit userProfile: UserProfile): Boolean = userProfile.saEnrolment.isDefined

   def isEnrolledForEpaye(implicit userProfile: UserProfile): Boolean = userProfile.epayeEnrolment.isDefined

   def isEnrolledForCorporationTax(implicit userProfile: UserProfile): Boolean = userProfile.ctEnrolment.isDefined

   def isEnrolledForVat(implicit userProfile: UserProfile): Boolean = userProfile.vatEnrolment.isDefined

   def shouldSeeIformsLinks(implicit userProfile: UserProfile): Boolean = isEnrolledForSelfAssessment(userProfile) || isEnrolledForCorporationTax(userProfile)



 }
