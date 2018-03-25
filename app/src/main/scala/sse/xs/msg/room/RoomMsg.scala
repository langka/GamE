package sse.xs.msg.room

import sse.xs.msg.user.User

/**
  * Created by xusong on 2018/3/14.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
sealed trait RoomMsg

case class RoomInfo(players: Array[Option[User]], master: User)

case class EnterRoom(user: User)

case class EnterRoomSuccess(roomInfo: RoomInfo)

case class NewUserEnter(roomInfo: RoomInfo)

case class TalkMessage(speaker: String, detail: String)


case class LeaveRoom(user: User)

case object LeaveRoomSuccess

case object OtherLeaveRoom

//game control
case object StartGame

case class GameStarted(roomInfo: RoomInfo)


case class Pos(x: Int, y: Int)

case class Move(from: Pos, to: Pos)

case object MoveSuccess

case class OtherMove(from: Pos, to: Pos)


case object GetAllRooms

case class RoomSearchResponse(rooms: Array[(Long,RoomInfo)])


case class CreateRoom(user: User)

case class DestroyRoom(id: Long)

case class CreateSuccess(token: Long)



