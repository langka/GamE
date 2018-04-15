package com.bupt.sworld.activity

import android.content.{Context, Intent}
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View.OnClickListener
import android.view.{TextureView, View}
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget._
import com.bupt.sworld.R
import com.bupt.sworld.adapter.{HomeMsgAdapter, JavaOnItemClickListener, RoomAdapter}
import com.bupt.sworld.service._
import sse.xs.msg.room.{InviteMessage, RoomInfo, TalkMessage}
import sse.xs.msg.user.User
import com.bupt.sworld.convert.Implicit._
import com.bupt.sworld.service.MessageService.ConfirmMsg

import scala.collection.mutable.ArrayBuffer


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
  var createNoEnter: View = _
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
    createNoEnter = findViewById(R.id.homepage_create_test)
    roomHint = findViewById(R.id.home_room_hint)
    lastUpdatedRooms = Array()
    roomAdapter = new RoomAdapter(this, lastUpdatedRooms)
    msgAdapter = new HomeMsgAdapter(this, ArrayBuffer())

    nameTextView.setOnClickListener((v: View) => {
      AccountActivity.Start(HomePageActivity.this)
    })


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
          LocalService.currentRid = resp.id
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
        val r = RoomInfo(Array(Some(LocalService.currentUser), None), LocalService.currentUser)
        LocalService.currentRoom = r
        showText("create room:" + success.token)

        RoomActivity.start(HomePageActivity.this)
      }, failure => {
        showText(failure.reason)
      })
    })
    createNoEnter.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        NetWorkService.createRoom(success => {
          showText("create room:" + success.token)
        }, failure => {
          showText(failure.reason)
        })
      }
    })
    sendTextView.setOnClickListener((v: View) => {
      val str = msgEditText.getText.toString.trim
      if (str == "") {
        showText("无法发送空消息")
      } else {
        val msg = TalkMessage(LocalService.currentUser.name, str, IdService.nextId())
        NetWorkService.sendPublicMessage(msg)
        msgAdapter.messages.append(msg)
        msgEditText.setText("")
        msgAdapter.notifyDataSetChanged()
      }
    })
    roomList.setAdapter(roomAdapter)
    msgList.setAdapter(msgAdapter)


    //消息监听器初始化
    MessageService.setPublicMessageListner(new MessageListener {
      override def onMessageArrive(anyRef: AnyRef): Unit = {
        Log.d("HomePageLogger", "received:" + anyRef.toString)
        anyRef match {
          case c: ConfirmMsg => //确认消息，存到辅助
            msgAdapter.helpers.append(c)
            msgAdapter.notifyDataSetChanged()

          case t: TalkMessage =>

            msgAdapter.messages.append(t)
            msgAdapter.notifyDataSetChanged()

          case i: InviteMessage =>
            msgAdapter.messages.append(i)
            msgAdapter.notifyDataSetChanged()
        }
      }
    })

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
      hideLoading()
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

