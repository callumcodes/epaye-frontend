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

package uk.gov.hmrc.epaye.connectors

import java.net.{URL, URLEncoder}

import org.joda.time.DateTime
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epaye.config.WSHttp
import uk.gov.hmrc.epaye.services.{EPayeLandpPercentagePartitioner, EnrolmentsService, EpayeRelease3PercentagePartitioner, EpayeRelease4PercentagePartitioner}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain.CredentialStrength
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

object UserProfileAuthConnector extends UserProfileAuthConnector with ServicesConfig {
  override def http = WSHttp
  lazy val serviceUrl: String = baseUrl("auth")
}

trait UserProfileAuthConnector extends AuthConnector with EnrolmentsService {

  def getUserProfile()(implicit hc: HeaderCarrier): Future[UserProfile] = {
    val rootUrl = getRootUrl()

    for {
      authority <- http.GET[AuthAuthority](getRootUrl.toString)
      userDetailsUrl = new URL(rootUrl, authority.userDetailsLink)
      enrolmentsUrl = new URL(rootUrl, authority.enrolments)
      idsUrl = new URL(rootUrl, authority.ids)
      userDetails <- http.GET[UserDetail](userDetailsUrl.toString)
      enrolments <- http.GET[List[Enrolment]](enrolmentsUrl.toString)
      ids <- http.GET[AuthIds](idsUrl.toString)
    } yield {
      val hmrcEnrolments = enrolments.filter(en => HmrcEnrolmentType.isEnrolmentKey(en.key))
      val orderedBtaEnrolments = prepareEnrolmentsForBta(hmrcEnrolments)
      UserProfile(userDetails, orderedBtaEnrolments, ids, authority.authorityModel)
    }
  }

  def getRootUrl()(implicit hc: HeaderCarrier): URL = {
    new URL(new URL(serviceUrl), hc.userId.get.value)
  }
}

case class AuthIds(internalId: String)

case class Credentials(gatewayId: Option[String])

sealed case class AuthAuthority(ids: String, enrolments: String, userDetailsLink: String, twoFactorAuthOtpId: Option[String], previouslyLoggedInAt: Option[DateTime], credentialStrength: CredentialStrength, credentials: Credentials) {
  def authorityModel = Authority(twoFactorAuthOtpId, previouslyLoggedInAt, credentialStrength, credentials.gatewayId)
}

case class Authority(twoFactorAuthOtpId: Option[String], previouslyLoggedInAt: Option[DateTime], credentialStrength: CredentialStrength, credId: Option[String] = None) {
  def isRegisteredFor2sv: Boolean = twoFactorAuthOtpId.isDefined
}

case class UserDetail(name: String, email: Option[String], affinityGroup: String, credentialRole: String) {
  lazy val emailTrimmed: Option[String] = email.filterNot(_.trim.isEmpty)
  def isAdmin: Boolean = credentialRole == CredentialRole.USER
  def isOrganisationAdmin: Boolean = isOrganisation && isAdmin
  def isOrganisation: Boolean = affinityGroup == AffinityGroupValue.ORGANISATION
}

case class UserProfile(userDetail: UserDetail, enrolments: List[Enrolment], authIds: AuthIds, authority: Authority) {
  lazy val (activeEnrolments, notActiveEnrolments) = enrolments.partition {
    _.isActivated
  }

  lazy val enrolmentMap: Map[HmrcEnrolmentType.EnrolmentKey, Enrolment] = enrolments.map(e => HmrcEnrolmentType.withName(e.key) -> e).toMap
  lazy val ctEnrolment: Option[CtEnrolment] = enrolmentMap.get(HmrcEnrolmentType.CORP_TAX).map(e =>
    e.identifiers.head match {
      case EnrolmentIdentifier("UTR", utr) => CtEnrolment(utr, e.isActivated)
      case _ => throw new IllegalArgumentException("First Enrolment identifier for CT is not 'UTR'")
    })
  lazy val epayeEnrolment: Option[EpayeEnrolment] = enrolmentMap.get(HmrcEnrolmentType.EPAYE).map(e => {
    val taxOfficeNumber = e.identifiers.find(_.key == "TaxOfficeNumber").getOrElse(throw new IllegalArgumentException("Cannot find 'TaxOfficeNumber' Enrolment identifier for EPAYE")).value
    val taxOfficeReference = e.identifiers.find(_.key == "TaxOfficeReference").getOrElse(throw new IllegalArgumentException("Cannot find 'TaxOfficeReference' Enrolment identifier for EPAYE")).value
    EpayeEnrolment(taxOfficeNumber, taxOfficeReference, e.isActivated)
  })
  lazy val saEnrolment: Option[SaEnrolment] = enrolmentMap.get(HmrcEnrolmentType.SA).map(e => e.identifiers.head match {
    case EnrolmentIdentifier("UTR", utr) => SaEnrolment(utr, e.isActivated)
    case _ => throw new IllegalArgumentException("First Enrolment identifier for SA is not 'UTR'")
  })
  lazy val vatEnrolment: Option[VatEnrolment] = enrolmentMap.get(HmrcEnrolmentType.VAT_DEC).map(e => e.identifiers.head match {
    case EnrolmentIdentifier("VATRegNo", utr) => VatEnrolment(utr, e.isActivated)
    case _ => throw new IllegalArgumentException("First Enrolment identifier for VAT is not 'VATRegNo'")
  })

  def showEpayeLandp: Boolean = epayeEnrolment exists { epaye => EPayeLandpPercentagePartitioner.accept(epaye) }
  def hasMtd: Boolean = enrolmentMap.exists(enrolment => enrolment._1.equals(HmrcEnrolmentType.MTD))
  def hasSa: Boolean = saEnrolment.nonEmpty
  def epayeRelease3: Boolean = showEpayeLandp && (epayeEnrolment exists { epaye => EpayeRelease3PercentagePartitioner.accept(epaye) })
  def epayeRelease4: Boolean = epayeRelease3 && (epayeEnrolment exists { epaye => EpayeRelease4PercentagePartitioner.accept(epaye) })
}

trait TaxEnrolment

case class CtEnrolment(utr: String, isActivated: Boolean) extends TaxEnrolment

case class EpayeEnrolment(taxOfficeNumber: String, taxOfficeReference: String, isActivated: Boolean) extends TaxEnrolment {
  def empRef = s"$taxOfficeNumber/$taxOfficeReference"
  def encodedEmpRef: String = URLEncoder.encode(empRef, "UTF-8")
  def toEmpRef = EmpRef(taxOfficeNumber, taxOfficeReference)
}

case class SaEnrolment(utr: String, isActivated: Boolean) extends TaxEnrolment

case class VatEnrolment(vrn: String, isActivated: Boolean) extends TaxEnrolment

object HmrcEnrolmentType extends Enumeration {
  type EnrolmentKey = Value

  def isEnrolmentKey(key: String): Boolean = values.exists(_.toString == key)

  val SA = Value("IR-SA")
  val IR_SA_PART_ORG = Value("IR-SA-PART-ORG")
  val IR_SA_TRUST_ORG = Value("IR-SA-TRUST-ORG")
  val AWRS = Value("HMRC-AWRS-ORG")
  val ATED = Value("HMRC-ATED-ORG")
  val VAT_DEC = Value("HMCE-VATDEC-ORG")
  val VAT_VAR = Value("HMCE-VATVAR-ORG")
  val EPAYE = Value("IR-PAYE")
  val CORP_TAX = Value("IR-CT")
  val VAT_RCSL = Value("HMCE-VATRSL-ORG")
  val VAT_MOSS_UNION = Value("HMRC-MOSS-U-ORG")
  val VAT_MOSS_NON_UNION = Value("HMRC-MOSSNU-ORG")
  val VAT_ECSL = Value("HMCE-ECSL-ORG")
  val VAT_EU_REFUNDS = Value("HMRC-EU-REF-ORG")
  val VAT_NOVA = Value("HMRC-NOVA-ORG")
  val PENSION_PRAC = Value("HMRC-PP-ORG")
  val PENSION_ADMIN = Value("HMRC-PSA-ORG")
  val PENSION_QROPS = Value("HMRC-QROPS-ORG")
  val MACHINE_GAMES = Value("HMRC-MGD-ORG")
  val GENERAL_BETTING = Value("HMRC-GTS-GBD")
  val POOL_BETTING = Value("HMRC-GTS-PBD")
  val REMOTE_GAMING = Value("HMRC-GTS-RGD")
  val AEOI = Value("HMRC-FATCA-ORG")
  val CHARITIES = Value("HMRC-CHAR-ORG")
  val ATWD = Value("HMCE-ATWD-ORG")
  val SDLT = Value("IR-SDLT-ORG")
  val REBATED_OILS = Value("HMCE-RO")
  val TIED_OILS = Value("HMCE-TO")
  val EMCS = Value("HMRC-EMCS-ORG")
  val ICS = Value("HMRC-ICS-ORG")
  val DDES = Value("HMCE-DDES")
  val NCTS = Value("HMCE-NCTS-ORG")
  val EBTI = Value("HMCE-EBTI-ORG")
  val NES = Value("HMCE-NES")
  val CIS = Value("HMRC-CIS-ORG")
  val ECW = Value("HMRC-ECW-IND")
  val EI = Value("HMRC-EI-ORG")
  val MLR = Value("HMRC-MLR-ORG")
  val PAYERS = Value("HMRC-PAYERS-ORG")
  val SBI = Value("HMRC-SBI-ORG")
  val SET_AS_1 = Value("HMRC-SET-AS-1")
  val SET_ORG = Value("HMRC-SET-ORG")
  val VAT_GIANT = Value("HMRC-VAT-GIANT")
  val CTF = Value("IR-CTF")

  val VAT_AGNT = Value("HMCE-VAT-AGNT")
  val AGENT_AGENT = Value("HMRC-AGENT-AGENT")
  val CHAR_AGENT = Value("HMRC-CHAR-AGENT")
  val GTS_AGENT = Value("HMRC-GTS-AGNT")
  val MGD_AGENT = Value("HMRC-MGD-AGNT")
  val NOVRN_AGENT = Value("HMRC-NOVRN-AGNT")
  val CT_AGENT = Value("IR-CT-AGENT")
  val PAYE_AGENT = Value("IR-PAYE-AGENT")
  val SA_AGENT = Value("IR-SA-AGENT")
  val SDLT_AGENT = Value("IR-SDLT-AGENT")

  val MERGED_GAMBLING = Value("HMRC-GTS-MERGED")

  val MTD = Value("HMRC-MTD-IT")
}

case class EnrolmentIdentifier(key: String, value: String)

case class Enrolment(key: String, identifiers: Seq[EnrolmentIdentifier], state: String) {
  val isActivated: Boolean = state == EnrolmentState.ACTIVATED

  lazy val getEnrolmentWithTypeTuple: (HmrcEnrolmentType.Value, Boolean) = (HmrcEnrolmentType.withName(key), isActivated)
}

object AffinityGroupValue {
  val INDIVIDUAL = "Individual"
  val ORGANISATION = "Organisation"
  val AGENT = "Agent"
}

object EnrolmentState {
  val ACTIVATED = "Activated"
  val NOT_YET_ACTIVATED = "NotYetActivated"
}

object CredentialRole {
  //Admin is also known as User. Admin is a platform name, User is the GG name.
  val USER = "User"
  val ASSISTANT = "Assistant"
}
