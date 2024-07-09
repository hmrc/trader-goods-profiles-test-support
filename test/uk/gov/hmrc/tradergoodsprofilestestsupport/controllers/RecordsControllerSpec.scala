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

package uk.gov.hmrc.tradergoodsprofilestestsupport.controllers

import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{BodyParsers, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.tradergoodsprofilestestsupport.connectors.RecordsConnector
import uk.gov.hmrc.tradergoodsprofilestestsupport.controllers.actions.{AuthAction, AuthenticatedRequest}
import uk.gov.hmrc.tradergoodsprofilestestsupport.models.{AdviceStatus, Declarable, GoodsItemPatch}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecordsControllerSpec extends AnyFreeSpec with Matchers with MockitoSugar with OptionValues {

  ".patch" - {

    "when the user has an enrolment with the eori they are trying to patch" - {

      "must submit the patch request return OK when given a valid payload" in {

        val mockConnector = mock[RecordsConnector]

        when(mockConnector.patch(any())(any())).thenReturn(Future.successful(Done))

        val payload = Json.obj(
          "active" -> false,
          "version" -> 123,
          "adviceStatus" -> "Requested",
          "declarable" -> "Not Ready For IMMI"
        )

        val expectedPatch = GoodsItemPatch(
          eori = "eori",
          recordId = "recordId",
          accreditationStatus = Some(AdviceStatus.Requested),
          version = Some(123),
          active = Some(false),
          locked = None,
          toReview = None,
          declarable = Some(Declarable.ImmiNotReady),
          reviewReason = None,
          updatedDateTime = None
        )

        val app =
          GuiceApplicationBuilder()
            .overrides(
              bind[RecordsConnector].toInstance(mockConnector),
              bind[AuthAction].toInstance(new FakeAuthAction)
            )
            .build()

        running(app) {

          val request =
            FakeRequest(PATCH, routes.RecordsController.patch("eori", "recordId").url)
              .withJsonBody(payload)

          val result = route(app, request).value

          status(result) mustEqual OK
          verify(mockConnector, times(1)).patch(eqTo(expectedPatch))(any())
        }
      }
    }

    "when the user has an enrolment with a different eori to the one they're trying to patch" - {

      "must return Forbidden" in {

        val mockConnector = mock[RecordsConnector]

        val payload = Json.obj(
          "active" -> false,
          "version" -> 123
        )

        val app =
          GuiceApplicationBuilder()
            .overrides(
              bind[RecordsConnector].toInstance(mockConnector),
              bind[AuthAction].toInstance(new FakeAuthAction)
            )
            .build()

        running(app) {

          val request =
            FakeRequest(PATCH, routes.RecordsController.patch("some other eori", "recordId").url)
              .withJsonBody(payload)

          val result = route(app, request).value

          status(result) mustEqual FORBIDDEN
          verify(mockConnector, never).patch(any)(any)
        }
      }
    }
  }
}

class FakeAuthAction @Inject() extends AuthAction(mock[AuthConnector], mock[BodyParsers.Default]) {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
    block(AuthenticatedRequest(request, "eori"))
}
