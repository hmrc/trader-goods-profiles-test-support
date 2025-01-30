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

import org.apache.pekko.Done
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.tradergoodsprofilestestsupport.config.Service
import uk.gov.hmrc.tradergoodsprofilestestsupport.models.GoodsItemPatch

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class RecordsConnector @Inject()(config: Configuration, httpClient: HttpClientV2)
                                (implicit ec: ExecutionContext) {

  private val baseUrl: Service = config.get[Service]("microservice.services.trader-goods-profiles-hawk-stub")
  private def patchUrl = url"$baseUrl/test-support/goods-item"

  def patch(patch: GoodsItemPatch)(implicit hc: HeaderCarrier): Future[Done] =
    httpClient
      .patch(patchUrl)
      .withBody(Json.toJson(patch))
      .execute[HttpResponse]
      .flatMap { response =>
        if (response.status == 200) {
          Future.successful(Done)
        } else {
          Future.failed(UpstreamErrorResponse("Unexpected response from downstream service", response.status))
        }
      }
}
