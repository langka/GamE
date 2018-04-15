package com.bupt.sworld.adapter

import android.content.Context
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, TextView}
import com.bupt.sworld.R
import com.bupt.sworld.activity.BaseActivity
import com.bupt.sworld.service.MessageService.ConfirmMsg
import sse.xs.msg.room.{InviteMessage, TalkMessage}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by xusong on 2018/3/24.
  * About:
  */
class HomeMsgAdapter(ctx: Context, datas: ArrayBuffer[AnyRef]) extends BaseAdapter {

  //可能有invite和talk!
  val messages = datas
  //confirm
  val helpers: ArrayBuffer[ConfirmMsg] = ArrayBuffer()

  override def getItem(i: Int): AnyRef = messages(i)

  override def getItemId(i: Int): Long = 0

  override def getView(i: Int, view: View, viewGroup: ViewGroup): View = {
    var holder: ViewHolder = null
    val vvv =
      if (view == null) {
        holder = new ViewHolder
        val inflater = LayoutInflater.from(ctx)
        val v = inflater.inflate(R.layout.item_room_msg, null)
        holder.content = v.findViewById(R.id.message_content)
        v.setTag(holder)
        v
      } else {
        holder = view.getTag().asInstanceOf[ViewHolder]
        view
      }
    messages(i) match {
      case talk: TalkMessage =>
        val alternative = helpers.find { h =>
          h.id == talk.id
        }
        val tip = alternative.map { a =>
          "已送达"
        }.getOrElse("未确认")
        val showed = stringOfTalk(talk) + "  " + tip
        holder.content.setText(showed)

      case invite: InviteMessage =>
        val showed = stringOfInvite(invite)
        holder.content.setText(showed)

    }

    vvv
  }

  override def getCount: Int = messages.length

  private def stringOfInvite(i: InviteMessage) = {
    i.user.name + " 邀请你加入房间一起玩!"
  }

  private def stringOfTalk(t: TalkMessage) = {
    t.speaker + ": " + t.detail
  }
}

class ViewHolder {
  var content: TextView = _
}
