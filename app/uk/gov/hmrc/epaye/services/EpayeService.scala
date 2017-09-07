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

//TODO
//@Singleton
//case class EpayeService @Inject() (epayeApiClient: EpayeApiBaseClient) {
//
//  lazy val logger = Logger(getClass)
//
//  def getUserMasterData(epayeEnrolment: EpayeEnrolment)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[UserMasterData]] =
//    if (epayeEnrolment.isActivated) {
//      epayeApiClient
//        .getMasterData(epayeEnrolment.toEmpRef)
//        .map(response =>
//          Some(extractUserMasterData(epayeEnrolment.toEmpRef, response)))
//    } else {
//      Future.successful(None)
//    }
//
//  def extractUserMasterData(empRef: EmpRef, apiResponse: ApiResponse[MasterDataJson]): UserMasterData = apiResponse match {
//    case ApiSuccess(MasterDataJson(referenceNumber, yearRegistered)) =>
//      UserMasterData(empRef, referenceNumber, yearRegistered)
//    case _ =>
//      UserMasterData(empRef, None, None)
//  }
//
//  def getHomepageChargesJson(empRef: EmpRef)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[HomepageEpayeChargesError, HomepageChargesJson]] = {
//    epayeApiClient.getHomepageCharges(empRef, hc).map {
//      case ApiSuccess(homepageChargesJson) =>
//        Right(homepageChargesJson)
//      case error =>
//        val errorMessage = error match {
//          case ApiJsonError(_) =>
//            logger.error("Error deserializing Epaye charges JSON")
//            "Error deserializing Epaye charges JSON"
//          case ApiNotFound() =>
//
//            logger.warn("Epaye charges not found")
//            "Epaye charges not found"
//          case other =>
//            logger.error(s"Error fetching data from EPAYE api: ${other.toString}")
//            s"Error fetching data from EPAYE api: ${other.toString}"
//        }
//        Left(HomepageEpayeChargesError(errorMessage))
//    }
//  }
//
//  def getAnnualStatement(empRef: EmpRef, taxYear: TaxYear)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[AnnualStatementError, AnnualStatement]] = {
//    epayeApiClient.getAnnualStatement(empRef, taxYear, hc).map {
//      case ApiSuccess(annualStatement) =>
//        Right(annualStatement)
//      case error =>
//        val errorMessage = error match {
//          case ApiJsonError(_) =>
//            logger.error("Error deserializing Epaye annual statement JSON")
//            "Error deserializing Epaye annual statement JSON"
//          case ApiNotFound() =>
//            logger.warn("Epaye annual statement not found")
//            "Epaye annual statement not found"
//          case other =>
//            logger.error(s"Error fetching data from EPAYE api: ${other.toString}")
//            s"Error fetching data from EPAYE api: ${other.toString}"
//        }
//        Left(AnnualStatementError(errorMessage))
//    }
//  }
//
//  def getMonthlyStatement(empRef: EmpRef, taxYear: TaxYear, taxMonth: TaxMonth)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Either[MonthlyStatementError, MonthlyStatementJson]] = {
//    epayeApiClient.getMonthlyStatement(empRef, taxYear, taxMonth, hc).map {
//      case ApiSuccess(monthlyStatement) =>
//        Right(monthlyStatement)
//      case error =>
//        val errorMessage = error match {
//          case ApiJsonError(_) =>
//            logger.error("Error deserializing Epaye monthly statement JSON")
//            "Error deserializing Epaye monthly statement JSON"
//          case ApiNotFound() =>
//            logger.warn("Epaye monthly statement not found")
//            "Epaye annual statement not found"
//          case other =>
//            logger.error(s"Error fetching data from EPAYE api: ${other.toString}")
//            s"Error fetching data from EPAYE api: ${other.toString}"
//        }
//        Left(MonthlyStatementError(errorMessage))
//    }
//  }
//}

sealed trait EpayeServiceError
case class HomepageEpayeChargesError(message: String) extends EpayeServiceError
case class AnnualStatementError(message: String) extends EpayeServiceError
case class MonthlyStatementError(message: String) extends EpayeServiceError
