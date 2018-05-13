package sse.xs.msg.game

import sse.xs.msg.user.User

/**
  * Created by xusong on 2018/5/7.
  * About:
  */
case class GameHistories(matches: List[SimpleMatch])

case class SimpleMatch(red: User, black: User, winner: Int, moves: String,gid:Int)

case class GetGameHistory(id: Int)