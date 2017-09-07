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

package uk.gov.hmrc.epaye.services

import uk.gov.hmrc.epaye.connectors.HmrcEnrolmentType._
import uk.gov.hmrc.epaye.connectors.{Enrolment, EnrolmentState, HmrcEnrolmentType}

trait EnrolmentsService {
  protected val sortedEnrolmentKeys: List[EnrolmentKey] = List(ATWD, AEOI, CHARITIES, CIS, CORP_TAX, DDES,
    EBTI, EPAYE, EMCS, MERGED_GAMBLING, ICS, MACHINE_GAMES,
    NCTS, NES, PENSION_ADMIN, PENSION_PRAC, PENSION_QROPS, REBATED_OILS, SA, IR_SA_PART_ORG,
    IR_SA_TRUST_ORG, ECW, SDLT, TIED_OILS, VAT_DEC, VAT_VAR, VAT_ECSL,
    ATED, AWRS, VAT_EU_REFUNDS,
    VAT_MOSS_NON_UNION, VAT_MOSS_UNION, VAT_RCSL)

  private def addMergedGamblingEnrolment(enrolments: List[Enrolment]) = {

    val mergedGamblingEnrolment = Enrolment(MERGED_GAMBLING.toString, Nil, EnrolmentState.ACTIVATED)

    val enrolmentKeys = enrolments.collect { case enrolment => enrolment.key }

    val gamblingEnrolments = Seq(GENERAL_BETTING.toString, POOL_BETTING.toString, REMOTE_GAMING.toString)

    val allEnrolments = if (enrolmentKeys.intersect(gamblingEnrolments).nonEmpty) { mergedGamblingEnrolment :: enrolments } else enrolments

    allEnrolments.distinct
  }

  private def sortEnrolments(enrolments: List[Enrolment]): List[Enrolment] = {
    val enrolmentsByKey: Map[String, Enrolment] = enrolments.map(e => (e.key, e)).toMap
    val sortedEnrolments = for (key <- sortedEnrolmentKeys if enrolmentsByKey.contains(key.toString)) yield enrolmentsByKey.get(key.toString).get

    val enrolmentsNotInSortedKeys = enrolments.filter(e => ! sortedEnrolmentKeys.contains(HmrcEnrolmentType.withName(e.key)))

    sortedEnrolments ++ enrolmentsNotInSortedKeys
  }

  def prepareEnrolmentsForBta(enrolments: List[Enrolment]): List[Enrolment] = {
    val mergedEnrolments = addMergedGamblingEnrolment(enrolments)
    sortEnrolments(mergedEnrolments)
  }
}