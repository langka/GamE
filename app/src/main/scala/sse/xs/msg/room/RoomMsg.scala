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

case class EnterRoomSuccess(roomInfo: RoomInfo, id: Long)

case class NewUserEnter(roomInfo: RoomInfo)

case class TalkMessage(speaker: String, detail: String, id: Long)


case class LeaveRoom(user: User)

case object LeaveRoomSuccess

case class OtherLeaveRoom(roomInfo: RoomInfo)


case object SwapRoom

case object Kick

case class SwapSuccess(r: RoomInfo)

case class KickSuccess(r: RoomInfo)

//game control
case object StartGame

case class GameStarted(roomInfo: RoomInfo)


case class Pos(x: Int, y: Int)

case class Move(from: Pos, to: Pos)

case object MoveSuccess

case class OtherMove(from: Pos, to: Pos)

case object Surrender

case class EndGame(red: Boolean)


case object GetAllRooms

case class RoomSearchResponse(rooms: Array[(Long, RoomInfo)])


case class CreateRoom(user: User)

case class DestroyRoom(id: Long)

case class CreateSuccess(token: Long)

//-------------20180409,id是房间id
case class InviteMessage(user: User, roomId: Long, mid: Long)
