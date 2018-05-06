package com.bupt.sworld.adapter

import java.text.SimpleDateFormat

import android.content.Context
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, ImageView, TextView}
import com.bupt.sworld.R
import com.bupt.sworld.activity.BaseActivity
import com.bupt.sworld.service.LocalService
import com.bupt.sworld.service.MessageService.ConfirmMsg
import sse.xs.msg.room.{InviteMessage, TalkMessage}

import scala.collection.mutable
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

  val timeMap = new mutable.HashMap[AnyRef, Long]

  def appendNew(x: AnyRef): Unit = {
    messages.append(x)
    timeMap.put(x, System.currentTimeMillis())
  }

  override def getItem(i: Int): AnyRef = messages(i)

  override def getItemId(i: Int): Long = 0

  override def getView(i: Int, view: View, viewGroup: ViewGroup): View = {
    var holder: ViewHolder = null
    val vvv =
      if (view == null) {
        holder = new ViewHolder
        val inflater = LayoutInflater.from(ctx)
        val v = inflater.inflate(R.layout.item_homemsg2, null)
        holder.speaker = v.findViewById(R.id.item_homemsg_speaker)
        holder.time = v.findViewById(R.id.item_homemsg_time)
        holder.stateTextView = v.findViewById(R.id.item_homemsg_state_text)
        holder.stateImg = v.findViewById(R.id.item_homemsg_state_img)
        holder.content = v.findViewById(R.id.item_homemsg_content)
        v.setTag(holder)
        v
      } else {
        holder = view.getTag().asInstanceOf[ViewHolder]
        view
      }

    messages(i) match {
      case talk: TalkMessage =>
        holder.speaker.setText(talk.speaker + ": ")
        val time = timeMap.get(talk)
        holder.time.setText(time.map(longToTime).getOrElse("Unknown time"))
        val alternative = helpers.find { h =>
          h.id == talk.id
        }
        val tip = if (talk.speaker != LocalService.currentUser.name) {
          holder.stateImg.setImageDrawable(ctx.getResources.getDrawable(R.drawable.ic_tishi))
          "[消息]"
        } else {
          alternative.map { a =>
            holder.stateImg.setImageDrawable(ctx.getResources.getDrawable(R.drawable.ic_queren))
            "[已送达]"
          }.getOrElse({
            holder.stateImg.setImageDrawable(ctx.getResources.getDrawable(R.drawable.ic_warning_))
            "[未确认]"
          })
        }
        holder.stateTextView.setText(tip)
        holder.content.setText(talk.detail)

      case invite: InviteMessage =>
        holder.speaker.setText(invite.user.name + ": ")
        val showed = stringOfInvite(invite)
        holder.content.setText("加入房间" + invite.roomId + "一起玩耍吧！")
        holder.stateTextView.setText("[邀请]")
        holder.stateImg.setImageDrawable(ctx.getResources.getDrawable(R.drawable.ic_tishi))
        val time = timeMap.get(invite)
        holder.time.setText(time.map(longToTime).getOrElse(""))

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

  private def longToTime(l:Long) = {
    val format = new SimpleDateFormat("HH:mm")
    val time = format.format(l)
    time
  }
}

class ViewHolder {
  var speaker: TextView = _
  var content: TextView = _
  var time: TextView = _
  var stateTextView: TextView = _
  var stateImg: ImageView = _
}
