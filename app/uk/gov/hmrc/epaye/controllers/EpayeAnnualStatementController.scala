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

package uk.gov.hmrc.epaye.controllers

//@Singleton
//case class EpayeAnnualStatementController @Inject() (
//  userProfileConnector: UserProfileAuthConnector,
//  userSessionAuditor: UserSessionAuditor,
//  authConnector: AuthConnector,
//  authenticatedActionProvider: AuthenticatedActionProvider,
//  messagesApi: MessagesApi,
//  epayeService: EpayeService
//)
//  extends FrontendController with Actions with EnrolmentValidation with ProfileConnectivity with I18nSupport {
//
//  private lazy val logger = Logger(getClass)
//
//  def getAnnualStatement(taxYear: TaxYear)(implicit profile: UserProfile, request: Request[_]): Future[Either[AnnualStatementError, AnnualStatement]] = profile.epayeEnrolment match {
//    case Some(enrolment) if profile.showEpayeLandp && profile.epayeRelease3 =>
//      Logger.debug(s"Fetching EPAYE annual statement for enrolment=$enrolment and tax year ${taxYear.asString}")
//      epayeService.getAnnualStatement(enrolment.toEmpRef, taxYear)
//        .aside(obj => logger.debug(s"Epaye charges response=$obj"))
//    case _ =>
//      Logger.debug("No EPAYE enrolment or feature switch is off")
//      Future.successful(Left(AnnualStatementError("No EPAYE enrolment or feature switch is off")))
//  }
//
//  def today(): LocalDate = LocalDate.now()
//  def currentTaxMonth(): TaxMonth =
//    TaxMonth.fromLocalDate(today())
//  def currentTaxYear(): TaxYear =
//    TaxYear(TaxYearResolver.taxYearFor(LocalDate.now()))
//  def currentTaxMonthOpt(taxYear: TaxYear): Option[TaxMonth] =
//    if (taxYear == currentTaxYear()) Some(currentTaxMonth())
//    else None
//
//  def statement(taxYear: TaxYear): Action[AnyContent] =
//    authenticatedActionProvider.authenticate().async { implicit authContext => implicit request =>
//      withProfile(implicit profile => {
//        val futureResponse = getAnnualStatement(taxYear)
//
//        val userMasterDataOptFuture: Future[Option[UserMasterData]] =
//          profile.epayeEnrolment
//            .fold(
//              Future.successful(Option.empty[UserMasterData])
//            )(epayeService.getUserMasterData)
//
//        for {
//          response <- futureResponse
//          userMasterDataOpt <- userMasterDataOptFuture
//        } yield checkEpayeEnrolmentAndPresentPage(
//            epaye_annual_statement(response, taxYear, userMasterDataOpt, currentTaxMonthOpt(taxYear), today())
//          )
//      })
//    }
//}
