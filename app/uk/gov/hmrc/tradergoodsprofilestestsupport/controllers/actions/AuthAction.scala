/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.tradergoodsprofilestestsupport.controllers.actions

import play.api.Logging
import play.api.mvc.Results.Forbidden
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthAction @Inject()(
                            val authConnector: AuthConnector,
                            val parser: BodyParsers.Default
                          )(implicit val executionContext: ExecutionContext)
extends ActionBuilder[AuthenticatedRequest, AnyContent]
  with ActionFunction[Request, AuthenticatedRequest]
  with AuthorisedFunctions with Logging {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    val enrolmentKey  = "HMRC-CUS-ORG"
    val identifierKey = "EORINumber"

    authorised(AffinityGroup.Individual or AffinityGroup.Organisation)
      .retrieve(Retrievals.authorisedEnrolments) {
        enrolments => {
          for {
            enrolment <- enrolments.getEnrolment(enrolmentKey)
            eori      <- enrolment.getIdentifier(identifierKey)
          } yield block(AuthenticatedRequest(request, eori.value))
        }.getOrElse {
          logger.warn("User had no CDS enrolment")
          Future.successful(Forbidden)
        }
      }
  }
}
