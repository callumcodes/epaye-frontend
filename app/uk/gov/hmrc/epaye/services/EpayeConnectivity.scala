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

import play.api.Logger
import play.api.mvc.Request
import uk.gov.hmrc.epaye.connectors.{EpayeAccountSummary, EpayeConnector, EpayeEnrolment, UserProfile}
import uk.gov.hmrc.epaye.models._
import uk.gov.hmrc.epaye.models.domain._
import uk.gov.hmrc.play.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._


import scala.concurrent.Future

object EpayeConnectivity extends EpayeConnectivity {
  override val epayeConnector = EpayeConnector
}

trait EpayeConnectivity {
  val epayeConnector: EpayeConnector
  val logger = Logger(getClass)

  def fetchEpayeModel(epayeEnrolmentOpt: Option[EpayeEnrolment])(implicit headerCarrier: HeaderCarrier): Future[EpayeModel] = {
    epayeEnrolmentOpt match {
      case Some(e @ EpayeEnrolment(_, _, true)) =>
        epayeConnector.accountSummary(e).map {
          case Some(accountSummary @ EpayeAccountSummary(_, _)) => EpayeAccountSummaryModel(accountSummary)
          case None => EpayeAccountSummaryNoData
        }.recover {
          case _: NotFoundException => EpayeAccountSummaryNoData
          case ex: Exception => EpayeGenericError(ex)
        }
      case _ => Future.successful(EpayeEmpty)
    }
  }

  def designatoryDetails(epayeEnrolment: EpayeEnrolment)(implicit headerCarrier: HeaderCarrier) = {
    epayeConnector.designatoryDetails(epayeEnrolment).recover {
      case e =>
        logger.warn(s"Failed to fetch epaye designatory details with message - ${e.getMessage}")
        None
    }
  }

  def fetchEpayeLandpModel(epayeEnrolmentOpt: Option[EpayeEnrolment])(implicit headerCarrier: HeaderCarrier, request: Request[_], userProfile: UserProfile): Future[Option[EpayeLandpModel]] = {
    epayeEnrolmentOpt collect {
      case epaye if epaye.isActivated && userProfile.showEpayeLandp => {
        val charges = epayeConnector.charges(epaye)
        val appLevyChargesFuture = charges.map(_.flatMap(_.appLevyCharges.map(_.toAppLevyCharges)))
        val appLevyInterestChargesFuture = charges.map(_.map(_.appLevyInterestCharges.map(_.toAppLevyInterestCharge)))
        val rtiChargesFuture = charges.map(_.map(_.rtiCharges.flatMap(_.toRtiCharge)))
        for {
          appLevyChargesOpt <- appLevyChargesFuture
          appLevyInterestCharges <- appLevyInterestChargesFuture
          rtiCharges <- rtiChargesFuture
          outstandingAmount = appLevyChargesOpt.map(_.aggregatedOutstandingAmount).getOrElse(BigDecimal("0"))
          accruedInterest = appLevyChargesOpt.map(_.aggregatedAccruedInterest).getOrElse(BigDecimal("0"))
          interestCharges = appLevyInterestCharges.getOrElse(Nil)
        } yield {
          val prefix = "fetchEpayeLandpModel: received:"
          logger.debug(s"$prefix ${appLevyChargesOpt.fold("no")(_ => "")} app levy charge")
          logger.debug(s"$prefix ${appLevyInterestCharges.getOrElse(Nil).length} app levy interest charges")
          logger.debug(s"$prefix ${rtiCharges.getOrElse(Nil).length} RTI charges")
          Some(EpayeLandpModel(outstandingAmount, accruedInterest, interestCharges, rtiCharges.getOrElse(Nil)))
        }
      }.recover {
        case e: Exception => {
          logger.debug(s"fetchEpayeLandpModel: exception when retrieving EPAYE charges:${e.getMessage}", e)
          None
        }
      }
    } getOrElse {
      logger.debug(s"fetchEpayeLandpModel: retrieving skipped:${epayeEnrolmentOpt.exists(_.isActivated)} ${userProfile.showEpayeLandp}")
      Future.successful(None)
    }
  }
}

case class EpayeLandpModel(
                            appLevyOutstandingAmount: BigDecimal,
                            appLevyAccruedInterest: BigDecimal,
                            appLevyInterestCharges: Seq[AppLevyInterestCharge],
                            rtiCharges: Seq[RtiCharge]
                          )
