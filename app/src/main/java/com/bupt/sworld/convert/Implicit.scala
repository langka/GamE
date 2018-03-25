package com.bupt.sworld.convert

import android.view.View
import android.view.View.OnClickListener
import android.widget.{Adapter, AdapterView}
import android.widget.AdapterView.OnItemClickListener

/**
  * Created by xusong on 2018/3/14.
  * About:
  */
object Implicit {
  implicit def convertToListener(f: View => Unit): OnClickListener = {
    new OnClickListener {
      override def onClick(view: View): Unit = f(view)
    }
  }

  implicit def convertToItemListener(f:(AdapterView[_],View,Int,Long)=>Unit):OnItemClickListener = {
   null
  }

}
