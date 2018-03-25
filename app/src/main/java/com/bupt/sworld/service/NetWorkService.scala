package com.bupt.sworld.service
import akka.util.Timeout
import com.bupt.sworld.actor.RoomManageActor.{CreateCallBack, EnterRoomCallBack, RoomsCallBack}
import com.bupt.sworld.actor.UserManageActor
import com.bupt.sworld.actor.common.Actors
import sse.xs.msg.CommonFailure
import sse.xs.msg.room.{CreateSuccess, EnterRoomSuccess, RoomSearchResponse}
import sse.xs.msg.user.{LoginFailure, LoginRequest, LoginSuccess}

import scala.concurrent.duration._
/**
  * Created by xusong on 2018/3/14.
  * About:Only this object can touch the actors directly,
  * not to expose the interface to the activities
  */

object NetWorkService {
  implicit val timeout = Timeout(10 seconds)


  def testConnection(onSucess: => Unit, onFailure: => Unit): Unit = {
    Actors.tryConnect(onSucess, onFailure)
  }

  def login(account: String, pwd: String, success: LoginSuccess => Unit, failure: LoginFailure => Unit): Unit = {
    import UserManageActor.LoginCallBack
    val request = LoginRequest(account, pwd)
    Actors.localUserRef ! LoginCallBack(request, success, failure)
  }

  def findAllRooms(success: RoomSearchResponse => Unit, failure: CommonFailure => Unit): Unit = {
    Actors.localRoomRef ! RoomsCallBack(success, failure)
  }

  def createRoom(onS: CreateSuccess => Unit, onF: CommonFailure => Unit): Unit = {
    Actors.localRoomRef ! CreateCallBack(onS, onF)
  }

  def enterRoom(id: Long, onS: EnterRoomSuccess => Unit, onF: CommonFailure => Unit): Unit = {
    Actors.localRoomRef ! EnterRoomCallBack(id, onS, onF)
  }

}


