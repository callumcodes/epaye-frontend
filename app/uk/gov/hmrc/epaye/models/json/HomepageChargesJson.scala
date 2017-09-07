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

package uk.gov.hmrc.epaye.models.json

import uk.gov.hmrc.epaye.models.domain.{NonRtiCharge, RtiCharge, UnallocatedCredit}


/**
  * JSON fetched from EPAYE microservice with all charges
  * to display on the homepage.
  *
  * @param rtiCharges Real Time charges
  * @param nonRtiCharges Non Real Time charges
  */
case class HomepageChargesJson(
                                rtiCharges: Seq[RtiChargesJson] = Seq.empty,
                                nonRtiCharges: Seq[NonRtiChargesJson] = Seq.empty,
                                unallocated: Option[UnallocatedCreditJson] = None
                              ) {
  lazy val getRtiCharges: Seq[RtiCharge] = {
    rtiCharges.flatMap { charge =>
      charge
        .rtiMonthCredit
        .orElse(charge.rtiMonthDebit)
        .orElse(charge.rtiMonthOverdue)
        .orElse(charge.specifiedRtiMonthCredit)
        .orElse(charge.specifiedRtiMonthDebit)
        .orElse(charge.specifiedRtiMonthOverdue)
        .orElse(charge.earlierYearUpdateCredit)
        .orElse(charge.earlierYearUpdateDebit)
        .orElse(charge.earlierYearUpdateOverdue)
    }
  }
  lazy val getNonRtiCharges: Seq[NonRtiCharge] = {
    nonRtiCharges.flatMap { charge =>
      charge
        .class1ABill
        .orElse(charge.class1AInterestOnBill)
        .orElse(charge.class1APenalty)
        .orElse(charge.class1AInterestOnPenalty)
        .orElse(charge.class1ACredit)
        .orElse(charge.interestOnCisBill)
        .orElse(charge.interestOnEmploymentAllowance)
        .orElse(charge.interestOnP11DPenalty)
        .orElse(charge.interestOnPenalty)
        .orElse(charge.interestOnReturnsAggregate)
        .orElse(charge.nonRtiCredit)
        .orElse(charge.nonRtiPenalty)
        .orElse(charge.interestOnEarlierYearUpdates)
        .orElse(charge.nonRtiEndOfYearAdjustmentCreditAggregate)
        .orElse(charge.nonRtiEndOfYearAdjustmentDebitAggregate)
        .orElse(charge.nonRtiEndOfYearAdjustmentInterestAggregate)
        .orElse(charge.appLevyInterest)
        .orElse(charge.inYearManualSpecifiedCreditAggregate)
        .orElse(charge.inYearManualSpecifiedDebitAggregate)
    }
  }
  lazy val unallocatedCredit: Option[UnallocatedCredit] =
    unallocated.map(unallocatedCreditJson => UnallocatedCredit(unallocatedCreditJson.total))
  def isEmpty: Boolean =
    rtiCharges.isEmpty && nonRtiCharges.isEmpty && unallocated.isEmpty
}
