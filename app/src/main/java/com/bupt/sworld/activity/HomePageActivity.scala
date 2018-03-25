package com.bupt.sworld.activity

import android.content.{Context, Intent}
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.{TextureView, View}
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget._
import com.bupt.sworld.R
import com.bupt.sworld.adapter.{HomeMsgAdapter, JavaOnItemClickListener, RoomAdapter}
import com.bupt.sworld.service.{LocalService, NetWorkService}
import sse.xs.msg.room.RoomInfo
import sse.xs.msg.user.User
import com.bupt.sworld.convert.Implicit._


/**
  * Created by xusong on 2018/3/15.
  * About:
  */
class HomePageActivity extends BaseActivity {

  var nameTextView: TextView = _
  var roomList: ListView = _
  var msgList: ListView = _
  var sendTextView: TextView = _
  var msgEditText: EditText = _
  var checkBox: CheckBox = _
  var createView: View = _
  var refreshView: View = _
  var roomHint: View = _


  var roomAdapter: RoomAdapter = _
  var msgAdapter: HomeMsgAdapter = _

  var lastUpdatedRooms: Array[(Long, RoomInfo)] = _


  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_homepage)
    initView()
    findAllRooms()
  }

  def initView(): Unit = {
    nameTextView = findViewById(R.id.home_name)
    roomList = findViewById(R.id.home_rooms)
    msgList = findViewById(R.id.sended_message)
    msgEditText = findViewById(R.id.home_msg)
    sendTextView = findViewById(R.id.home_send)
    checkBox = findViewById(R.id.home_check)
    createView = findViewById(R.id.homepage_create)
    refreshView = findViewById(R.id.homepage_refresh)
    roomHint = findViewById(R.id.home_room_hint)
    lastUpdatedRooms = Array()
    roomAdapter = new RoomAdapter(this, lastUpdatedRooms)
    msgAdapter = new HomeMsgAdapter(this, Array())
    checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener {
      override def onCheckedChanged(compoundButton: CompoundButton, b: Boolean): Unit = {
        updateRoomListView()
      }
    })
    roomList.setOnItemClickListener(new JavaOnItemClickListener {
      override protected def onItemClick(location: Int): Unit = {
        showLoading()
        val current = roomAdapter.getItem(location).asInstanceOf[(Long, RoomInfo)]
        showText("ROOM:" + current._1)
        NetWorkService.enterRoom(current._1, resp => {
          hideLoading()
          LocalService.currentRoom = resp.roomInfo
          RoomActivity.start(HomePageActivity.this)
        }, failure => {
          hideLoading()
          showText(failure.reason)
        })
      }
    })
    refreshView.setOnClickListener((v: View) => {
      findAllRooms()
    })
    createView.setOnClickListener((v: View) => {
      NetWorkService.createRoom(success => {
        showText("create room:" + success.token)
      }, failure => {
        showText(failure.reason)
      })
    })
    roomList.setAdapter(roomAdapter)
    msgList.setAdapter(msgAdapter)
    updateUserInfoView()
  }

  def updateUserInfoView(): Unit = {
    nameTextView.setText(LocalService.currentUser.name)
  }

  // TODO: 额外判断是否有房间，没有显示错误提示
  def updateRoomListView(): Unit = {
    val checked = checkBox.isChecked

    def playerCount(players: Array[Option[User]]) = {
      players.count(_.isDefined)
    }

    //如果启用了过滤，就只显示有一个空位的房间,否则显示全部
    val datas = if (checked) lastUpdatedRooms.filter { p =>
      val players = p._2.players
      playerCount(players) == 1
    }
    else lastUpdatedRooms
    roomAdapter.rooms = datas
    if (roomAdapter.getCount == 0) {
      showText("当前没有房间!")
      roomHint.setVisibility(View.VISIBLE)
    }
    else {
      roomAdapter.notifyDataSetChanged()
      roomHint.setVisibility(View.INVISIBLE)
    }
  }

  def findAllRooms(): Unit = {
    showLoading()
    NetWorkService.findAllRooms(resp => {
      lastUpdatedRooms = resp.rooms
      updateRoomListView()
    }, failure => {
      hideLoading()
      showText(failure.reason)
      updateRoomListView()
    })
  }

}

object HomePageActivity {

  def start(ctx: Context, user: User): Unit = {
    val intent = new Intent(ctx, classOf[HomePageActivity])
    ctx.startActivity(intent)

  }
}

