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

import play.api.Logger
import play.api.libs.json.Json
import uk.gov.hmrc.epaye.connectors.http.MicroServiceException
import uk.gov.hmrc.epaye.models.json.EpayeResponseJson
import uk.gov.hmrc.epaye.FrontendAppConfig
import uk.gov.hmrc.epaye.config.WSHttp
import uk.gov.hmrc.epaye.util.ConfigurableTimeHeaderCarrier
import uk.gov.hmrc.epaye.util.ConfigurableTimeHeaderCarrier.toHeaderCarrier
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future

// TODO: move into [[EpayeApiClient]]
object EpayeConnector extends EpayeConnector with ServicesConfig {
  override val http = WSHttp
  override val epayeUrl = baseUrl("epaye")
}

trait EpayeConnector {

  val logger = Logger(getClass())

  def epayeUrl: String

  def http: HttpGet

  def accountSummary(enrolment: EpayeEnrolment)(implicit hc: HeaderCarrier): Future[Option[EpayeAccountSummary]] = {
    if (FrontendAppConfig.epayeUseOdsApi) {
      val uri = s"/epaye/${enrolment.encodedEmpRef}/account-summary"
      http.GET[Option[EpayeAccountSummary]](epayeUrl + uri)(handleResponse(uri), hc)
    }
    else {
      logger.debug("obtaining account summary via ODS API has been turned off - returning empty result")
      Future.successful(None)
    }
  }

  private def handleResponse(uri: String): HttpReads[Option[EpayeAccountSummary]] = new HttpReads[Option[EpayeAccountSummary]] {
    def read(method: String, url: String, response: HttpResponse) = response.status match {
      case 200 => Some(response.json.as[EpayeAccountSummary])
      case 204 => None
      case 404 => throw new NotFoundException(response.body)
      case _ => throw new MicroServiceException(s"Unexpected response status: ${response.status} (possible further details: ${response.body}) for call to $uri", response)
    }
  }

  def designatoryDetails(enrolment: EpayeEnrolment)(implicit hc: HeaderCarrier) = {
    val uri = s"/epaye/${enrolment.encodedEmpRef}/designatory-details"
    http.GET[Option[EpayeDesignatoryDetailsCollection]](epayeUrl + uri)
  }

  def charges(enrolment: EpayeEnrolment)(implicit hc: ConfigurableTimeHeaderCarrier): Future[Option[EpayeResponseJson]] = {
    val uri = s"/epaye/${enrolment.encodedEmpRef}/charges"
    http.GET[Option[EpayeResponseJson]](epayeUrl + uri)
  }

}

case class EpayeDesignatoryDetailsCollection(employer: Option[DesignatoryDetails] = None, communication: Option[DesignatoryDetails] = None)

case class EpayeAccountSummary(rti: Option[RTI] = None, nonRti: Option[NonRTI] = None)

case class RTI(balance: BigDecimal)

object RTI {
  implicit val format = Json.format[RTI]
}

case class NonRTI(paidToDate: BigDecimal, currentTaxYear: Int)

object NonRTI {
  implicit val format = Json.format[NonRTI]
}

