/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.github.filosganga.play.predictionio

import play.api.Application
import io.prediction.{Item, User}

import org.joda.time.DateTime
import play.api.libs.concurrent.Akka
import scala.concurrent.{ExecutionContext, Future}

/**
 *
 * @author Filippo De Luca - me@filippodeluca.com
 */
object PredictionIO {

  /** The exception we are throwing. */
  private val pluginNotRegisteredError = new IllegalStateException("PredictionIO plugin is not registered.")

  private def api(implicit app: Application): Api =
    app.plugin[HasApi].getOrElse(throw pluginNotRegisteredError).api

  def createUser(uid: String, location: Option[Location] = None)(implicit app: Application, ec: ExecutionContext): Future[User] = {
    api.createUser(uid, location)
  }

  def getUser(uid: String)(implicit app: Application, ec: ExecutionContext): Future[User] = {
    api.getUser(uid)
  }

  def deleteUser(uid: String)(implicit app: Application, ec: ExecutionContext): Future[Unit] = {
    api.deleteUser(uid)
  }

  def createItem(id: String,
                 types: Set[String] = Set.empty,
                 location: Option[Location] = None,
                 start: Option[DateTime] = None,
                 end: Option[DateTime] = None)(implicit app: Application, ec: ExecutionContext) = {
    implicit val ec = Akka.system.dispatcher

    api.createItem(id, types, location, end)
  }


  def getItem(id: String)(implicit app: Application, ec: ExecutionContext): Future[Item] =
    api.getItem(id)

  def deleteItem(id: String)(implicit app: Application, ec: ExecutionContext): Future[Unit] = {
    api.deleteItem(id)
  }

  def userActionItem(userId: String,
                     itemId: String,
                     action: String,
                     rate: Option[Int] = None,
                     dateTime: DateTime = DateTime.now(),
                     location: Option[Location] = None)(implicit app: Application, ec: ExecutionContext): Future[Unit] = {

    api.userActionItem(userId, itemId, action, rate, dateTime, location)
  }

  def getItemsInfoRecTopN(engine: String,
                          userId: String,
                          n: Int = 15,
                          types: Set[String] = Set.empty,
                          attributes: Set[String] = Set.empty,
                          location: Option[Location],
                          distance: Option[Distance])(implicit app: Application, ec: ExecutionContext): Future[Iterable[ItemInfo]] = {

    api.getItemsRecTopN(engine, userId, n, types, attributes, location, distance)
  }

  def getItemsInfoRecTopNM(engine: String,
                          userId: String,
                          n: Int = 15,
                          types: Set[String] = Set.empty,
                          attributes: Set[String] = Set.empty,
                          location: Option[Location],
                          distance: Option[Distance])(implicit app: Application, ec: ExecutionContext): Future[Map[String,Set[String]]] = {

    api.getItemsRecTopNM(engine, userId, n, types, attributes, location, distance)
  }

  def getItemsInfoSimTopN(engine: String,
                          targetId: String,
                          n: Int = 15,
                          types: Set[String] = Set.empty,
                          attributes: Set[String] = Set.empty,
                          location: Option[Location],
                          distance: Option[Distance])(implicit app: Application, ec: ExecutionContext): Future[Iterable[ItemInfo]] = {

    api.getItemsSimTopN(engine, targetId, n, types, attributes, location, distance)
  }

  def getItemsRecTopN(engine: String,
                      userId: String,
                      n: Int = 15,
                      types: Set[String] = Set.empty,
                      attributes: Set[String] = Set.empty,
                      location: Option[Location],
                      distance: Option[Distance])(implicit app: Application, ec: ExecutionContext): Future[Iterable[Item]] = {

    getItemsInfoRecTopN(engine, userId, n, types, attributes, location, distance).flatMap {
      xs => Future.sequence(xs.map(x => getItem(x.id)))
    }
  }

  def getItemsSimTopN(engine: String,
                      targetId: String,
                      n: Int = 15,
                      types: Set[String] = Set.empty,
                      attributes: Set[String] = Set.empty,
                      location: Option[Location],
                      distance: Option[Distance])(implicit app: Application, ec: ExecutionContext): Future[Iterable[Item]] = {

    getItemsInfoSimTopN(engine, targetId, n, types, attributes, location, distance).flatMap {
      xs => Future.sequence(xs.map(x => getItem(x.id)))
    }
  }

}
