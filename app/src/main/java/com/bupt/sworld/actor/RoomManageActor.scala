package com.bupt.sworld.actor

import akka.actor.{Actor, ActorRef, Cancellable}
import android.util.Log
import com.bupt.sworld.actor.RoomManageActor._
import com.bupt.sworld.actor.UserManageActor.{InitTimeout, LoginCallBack}
import com.bupt.sworld.actor.common.Actors
import com.bupt.sworld.actor.common.Actors.post
import com.bupt.sworld.service.LocalService
import sse.xs.msg.CommonFailure
import sse.xs.msg.room._
import sse.xs.msg.user.LoginFailure

/**
  * Created by xusong on 2018/3/24.
  * About:
  */
class RoomManageActor extends Actor {
  val selection = context.actorSelection(Actors.basePath + "/roommanager")
  val TAG = classOf[RoomManageActor].getName

  import scala.concurrent.duration._

  var remoteRef: ActorRef = _

  import context._

  var scheduler: Cancellable = _

  //在actor启动的时候尝试解析
  override def preStart(): Unit = {
    selection ! "HELLO"
    scheduler = context.system.scheduler.schedule(0 seconds, 3 seconds, self, InitTimeout)
  }

  override def receive: Receive = {
    case "OJBK" =>
      Log.d(TAG, "received OJBK FROM ROOMMANAGER")
      remoteRef = sender()
      scheduler.cancel()
      become(ready)
    //无限重试
    case InitTimeout =>
      selection ! "HELLO"
      Log.d(TAG, "RESEND HELLO TO ROOMMANAGER!")
    case RoomsCallBack(s, f) => //
      val failure = CommonFailure("正在连接至房间服务器，请稍后！")
      post {
        f(failure)
      }
  }

 //

  def ready: Receive = {
    case RoomsCallBack(s, f) =>
      remoteRef ! GetAllRooms
      become(waitForRooms(s, f))
      context.system.scheduler.scheduleOnce(5 seconds, self, RoomsTimeout)
    case EnterRoomCallBack(id, onS, onF) =>
      val selection = roomSelection(id)
      selection ! EnterRoom(LocalService.currentUser)
      context.system.scheduler.scheduleOnce(5 seconds, self, EnterTimeout)
      become(waitToEnter(onS, onF))
    case CreateCallBack(s, f) =>
      remoteRef ! CreateRoom(LocalService.currentUser)
      context.system.scheduler.scheduleOnce(5 seconds, self, CreateTimeout)
      become(waitForCreateResp(s, f))
  }

  def waitForRooms(s: RoomSearchResponse => Unit, f: CommonFailure => Unit): Receive = {
    case RoomsCallBack(s1, f1) =>
      val failure = CommonFailure("WAITING FOR RESP")
      post {
        f1(failure)
      }
    case RoomsTimeout =>
      val failure = CommonFailure("房间服务器超时!")
      post {
        f(failure)
      }
      unbecome()
    case resp: RoomSearchResponse =>
      post {
        s(resp)
      }
      unbecome()
  }

  def waitToEnter(s: EnterRoomSuccess => Unit, f: CommonFailure => Unit): Receive = {
    case EnterTimeout =>
      val failure = CommonFailure("进入房间超时!")
      post {
        f(failure)
      }
      unbecome()
    case e: EnterRoomSuccess =>
      post {
        s(e)
      }
      become(inRoom)
    case failure: CommonFailure =>
      post {
        f(failure)
      }
      unbecome()
  }

  def waitForCreateResp(s: (CreateSuccess) => Unit, f: (CommonFailure) => Unit): Receive = {
    case c: CreateSuccess =>
      post {
        s(c)
      }
      become(inRoom)
    case CreateTimeout =>
      val failure = CommonFailure("创建房间超时")
      post {
        f(failure)
      }
      unbecome()
  }

  def inRoom: Receive = {
    case _ =>
  }

  private def roomSelection(id: Long) = {
    val r = "/room" + id
    context.actorSelection(Actors.basePath + "/roommanager" + r)
  }

}

object RoomManageActor {

  case class RoomsCallBack(s: RoomSearchResponse => Unit, f: CommonFailure => Unit)

  case object RoomsTimeout

  case class EnterRoomCallBack(id: Long, onS: EnterRoomSuccess => Unit, onF: CommonFailure => Unit)

  case object EnterTimeout

  case object CreateTimeout

  case class CreateCallBack(s: CreateSuccess => Unit, f: CommonFailure => Unit)

}
