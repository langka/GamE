package com.bupt.sworld.adapter

import android.content.Context
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, TextView}
import com.bupt.sworld.R
import sse.xs.msg.room.RoomInfo

/**
  * Created by xusong on 2018/3/24.
  * About:
  */
class RoomAdapter(ctx: Context, data: Array[(Long, RoomInfo)]) extends BaseAdapter {
  var rooms: Array[(Long, RoomInfo)] = data

  override def getItem(i: Int): AnyRef = rooms(i)

  override def getItemId(i: Int): Long = 0

  override def getView(i: Int, view: View, viewGroup: ViewGroup): View = {
    val v = if (view == null) {
      val vw = LayoutInflater.from(ctx).inflate(R.layout.item_room, null)
      val holder = new Holder
      holder.id = vw.findViewById(R.id.item_room_name)
      vw.setTag(holder)
      vw
    } else view
    val holder = v.getTag().asInstanceOf[Holder]
    val current = rooms(i)._1
    holder.id.setText(current + "")
    v
  }

  override def getCount: Int = rooms.length
}

class Holder {
  var id: TextView = _
}
