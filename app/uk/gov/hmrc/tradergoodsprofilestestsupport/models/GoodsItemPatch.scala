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

package uk.gov.hmrc.tradergoodsprofilestestsupport.models

import play.api.libs.json.{Json, OFormat}

import java.time.Instant

final case class GoodsItemPatch(
                                 eori: String,
                                 recordId: String,
                                 accreditationStatus: Option[AdviceStatus],
                                 version: Option[Int],
                                 active: Option[Boolean],
                                 locked: Option[Boolean],
                                 toReview: Option[Boolean],
                                 declarable: Option[Declarable],
                                 reviewReason: Option[String],
                                 updatedDateTime: Option[Instant]
                               )

object GoodsItemPatch {

  def fromPatchRequest(eori: String, recordId: String, patchRequest: GoodsItemPatchRequest): GoodsItemPatch =
    GoodsItemPatch(
      eori = eori,
      recordId = recordId,
      accreditationStatus = patchRequest.adviceStatus,
      version = patchRequest.version,
      active = patchRequest.active,
      locked = patchRequest.locked,
      toReview = patchRequest.toReview,
      declarable = patchRequest.declarable,
      reviewReason = patchRequest.reviewReason,
      updatedDateTime = patchRequest.updatedDateTime
    )

  implicit lazy val format: OFormat[GoodsItemPatch] = Json.format
}
