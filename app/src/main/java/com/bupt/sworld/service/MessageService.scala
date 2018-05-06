package com.bupt.sworld.service

import sse.xs.msg.room.Move

/**
  * Created by xusong on 2018/4/14.
  * About:回调注册服务
  */
object MessageService {

  //message的confirm消息
  case class ConfirmMsg(id:Long)


  var gameMessageListener: Option[MessageListener] = None

  var roomMessageListener: Option[MessageListener] = None

  var publicMessageListener: Option[MessageListener] = None


  def setPublicMessageListener(p: MessageListener): Unit = {
    publicMessageListener = Some(p)
  }

  def setGameMsgListener(g: MessageListener): Unit = {
    gameMessageListener = Some(g)
  }

  def setRoomMsgListener(g: MessageListener): Unit = {
    roomMessageListener = Some(g)
  }


  def onPublicMessageArrive(anyRef: AnyRef): Unit = {
    publicMessageListener foreach { x =>
      x.onMessageArrive(anyRef)

    }
  }

  def onRoomMessageArrive(anyRef: AnyRef): Unit = {
    roomMessageListener foreach { r =>
      r.onMessageArrive(anyRef)
    }
  }

  def onGameMessageArrive(message: AnyRef): Unit = {
    gameMessageListener foreach { e =>
      e.onMessageArrive(message)
    }
  }

}

trait MessageListener {
  def onMessageArrive(anyRef: AnyRef): Unit
}