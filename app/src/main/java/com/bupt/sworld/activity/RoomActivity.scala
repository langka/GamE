package com.bupt.sworld.activity
import android.content.{Context, Intent}
import android.os.Bundle

/**
  * Created by xusong on 2018/3/24.
  * About:
  */
class RoomActivity extends BaseActivity{
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
  }
}
object RoomActivity{
  def start(ctx:Context): Unit ={
    val intent = new Intent(ctx,classOf[RoomActivity])
    ctx.startActivity(intent)
  }

}
