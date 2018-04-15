package com.bupt.sworld.service

import java.util.concurrent.atomic.AtomicLong

/**
  * Created by xusong on 2018/4/15.
  * About:
  */
object IdService {
  private val id = new AtomicLong(0)

  def nextId(): Long = {
    id.getAndIncrement()
  }
}
