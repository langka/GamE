package com.bupt.sworld.activity

import android.content.{Context, Intent}
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{EditText, ListView, TextView}
import com.bupt.sworld.R
import com.bupt.sworld.adapter.RoomMsgAdapter
import com.bupt.sworld.service._
import sse.xs.msg.user.User
import com.bupt.sworld.convert.Implicit._
import sse.xs.msg.CommonFailure
import sse.xs.msg.room._

import scala.collection.mutable.ArrayBuffer

/**
  * Created by xusong on 2018/3/24.
  * About:
  */
class RoomActivity extends BaseActivity {

  var redView: View = _
  var blackView: View = _
  var swap: View = _
  var leave: View = _
  var kick: View = _
  var start: View = _
  var msgEdit: EditText = _
  var sendView: View = _
  var msgList: ListView = _
  var inviteView: View = _

  var roomMsgAdapter: RoomMsgAdapter = _

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_room)
    initView()
  }

  def initView(): Unit = {
    redView = findViewById(R.id.red_container)
    blackView = findViewById(R.id.black_container)
    swap = findViewById(R.id.swap)
    leave = findViewById(R.id.leave)
    kick = findViewById(R.id.kick)
    start = findViewById(R.id.start)
    msgEdit = findViewById(R.id.input_message)
    sendView = findViewById(R.id.send)
    msgList = findViewById(R.id.room_talk)
    inviteView = findViewById(R.id.invite)

    sendView.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        val str = msgEdit.getText.toString.trim
        if(str==null){
          showText("消息内容不可以为空!")
        }else{
          val t = TalkMessage(LocalService.currentUser.name,str,IdService.nextId())
          msgEdit.setText("")
          NetWorkService.sendRoomMessage(t)
        }
      }
    })

    inviteView.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        val i = InviteMessage(LocalService.currentUser, LocalService.currentRid, IdService.nextId())
        NetWorkService.sendInviteToRoom(i)
        roomMsgAdapter.add(i)
      }
    })
    swap.setOnClickListener((v: View) => {
      NetWorkService.swap(LocalService.currentRid, r => {
        LocalService.currentRoom = r
        updateRoomInfoView()
      }, f => {
        showText(f.reason)
        updateRoomInfoView()
      })
    })
    leave.setOnClickListener((v: View) => {
      LocalService.currentRid = -1
      LocalService.currentRoom = null
      NetWorkService.leave()
      finish()
    })
    kick.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        showText("以和为贵，为什么要踢人呢?")
      }
    })
    start.setOnClickListener((v: View) => {
      if (LocalService.currentRoom.players.forall(_.isDefined)) {
        NetWorkService.startGame(info => {
          LocalService.currentRoom = info
          GameActivity.Start(RoomActivity.this)
        }, faiure => {
          showText(faiure.reason)
        })
      } else {
        showText("房间人数不足，无法开始游戏")
      }
    })
    roomMsgAdapter = new RoomMsgAdapter(this, ArrayBuffer())

    MessageService.setRoomMsgListener(new MessageListener {
      override def onMessageArrive(anyRef: AnyRef): Unit = {
        //所有的room操作都必须返回roominfo，从而进行同步
        anyRef match {

          case g: GameStarted =>
            //在进入游戏时进行一次同步
            LocalService.currentRoom = g.roomInfo
            updateRoomInfoView()
            GameActivity.Start(RoomActivity.this)

          case t: TalkMessage =>
            roomMsgAdapter.add(t)

          case j: NewUserEnter =>
            roomMsgAdapter.add(j)
            LocalService.currentRoom = j.roomInfo
            updateRoomInfoView()
          case o: OtherLeaveRoom =>
            roomMsgAdapter.add(o)
            LocalService.currentRoom = o.roomInfo
            updateRoomInfoView()
          // TODO: 添加交换，kick，leave等消息

          case s: SwapSuccess =>
            roomMsgAdapter.add(s)
            LocalService.currentRoom = s.r
            updateRoomInfoView()
        }

        roomMsgAdapter.notifyDataSetChanged()
      }
    })

    msgList.setAdapter(roomMsgAdapter)
    updateRoomInfoView()
  }

  private def updateRoomInfoView(): Unit = {
    val players = LocalService.currentRoom.players
    updatePlayerView(redView, players(0))
    updatePlayerView(blackView, players(1))
  }

  private def updatePlayerView(v: View, player: Option[User]): Unit = {
    val infoContainer: View = v.findViewById(R.id.header_info_container)
    val nameTextView: TextView = v.findViewById(R.id.head_name)
    val levelTextView: TextView = v.findViewById(R.id.head_level)
    val win: TextView = v.findViewById(R.id.head_win)
    val lost: TextView = v.findViewById(R.id.head_lost)
    val hint: View = v.findViewById(R.id.no_enemy)
    player match {
      case None =>
        infoContainer.setVisibility(View.INVISIBLE)
        hint.setVisibility(View.VISIBLE)
      case Some(user) =>
        infoContainer.setVisibility(View.VISIBLE)
        hint.setVisibility(View.INVISIBLE)
        nameTextView.setText(user.name)
        levelTextView.setText("level: " + levelOf(user))
        win.setText(user.win + "")
        lost.setText(user.lose + "")
    }
  }

  private def levelOf(p: User) = {
    (p.win * 7 + p.lose * 3) / 10
  }

  override def onBackPressed(): Unit = {
    super.onBackPressed()
    Log.d("RoomActivityLogger", "back pressed!,quit room")
    NetWorkService.leave()
  }
}

object RoomActivity {
  def start(ctx: Context): Unit = {
    val intent = new Intent(ctx, classOf[RoomActivity])
    ctx.startActivity(intent)
  }

}
