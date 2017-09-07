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

package uk.gov.hmrc.epaye

import play.api.libs.json.Json
import uk.gov.hmrc.epaye.models.domain.{AppLevyCharges, AppLevyInterestCharge, TaxYear}
import uk.gov.hmrc.play.controllers.RestFormats

package object connectors {
  //shared
  implicit val designatoryDetailsAddressFormat = Json.format[DesignatoryDetailsAddress]
  implicit val designatoryDetailsTelephoneFormat = Json.format[DesignatoryDetailsTelephone]
  implicit val designatoryDetailsEmailFormat = Json.format[DesignatoryDetailsEmail]
  implicit val designatoryDetailsContactFormat = Json.format[DesignatoryDetailsContact]
  implicit val designatoryDetailsNameFormat = Json.format[DesignatoryDetailsName]
  implicit val designatoryDetailsFormat = Json.format[DesignatoryDetails]

  //ePAYE
  implicit val rtiFormat = Json.format[RTI]
  implicit val nonRtiFormat = Json.format[NonRTI]
  implicit val epayeAccountSummaryFormat = Json.format[EpayeAccountSummary]
  implicit val epayeDesignatoryDetailsCollectionFormat = Json.format[EpayeDesignatoryDetailsCollection]
  implicit val appLevyChargesFormat = Json.format[AppLevyCharges]
  implicit val appLevyInterestChargeFormat = Json.format[AppLevyInterestCharge]

  //Self Assessment or Filing Info
  implicit val taxYearFormat = Json.format[TaxYear]

  //UserProfile

  implicit val enrolmentIdentifierFormat = Json.format[EnrolmentIdentifier]
  implicit val authIdsFormat = Json.format[AuthIds]
  implicit val userDetailFormat = Json.format[UserDetail]
  implicit val credentials = Json.format[Credentials]
  implicit val authAuthorityFormat = {
    implicit val dateFormat = RestFormats.dateTimeFormats
    Json.format[AuthAuthority]
  }
  implicit val authorityFormat = Json.format[Authority]
  implicit val enrolment2Format = Json.format[Enrolment]
  implicit val userProfileFormat = Json.format[UserProfile]
}
