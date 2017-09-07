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

case class DesignatoryDetails(name: DesignatoryDetailsName, address: Option[DesignatoryDetailsAddress] = None,
                              contact: Option[DesignatoryDetailsContact] = None)

case class DesignatoryDetailsName(nameLine1: Option[String] = None, nameLine2: Option[String] = None)

case class DesignatoryDetailsAddress(addressLine1: Option[String] = None,
                                     addressLine2: Option[String] = None,
                                     addressLine3: Option[String] = None,
                                     addressLine4: Option[String] = None,
                                     addressLine5: Option[String] = None,
                                     postcode: Option[String] = None,
                                     foreignCountry: Option[String] = None
                                    )

case class DesignatoryDetailsContact(telephone: Option[DesignatoryDetailsTelephone] = None, email: Option[DesignatoryDetailsEmail] = None)

case class DesignatoryDetailsTelephone(daytime: Option[String] = None, fax: Option[String] = None, evening: Option[String] = None, mobile: Option[String] = None, telephoneNumber: Option[String] = None)

case class DesignatoryDetailsEmail(primary: Option[String] = None)