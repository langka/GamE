package com.bupt.sworld.service

import akka.util.Timeout
import com.bupt.sworld.actor.RoomManageActor._
import com.bupt.sworld.actor.UserManageActor
import com.bupt.sworld.actor.UserManageActor.{HistoryCallBack, ModifyCallBack, RegisterCallBack}
import com.bupt.sworld.actor.common.Actors
import sse.xs.msg.CommonFailure
import sse.xs.msg.game.GameHistories
import sse.xs.msg.room._
import sse.xs.msg.user._

import scala.concurrent.duration._

/**
  * Created by xusong on 2018/3/14.
  * About:Only this object can touch the actors directly,
  * not to expose the interface to the activities
  */

object NetWorkService {
  def startGame(infoToUnit: (RoomInfo) => Unit, failureToUnit: (CommonFailure) => Unit) = {
    Actors.localRoomRef ! StartCallBack(infoToUnit, failureToUnit)
  }

  implicit val timeout = Timeout(10 seconds)


  def testConnection(onSucess: => Unit, onFailure: => Unit): Unit = {
    Actors.tryConnect(onSucess, onFailure)
  }


  def register(account: String, pwd: String, onSuccess: RegisterSuccess => Unit, onFailure: RegisterFailure => Unit): Unit = {
    val request = RegisterRequest(account, pwd)
    Actors.localUserRef ! RegisterCallBack(request, onSuccess, onFailure)

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

  def swap(id: Long, onS: RoomInfo => Unit, onF: CommonFailure => Unit): Unit = {
    Actors.localRoomRef ! SwapRoomCallBack(id, onS, onF)
  }

  def kick(id: Long, onS: RoomInfo => Unit, onF: CommonFailure => Unit): Unit = {
    Actors.localRoomRef ! KickCallBack(id, onS, onF)
  }

  def leave(): Unit = {
    Actors.localRoomRef ! LeaveRoom(LocalService.currentUser)
  }

  def sendRoomMessage(t: TalkMessage): Unit = {
    Actors.localRoomRef ! MyTalkMessage(t)
  }

  def sendPublicMessage(t: TalkMessage): Unit = {
    Actors.localUserRef ! t
  }

  def sendInviteToRoom(i: InviteMessage): Unit = {
    Actors.localUserRef ! i
  }

  def movePiece(i: Move, onS: String => Unit, onF: CommonFailure => Unit): Unit = {
    Actors.localRoomRef ! MoveCallBack(i, onS, onF)
  }

  def surrender(): Unit = {
    //投降会导致state变为inRoom，必须立即结束当前activity
    Actors.localRoomRef ! Surrender
  }

  def endGame(redWin: Boolean): Unit = {
    // TODO:
    Actors.localRoomRef ! EndGame(redWin)
  }

  def sendGameMessage(m: TalkMessage): Unit = {
    Actors.localRoomRef ! MyTalkMessage(m)
  }

  def modifyUserInfo(u: User, success: User => Unit, failure: CommonFailure => Unit): Unit = {
    Actors.localUserRef ! ModifyCallBack(ModifyU(u), success, failure)
  }

  def getGameHistories(id: Int, onSuccess: GameHistories => Unit, onFailure: CommonFailure => Unit): Unit = {
    Actors.localUserRef ! HistoryCallBack(id, onSuccess, onFailure)
  }

  def watchRoom(id: Long, onS: EnterRoomSuccess=>Unit, onF: CommonFailure => Unit): Unit = {
    Actors.localRoomRef ! WatchRoomCallback(id, onS, onF)
  }

}


