package com.bupt.sworld.actor.common

import akka.actor.Actor
import android.util.Log

/**
  * Created by xusong on 2018/3/25.
  * About:
  */
trait ActorLogger {
  //自身类型，该trait只会被允许混入到actor中
  this: Actor =>

  def log(info: String): Unit = {
    Log.d(getClass.getName, info)
  }

}
