package com.bupt.sworld.adapter

import java.time.LocalDate

import android.content.Context
import android.os.Build.VERSION_CODES
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, ImageView, TextView}
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
      val vw = LayoutInflater.from(ctx).inflate(R.layout.item_room2, null)
      val holder = new Holder
      holder.stateImg = vw.findViewById(R.id.item_room_state)
      holder.roomId = vw.findViewById(R.id.item_room_id)
      holder.roomMaster = vw.findViewById(R.id.item_room_master)
      holder.roomTime = vw.findViewById(R.id.item_room_time)
      holder.roomCount = vw.findViewById(R.id.item_room_count)
      vw.setTag(holder)
      vw
    } else view
    val holder = v.getTag().asInstanceOf[Holder]
    val current = rooms(i)._1
    holder.roomId.setText("房间号: " + current)
    holder.roomMaster.setText("房主: " + rooms(i)._2.master.name)
    val count = rooms(i)._2.players.count(_.isDefined)
    if (count == 1) {
      holder.stateImg.setImageDrawable(ctx.getResources.getDrawable(R.drawable.waiting))
      holder.roomCount.setText("1/2")
    } else {
      holder.stateImg.setImageDrawable(ctx.getResources.getDrawable(R.drawable.full))
      holder.roomCount.setText("2/2")
    }
    holder.roomTime.setText("22:05")

    v
  }

  override def getCount: Int = rooms.length
}

class Holder {
  var stateImg: ImageView = _
  var roomId: TextView = _
  var roomMaster: TextView = _
  var roomTime: TextView = _
  var roomCount: TextView = _
}
