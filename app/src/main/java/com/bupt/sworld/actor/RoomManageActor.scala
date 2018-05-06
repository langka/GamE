package com.bupt.sworld.actor

import akka.actor.{Actor, ActorRef, Cancellable}
import akka.remote.transport.ThrottlerTransportAdapter.Direction.Receive
import android.util.Log
import com.bupt.sworld.actor.RoomManageActor.{KickTimeout, _}
import com.bupt.sworld.actor.UserManageActor.{InitTimeout, LoginCallBack}
import com.bupt.sworld.actor.common.Actors
import com.bupt.sworld.actor.common.Actors.post
import com.bupt.sworld.service.MessageService.ConfirmMsg
import com.bupt.sworld.service.{LocalService, MessageService}
import sse.xs.msg.CommonFailure
import sse.xs.msg.room._
import sse.xs.msg.user.LoginFailure

import scala.collection.mutable

/**
  * Created by xusong on 2018/3/24.
  * About:
  */
class RoomManageActor extends Actor {
  val selection = context.actorSelection(Actors.basePath + "/roommanager")
  val TAG = classOf[RoomManageActor].getName

  import scala.concurrent.duration._

  var remoteRef: ActorRef = _

  var currentRoomRef: ActorRef = _

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
      val failure = CommonFailure("房间服务器未初始化完成，请稍后！")
      post {
        f(failure)
      }
  }

  //

  def ready: Receive = {
    case RoomsCallBack(s, f) =>
      remoteRef ! GetAllRooms
      become(waitForRooms(s, f))
      context.system.scheduler.scheduleOnce(10 seconds, self, RoomsTimeout)
    case EnterRoomCallBack(id, onS, onF) =>
      val selection = roomSelection(id)
      selection ! EnterRoom(LocalService.currentUser)
      context.system.scheduler.scheduleOnce(10 seconds, self, EnterTimeout)
      become(waitToEnter(onS, onF))
    case CreateCallBack(s, f) =>
      remoteRef ! CreateRoom(LocalService.currentUser)
      context.system.scheduler.scheduleOnce(5 seconds, self, CreateTimeout)
      become(waitForCreateResp(s, f))

  }

  def waitForRooms(s: RoomSearchResponse => Unit, f: CommonFailure => Unit): Receive = {
    case RoomsCallBack(s1, f1) =>
      val failure = CommonFailure("请求频繁！")
      post {
        f1(failure)
      }
    case RoomsTimeout =>
      val failure = CommonFailure("房间服务器超时!")
      post {
        f(failure)
      }
      become(ready)
    case resp: RoomSearchResponse =>
      post {
        s(resp)
      }
      become(ready)
  }

  def waitToEnter(s: EnterRoomSuccess => Unit, f: CommonFailure => Unit): Receive = {
    case EnterTimeout =>
      val failure = CommonFailure("进入房间超时!")
      post {
        f(failure)
      }
      become(ready)
    case e: EnterRoomSuccess =>
      post {
        s(e)
      }
      become(inRoom(e.id))
    case failure: CommonFailure =>
      post {
        f(failure)
      }
      become(ready)
  }

  def waitForCreateResp(s: (CreateSuccess) => Unit, f: (CommonFailure) => Unit): Receive = {
    case c: CreateSuccess =>
      Log.d("CCC", "CRR")
      post {
        s(c)
      }

      // TODO: 改为inroom
      become(inRoom(c.token))
    case CreateTimeout =>
      val failure = CommonFailure("创建房间超时")
      post {
        f(failure)
      }
      become(ready)
  }

  def inRoom(id: Long): Receive =
    roomMessageDispatcher(id) orElse {
      case SwapRoomCallBack(i, s, f) =>
        roomSelection(id) ! SwapRoom
        context.system.scheduler.scheduleOnce(10 seconds, self, SwapTimeout)
        context.become(waitForSwap(id, s, f))
      case KickCallBack(i, s, f) =>
        roomSelection(id) ! Kick
        context.system.scheduler.scheduleOnce(10 seconds, self, KickTimeout)
        context.become(waitForKick(id, s, f))

      case l: LeaveRoom =>
        roomSelection(id) ! LeaveRoom(LocalService.currentUser)
        become(ready)

      case StartCallBack(r, f) =>
        roomSelection(id) ! StartGame
        context.system.scheduler.scheduleOnce(10 seconds, self, StartTimeout)
        become(waitForStart(id, r, f))
    }

  //负责处理被动收到收到的消息
  val talkMsgs = new mutable.TreeSet[Long]

  def gameMessageDispatcher(id: Long): Receive = {
    //投降
    case Surrender =>
      roomSelection(id) ! Surrender
      become(inRoom(id))
    //游戏结束由客户端自行判断，不需要等待服务器端发送消息
    case e:EndGame =>
      roomSelection(id) ! e
      become(inRoom(id))
    case e: OtherMove =>
      post {
        MessageService.onGameMessageArrive(e)
      }
    case t: TalkMessage =>
      //自己发的信息
      if (t.speaker == LocalService.currentUser.name) {
        if (talkMsgs.contains(t.id)) {
          //确认消息
          post {
            MessageService.onGameMessageArrive(ConfirmMsg(t.id))
          }
        }
        else {
          roomSelection(id) ! t
          talkMsgs.add(t.id)
        }
      }

      else {
        post {
          MessageService.onGameMessageArrive(t)
        }
      }

  }

  def roomMessageDispatcher(id: Long): Receive = {

    case g: GameStarted =>
      post {
        MessageService.onRoomMessageArrive(g)
      }
      become(gameStarted(id))
    case t: TalkMessage =>
      post {
        MessageService.onRoomMessageArrive(t)
      }

    case n: NewUserEnter =>
      post {
        MessageService.onRoomMessageArrive(n)
      }

    case o: OtherLeaveRoom =>
      post {
        MessageService.onRoomMessageArrive(o)
      }

    case s:SwapSuccess =>
      post{
        MessageService.onRoomMessageArrive(s)
      }
  }

  def gameStarted(id: Long): Receive = gameMessageDispatcher(id) orElse {

    case e: MoveCallBack =>
      roomSelection(id) ! e.m
      context.system.scheduler.scheduleOnce(10 seconds, self, MoveTimeout)
      become(waitForMoveResp(id, e.onSucc, e.onFailure))

    //普通聊天信息不用保证其重要性
    case t: MyTalkMessage =>
      roomSelection(id) ! t.msg
  }


  def waitForMoveResp(id: Long, ons: String => Unit, onf: CommonFailure => Unit): Receive =
    gameMessageDispatcher(id) orElse {
      case MoveSuccess =>
        val msg = "移动成功!"
        post {
          ons(msg)
        }
        become(gameStarted(id))
      case MoveTimeout =>
        val failure = CommonFailure("移动超时")
        post {
          onf(failure)
        }
        become(gameStarted(id))
      case failure: CommonFailure =>
        post {
          onf(failure)
        }
        become(gameStarted(id))

    }


  def waitForStart(id: Long, onS: RoomInfo => Unit, onF: CommonFailure => Unit): Receive =
    roomMessageDispatcher(id) orElse {
      case StartTimeout =>
        val failure = CommonFailure("连接超时")
        post {
          onF(failure)
        }
        become(inRoom(id))
      case GameStarted(r) =>
        post {
          onS(r)
        }
        become(gameStarted(id))
      case c: CommonFailure =>
        post {
          onF(c)
        }
        become(inRoom(id))
    }

  def waitForSwap(id: Long, s: RoomInfo => Unit, f: CommonFailure => Unit): Receive =
    roomMessageDispatcher(id) orElse {
      case SwapTimeout =>
        val failure = CommonFailure("连接超时")
        post {
          f(failure)
        }
        become(inRoom(id))
      case SwapSuccess(r) =>
        post {
          s(r)
        }
        become(inRoom(id))
      case c: CommonFailure =>
        post {
          f(c)
        }
        become(inRoom(id))
    }

  def waitForKick(id: Long, s: RoomInfo => Unit, f: CommonFailure => Unit): Receive =
    roomMessageDispatcher(id) orElse {
      case KickTimeout =>
        val failure = CommonFailure("连接超时")
        post {
          f(failure)
        }
        become(inRoom(id))
      case KickSuccess(r) =>
        post {
          s(r)
        }
        become(inRoom(id))
      case c: CommonFailure =>
        post {
          f(c)
        }
        become(inRoom(id))
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

  case class SwapRoomCallBack(id: Long, s: RoomInfo => Unit, f: CommonFailure => Unit)

  case object SwapTimeout

  case class KickCallBack(id: Long, s: RoomInfo => Unit, f: CommonFailure => Unit)

  case object KickTimeout

  case class StartCallBack(r: RoomInfo => Unit, s: CommonFailure => Unit)

  case object StartTimeout


  case class MoveCallBack(m: Move, onSucc: String => Unit, onFailure: CommonFailure => Unit)

  case object MoveTimeout


  case class MyTalkMessage(msg: TalkMessage)

}

