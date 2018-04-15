package com.bupt.sworld.actor.common

import akka.actor.{ActorRef, ActorSelection, ActorSystem, Props}
import akka.util.Timeout
import android.os.Handler
import com.bupt.sworld.activity.MetaData
import com.bupt.sworld.actor.{RoomManageActor, UserManageActor}

import scala.concurrent.duration._

/**
  * Created by xusong on 2018/3/14.
  * About:
  */

import scala.concurrent.ExecutionContext.Implicits.global

object Actors {
  /*
   *  remote ref
   */
  var connSelection: ActorSelection = _
  var userActor: ActorSelection = _
  var roomMangerActor: ActorSelection = _
  var systemActor: ActorSelection = _
  var handler: Handler = _
  implicit val timeout = Timeout(10 seconds)

  var connActor: ActorRef = _

  /*
   * local actor ref
   */
  var localUserRef: ActorRef = _

  var localRoomRef: ActorRef = _

  val basePath = "akka.tcp://" + MetaData.systemName + "@" + MetaData.ip + ":" + MetaData.port + "/user"

  def init(handler: Handler): Unit = {
    Actors.handler = handler
    val system = ActorSystem()

    //local
    localUserRef = system.actorOf(Props[UserManageActor], "um")
    localRoomRef = system.actorOf(Props[RoomManageActor], "roomm")
    val head: String = "akka.tcp://"
    val base = head + MetaData.systemName + "@" + MetaData.ip + ":" + MetaData.port + "/user/"
    //remote
    connSelection = system.actorSelection(base + "/conn")

  }

  //通过尝试解析actorref判断是否与服务器有连接
  def tryConnect(onSucess: => Unit, onFailure: => Unit): Unit = {
    val f = connSelection.resolveOne()
    f onSuccess {
      case e => connActor = e
        post(onSucess)
    }
    f onFailure {
      case e =>
        post(onFailure)
    }
  }


  def post(a: => Unit): Unit = {
    handler post new Runnable {
      override def run(): Unit = a
    }
  }
}
