package com.bupt.sworld.adapter

import android.content.Context
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, TextView}
import com.bupt.sworld.R
import com.bupt.sworld.service.LocalService
import com.bupt.sworld.service.MessageService.ConfirmMsg
import sse.xs.msg.room.{Move, OtherMove, Pos, TalkMessage}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by xusong on 2018/4/14.
  * About:
  */
class GameMessageAdapter(ctx: Context, dt: ArrayBuffer[AnyRef]) extends BaseAdapter {
  var datas = dt

  var confirms: ArrayBuffer[Long] = ArrayBuffer()

  override def getItem(i: Int): AnyRef = {
    datas(i)
  }

  override def getItemId(i: Int): Long = {
    datas.length
  }

  override def getView(i: Int, view: View, viewGroup: ViewGroup): View = {
    var vholder = new ViHolder
    val vvv =
      if (view == null) {
        val inflater = LayoutInflater.from(ctx)
        val v = inflater.inflate(R.layout.item_room_msg, null)
        vholder.content = v.findViewById(R.id.message_content)
        v.setTag(vholder)
        v
      } else {
        vholder = view.getTag().asInstanceOf[ViHolder]
        view
      }

    val current = getItem(i)
    val str = current match {
      case m: Move =>
        "我的移动: " + stringOfPos(m.from, m.to)
      case o: OtherMove =>
        "敌方移动: " + stringOfPos(o.from, o.to)
      case t: TalkMessage =>
        val tip = if (t.speaker == LocalService.currentUser.name) {
          if (confirms.contains(t.id)) "已送达" else "未确认"
        } else {
          ""
        }
        t.speaker + " : " + t.detail + "  " + tip
    }
    vholder.content.setText(str)
    vvv
  }

  override def getCount: Int = {
    datas.size
  }

  private def stringOfPos(f: Pos, t: Pos) = {
    "( " + f.x + "," + f.y + " -> " + t.x + "," + t.y + " )"
  }
}

class ViHolder {
  var content: TextView = _
}
