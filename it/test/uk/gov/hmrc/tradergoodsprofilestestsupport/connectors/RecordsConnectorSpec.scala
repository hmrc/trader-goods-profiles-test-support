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

package uk.gov.hmrc.tradergoodsprofilestestsupport.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.tradergoodsprofilestestsupport.models.{AdviceStatus, Declarable, GoodsItemPatch}

class RecordsConnectorSpec
  extends AnyFreeSpec
    with Matchers
    with WireMockSupport
    with ScalaFutures
    with IntegrationPatience {

  private lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.trader-goods-profiles-hawk-stub.port" -> wireMockPort)
      .build()

  private lazy val connector = app.injector.instanceOf[RecordsConnector]

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  ".patch" - {

    val payload = GoodsItemPatch(
      eori = "eori",
      recordId = "123",
      accreditationStatus = Some(AdviceStatus.Requested),
      version = Some(1),
      active = Some(false),
      locked = Some(true),
      toReview = None,
      declarable = Some(Declarable.ImmiReady),
      reviewReason = None,
      updatedDateTime = None
    )

    "must send a patch request and return success" in {

      wireMockServer.stubFor(
        patch(urlEqualTo("/test-support/goods-item"))
          .withRequestBody(equalTo(Json.toJson(payload).toString))
          .willReturn(
            ok()
          )
      )

      connector.patch(payload).futureValue
    }

    "must return a failed future when the server returns an error" in {

      wireMockServer.stubFor(
        patch(urlEqualTo("/test-support/goods-item"))
          .withRequestBody(equalTo(Json.toJson(payload).toString))
          .willReturn(
            serverError()
          )
      )

      connector.patch(payload).failed.futureValue
    }
  }
}
