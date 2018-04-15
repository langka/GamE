package com.bupt.sworld.adapter

import android.content.Context
import android.view.{View, ViewGroup}
import android.widget.BaseAdapter

import scala.collection.mutable.ArrayBuffer

/**
  * Created by xusong on 2018/4/14.
  * About:
  */
class GameMessageAdapter(ctx: Context, dt: ArrayBuffer[AnyRef]) extends BaseAdapter {
  var datas = dt

  override def getItem(i: Int): AnyRef = {
    datas(i)
  }

  override def getItemId(i: Int): Long = {
    0
  }

  override def getView(i: Int, view: View, viewGroup: ViewGroup): View = {
    null
  }

  override def getCount: Int = {
    datas.size
  }
}
