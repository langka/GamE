package com.bupt.sworld.actor

import akka.actor.{Actor, ActorIdentity, ActorRef, ActorSelection, Cancellable, Identify, Stash}
import com.bupt.sworld.actor.UserManageActor.LoginCallBack
import com.bupt.sworld.activity.MetaData._
import akka.pattern._
import akka.pattern.{ask, pipe}
import android.util.Log
import com.bupt.sworld.actor.common.Actors.post
import com.bupt.sworld.service.LocalService
import sse.xs.msg.CommonFailure
import sse.xs.msg.room.RoomSearchResponse
import sse.xs.msg.user._

/**
  * Created by xusong on 2018/3/15.
  * About:
  */


class UserManageActor extends Actor {

  import context._
  import UserManageActor._
  import scala.concurrent.duration._

  private var user: User = _

  private val selection = system.actorSelection("akka.tcp://nice@10.209.8.196:2552/user/usermanager")


  var remoteRef: ActorRef = _
  var scheduler:Cancellable = _
  //在actor启动的时候尝试解析
  override def preStart(): Unit = {
    selection ! "HELLO"
    scheduler=context.system.scheduler.schedule(0 seconds,3 seconds,self,InitTimeout)
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

  def loggedIn: Receive = {
    case _ =>
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



}
