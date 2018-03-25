package com.bupt.sworld.adapter

import android.content.Context
import android.view.{View, ViewGroup}
import android.widget.BaseAdapter
import sse.xs.msg.room.RoomInfo

/**
  * Created by xusong on 2018/3/24.
  * About:
  */
class RoomAdapter(ctx:Context,data:Array[(Long,RoomInfo)]) extends BaseAdapter{
  var rooms: Array[(Long, RoomInfo)] = data

  override def getItem(i: Int): AnyRef = rooms(i)

  override def getItemId(i: Int): Long = 0

  override def getView(i: Int, view: View, viewGroup: ViewGroup): View = {
     null
  }

  override def getCount: Int = rooms.length
}
