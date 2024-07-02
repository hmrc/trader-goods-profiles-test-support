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

import play.api.Logging
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendBaseController
import uk.gov.hmrc.tradergoodsprofilestestsupport.connectors.RecordsConnector
import uk.gov.hmrc.tradergoodsprofilestestsupport.controllers.actions.AuthAction
import uk.gov.hmrc.tradergoodsprofilestestsupport.models.{GoodsItemPatch, GoodsItemPatchRequest}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RecordsController @Inject()(
                                   override val controllerComponents: ControllerComponents,
                                   connector: RecordsConnector,
                                   authenticate: AuthAction
                                 )(implicit ec: ExecutionContext) extends BackendBaseController with Logging {

  def patch(eori: String, recordId: String): Action[GoodsItemPatchRequest] = authenticate(parse.json[GoodsItemPatchRequest]).async { implicit request =>
    if (request.eori == eori) {
      val patch = GoodsItemPatch.fromPatchRequest(eori, recordId, request.body)

      connector.patch(patch)
        .map(_ => Ok)
    } else {
      logger.warn(s"Eoris did not match: $eori / ${request.eori}")
      Future.successful(Forbidden)
    }
  }
}
