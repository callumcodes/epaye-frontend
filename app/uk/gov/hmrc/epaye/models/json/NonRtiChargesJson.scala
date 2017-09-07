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

import uk.gov.hmrc.epaye.models.domain._

case class NonRtiChargesJson(
  class1ABill: Option[Class1ABill] = None,
  class1AInterestOnBill: Option[Class1AInterestOnBill] = None,
  class1APenalty: Option[Class1APenalty] = None,
  class1AInterestOnPenalty: Option[Class1AInterestOnPenalty] = None,
  class1ACredit: Option[Class1ACredit] = None,
  interestOnPenalty: Option[InterestOnPenalty] = None,
  interestOnP11DPenalty: Option[InterestOnP11DPenalty] = None,
  interestOnCisBill: Option[InterestOnCisBill] = None,
  interestOnEmploymentAllowance: Option[InterestOnEmploymentAllowance] = None,
  interestOnReturnsAggregate: Option[InterestOnReturnsAggregate] = None,
  nonRtiCredit: Option[NonRtiCredit] = None,
  nonRtiPenalty: Option[NonRtiPenalty] = None,
  interestOnEarlierYearUpdates: Option[InterestOnEarlierYearUpdate] = None,
  nonRtiEndOfYearAdjustmentCreditAggregate: Option[NonRtiEndOfYearAdjustmentCreditAggregate] = None,
  nonRtiEndOfYearAdjustmentDebitAggregate: Option[NonRtiEndOfYearAdjustmentDebitAggregate] = None,
  nonRtiEndOfYearAdjustmentInterestAggregate: Option[NonRtiEndOfYearAdjustmentInterestAggregate] = None,
  appLevyInterest: Option[AppLevyInterest] = None,
  inYearManualSpecifiedCreditAggregate: Option[InYearManualSpecifiedCreditAggregate] = None,
  inYearManualSpecifiedDebitAggregate: Option[InYearManualSpecifiedDebitAggregate] = None
)

