package com.bupt.sworld.actor

import akka.actor.{Actor, ActorIdentity, ActorRef, ActorSelection, Cancellable, Identify, Stash}
import com.bupt.sworld.actor.UserManageActor.LoginCallBack
import com.bupt.sworld.activity.MetaData._
import akka.pattern._
import akka.pattern.{ask, pipe}
import android.util.Log
import com.bupt.sworld.activity.MetaData
import com.bupt.sworld.actor.RoomManageActor.MoveTimeout
import com.bupt.sworld.actor.common.Actors.post
import com.bupt.sworld.service.MessageService.ConfirmMsg
import com.bupt.sworld.service.{LocalService, MessageService}
import sse.xs.msg.CommonFailure
import sse.xs.msg.game.{GameHistories, GetGameHistory}
import sse.xs.msg.room.{InviteMessage, RoomSearchResponse, TalkMessage}
import sse.xs.msg.user._

import scala.collection.mutable

/**
  * Created by xusong on 2018/3/15.
  * About:
  */


class UserManageActor extends Actor {

  import context._
  import UserManageActor._
  import scala.concurrent.duration._

  private var user: User = _

  private val basePath = "akka.tcp://" + MetaData.systemName + "@" + MetaData.ip + ":" + MetaData.port + "/user"
  private val selection = system.actorSelection(basePath + "/usermanager")


  var remoteRef: ActorRef = _
  var scheduler: Cancellable = _

  //在actor启动的时候尝试解析
  override def preStart(): Unit = {
    selection ! "HELLO"
    scheduler = context.system.scheduler.schedule(0 seconds, 3 seconds, self, InitTimeout)
  }

  override def receive: Receive = {
    //认为如果收到了信息，就一定会有一个ref
    case "OJBK" =>
      remoteRef = sender()
      scheduler.cancel()
      if (remoteRef != null)
        become(ready)
      else become(errorState)
    //无限重试
    case InitTimeout =>
      selection ! "HELLO"

    case LoginCallBack(r, s, f) => //
      val failure = LoginFailure("正在连接至用户服务器，请稍后！")
      post {
        f(failure)
      }
  }

  val idSet = new mutable.TreeSet[Long]()

  def loggedIn: Receive = {
    //发送到大厅的聊天/邀请消息,这个消息将会被所有人收到，
    //此消息不需要保证一定送达
    case t: TalkMessage =>
      //这是自己发的
      if (t.speaker == LocalService.currentUser.name) {
        if (!idSet.contains(t.id)) {
          //这是一次发送
          idSet.add(t.id)
          remoteRef ! t
        } else {
          //这是一次自己的确认回复
          post {
            MessageService.onPublicMessageArrive(ConfirmMsg(t.id))
          }

        }
        //来自其他用户的消息
      } else {
        post {
          MessageService.onPublicMessageArrive(t)
        }
      }

    case i: InviteMessage =>
      if (i.user.name == LocalService.currentUser.name) {
        //自己的消息
        if (!idSet.contains(i.mid)) {
          //发送
          idSet.add(i.mid)
          remoteRef ! i
        } else {
          //do nothing
          //invite消息不需要确认
        }
      } else {
        post {
          MessageService.onPublicMessageArrive(i)

        }
      }
    case m: ModifyCallBack =>
      remoteRef ! m.m
      context.system.scheduler.scheduleOnce(5 seconds, self, ModifyTimeOut)
      become(waitForModifyResp(m.s, m.f))

    case history: HistoryCallBack =>
      remoteRef ! GetGameHistory(history.id)
      context.system.scheduler.scheduleOnce(5 seconds, self, HistoryTimeout)
      become(waitForHistories(history.success, history.failure))
  }

  def waitForHistories(onS: GameHistories => Unit, onF: CommonFailure => Unit): Receive = {
    case HistoryTimeout =>
      val failure = CommonFailure("查询超时！")
      post {
        onF(failure)
      }
      become(loggedIn)

    case s: GameHistories =>
      become(loggedIn)
      post {
        onS(s)
      }
    case c: CommonFailure =>
      post {
        onF(c)
      }
      become(loggedIn)
  }

  def waitForModifyResp(onSuccess: User => Unit, onFailure: CommonFailure => Unit): Receive = {
    case ModifyTimeOut =>
      val failure = CommonFailure("修改超时！")
      post {
        onFailure(failure)
      }
      become(loggedIn)

    case s: ModifySuccess =>
      become(loggedIn)
      post {
        onSuccess(s.user)
      }
    case c: CommonFailure =>
      post {
        onFailure(c)
      }
      become(loggedIn)
  }

  def waitFoLoginResp(onS: (LoginSuccess) => Unit, onF: (LoginFailure) => Unit): Receive = {
    //case x:String=>post(onS(LoginSuccess(User("x","y"))))
    case f: LoginFailure =>
      become(ready)
      post(onF(f))
    case s: LoginSuccess =>
      LocalService.currentUser = s.user
      become(loggedIn)
      user = s.user
      post {
        onS(s)
      }
    case LoginTimeout =>
      become(ready)
      val failure = LoginFailure("登录超时!")
      post {
        onF(failure)
      }
  }

  def waitForRegisterResp(onS: RegisterSuccess => Unit, onF: RegisterFailure => Unit): Receive = {
    case s: RegisterSuccess =>
      LocalService.currentUser = s.user
      become(loggedIn)
      user = s.user
      post {
        onS(s)
      }
    case f: RegisterFailure =>
      become(ready)
      post {
        onF(f)
      }
    case RegisterTimeout =>
      val failure = RegisterFailure("zhu ce chao shi!")
  }

  def ready: Receive = {
    case LoginCallBack(r, s, f) =>
      remoteRef ! r
      Log.d(TAG, "SENDED LOGIN REQUEST")
      context.system.scheduler.scheduleOnce(5 seconds, self, LoginTimeout)
      become(waitFoLoginResp(s, f))
    case RegisterCallBack(r, s, f) =>
      remoteRef ! r
      context.system.scheduler.scheduleOnce(5 seconds, self, RegisterTimeout)
      become(waitForRegisterResp(s, f))
  }

  def errorState: Receive = {
    case _ =>
  }


}

object UserManageActor {
  //tags
  val TAG = classOf[UserManageActor].getName

  case object InitTimeout

  //accepted messages
  case class LoginCallBack(request: LoginRequest, s: LoginSuccess => Unit, f: LoginFailure => Unit)

  case class RegisterCallBack(request: RegisterRequest, s: RegisterSuccess => Unit, f: RegisterFailure => Unit)

  case object LoginTimeout

  case object RegisterTimeout

  case class ModifyCallBack(m: ModifyU, s: User => Unit, f: CommonFailure => Unit)

  case object ModifyTimeOut

  case class HistoryCallBack(id: Int, success: GameHistories => Unit, failure: CommonFailure => Unit)

  case object HistoryTimeout

}
