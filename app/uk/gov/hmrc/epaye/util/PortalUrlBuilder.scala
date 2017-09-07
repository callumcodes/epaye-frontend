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

import play.api.{Logger, LoggerLike}
import play.api.i18n.Lang
import uk.gov.hmrc.epaye.FrontendAppConfig
import uk.gov.hmrc.epaye.config.PortalConfig
import uk.gov.hmrc.epaye.connectors.HmrcEnrolmentType.EnrolmentKey
import uk.gov.hmrc.epaye.connectors.{Enrolment, HmrcEnrolmentType, UserProfile}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.time.TaxYearResolver

import scala.annotation.tailrec

trait PortalUrlBuilder {
  lazy val playLangToPortalLang = PortalConfig.playLangToPortalLang
  def log: LoggerLike

  def buildPortalUrlForYear(key: String, year: Int)(implicit userProfile: UserProfile, authContext: AuthContext, lang: Lang): String =
    buildPortalUrl(key, year)

  def buildPortalUrl(destinationPathKey: String, year: Int = TaxYearResolver.currentTaxYear)(implicit userProfile: UserProfile, authContext: AuthContext, lang: Lang): String = {
    val url = resolvePlaceHolder(
      PortalConfig.destinationUrl(destinationPathKey),
      Seq(
        ("<year>", Some(toSaTaxYearRepresentation(year))),
        ("<utr>", userProfile.saEnrolment.map(_.utr)),
        ("<sapartutr>", findIdentifierForEnrolment(HmrcEnrolmentType.IR_SA_PART_ORG)),
        ("<satrustutr>", findIdentifierForEnrolment(HmrcEnrolmentType.IR_SA_TRUST_ORG)),
        ("<vrn>", userProfile.vatEnrolment.map(_.vrn)),
        ("<ctutr>", userProfile.ctEnrolment.map(_.utr)),
        ("<empref>", userProfile.epayeEnrolment.map(_.empRef))
      )
    )
    appendLanguage(url)
  }

  def findIdentifierForEnrolment(enrolmentKey: EnrolmentKey)(implicit userProfile: UserProfile): Option[String] = {
    userProfile.enrolmentMap.get(enrolmentKey) match {
      case Some(Enrolment(_, head :: restOfList, _)) => Some(head.value)
      case _ => None
    }
  }

  def appendLanguage(url: String)(implicit lang: Lang): String = {
    if (FrontendAppConfig.enableLanguageSwitching) {
      val urlWithLang = playLangToPortalLang.get(lang.language) map { portalLang =>
        val paramChar = if (url.contains("?")) "&" else "?"
        s"$url${paramChar}lang=$portalLang"
      }
      urlWithLang getOrElse url
    } else url
  }

  @tailrec
  private def resolvePlaceHolder(url: String, tagsToBeReplacedWithData: Seq[(String, Option[Any])]): String =
    if (tagsToBeReplacedWithData.isEmpty)
      url
    else
      resolvePlaceHolder(replace(url, tagsToBeReplacedWithData.head), tagsToBeReplacedWithData.tail)

  private def replace(url: String, tagToBeReplacedWithData: (String, Option[Any])): String = {
    val (tagName, tagValueOption) = tagToBeReplacedWithData
    tagValueOption match {
      case Some(valueOfTag) => url.replace(tagName, valueOfTag.toString)
      case _ => {
        if (url.contains(tagName)) {
          log.error(s"Failed to populate parameter $tagName in URL $url")
        }
        url
      }
    }
  }

  private[util] def toSaTaxYearRepresentation(taxYear: Int) = {
    val taxYearMinusOne = taxYear - 1
    val lastTwoDigitsThisTaxYear = toLastTwoDigitsString(taxYear)
    val lastTwoDigitsLastTaxYear = toLastTwoDigitsString(taxYearMinusOne)

    s"${lastTwoDigitsLastTaxYear}$lastTwoDigitsThisTaxYear"
  }

  def toLastTwoDigitsString(taxYear: Int): String = {
    taxYear % 100 match {
      case it if it < 10 => s"0$it"
      case it => s"$it"
    }
  }
}

object PortalUrlBuilder extends PortalUrlBuilder {
  override val log = Logger
}
