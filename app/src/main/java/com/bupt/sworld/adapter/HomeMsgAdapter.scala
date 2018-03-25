package com.bupt.sworld.adapter

import android.content.Context
import android.view.{View, ViewGroup}
import android.widget.BaseAdapter
import com.bupt.sworld.activity.BaseActivity
import sse.xs.msg.room.TalkMessage

/**
  * Created by xusong on 2018/3/24.
  * About:
  */
class HomeMsgAdapter(ctx:Context,datas:Array[TalkMessage]) extends BaseAdapter{

  var messages = datas
  override def getItem(i: Int): AnyRef = messages(i)

  override def getItemId(i: Int): Long = 0

  override def getView(i: Int, view: View, viewGroup: ViewGroup): View = {
    null
  }

  override def getCount: Int = messages.length
}
