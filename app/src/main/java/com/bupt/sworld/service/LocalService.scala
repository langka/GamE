package com.bupt.sworld.service

import sse.xs.msg.room.RoomInfo
import sse.xs.msg.user.User

/**
  * Created by xusong on 2018/3/24.
  * About:
  */
object LocalService {
  var currentUser:User = _
  var currentRoom:RoomInfo = _
  var currentRid:Long = _
}
