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

package uk.gov.hmrc.epaye.connectors.http

import play.api.libs.json.Reads
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpReads, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


private object Utils {
  private def unexpectedOrUnprocessableResponse(uri: String, response: HttpResponse): Throwable = {
    response.status match {
      case 422 => new UnprocessableEntityException(s"Unprocessable entity trying to hit $uri", response)
      case _ => new MicroServiceException(s"Unexpected response status: ${response.status} (possible further details: ${response.body}) for call to $uri", response)
    }
  }

  def readOptionOf[P](uri: String)(implicit rds: HttpReads[P]): HttpReads[Option[P]] = new HttpReads[Option[P]] {
    override def read(method: String, url: String, response: HttpResponse) = response.status match {
      case 200 => Some(rds.read(method, url, response))
      case 204 | 404 => None
      case _ => throw unexpectedOrUnprocessableResponse(url, response)
    }
  }

  def readTryOf[T](uri: String)(implicit rds: HttpReads[T]): HttpReads[Try[Option[T]]] = new HttpReads[Try[Option[T]]] {
    override def read(method: String, url: String, response: HttpResponse) = response.status match {
      case 200 => Success(Some(rds.read(method, url, response)))
      case 204 | 404 => Success(None)
      case _ => Failure(unexpectedOrUnprocessableResponse(url, response))
    }
  }
}

case class MicroServiceException(message: String, response: HttpResponse) extends Exception(message)

case class UnprocessableEntityException(message: String, response: HttpResponse) extends Exception(message)

package object implicits {
  implicit class GetSugar(val get: HttpGet) extends AnyVal {
    def getOptional[T](uri: String)(implicit ec: ExecutionContext, mf: Manifest[T], rds: Reads[T], hc: HeaderCarrier): Future[Option[T]] = get.GET[Option[T]](uri)(Utils.readOptionOf(uri), hc)
    def getTry[T](uri: String)(implicit ec: ExecutionContext, rds: HttpReads[T], hc: HeaderCarrier): Future[Try[Option[T]]] = get.GET[Try[Option[T]]](uri)(Utils.readTryOf(uri), hc)
  }
}
