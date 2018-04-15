package com.bupt.sworld.activity

import android.content.{Context, Intent}
import android.os.Bundle
import android.widget.ListView
import com.bupt.sworld.R
import com.bupt.sworld.adapter.GameMessageAdapter
import com.bupt.sworld.custom.ChessView
import com.bupt.sworld.custom.ChessView.OnStepMoveListener
import com.bupt.sworld.service.{LocalService, MessageListener, MessageService, NetWorkService}
import sse.xs.msg.room.{Move, OtherMove, Pos, TalkMessage}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by xusong on 2018/4/2.
  * About:
  */
class GameActivity extends BaseActivity {

  var chessView: ChessView = _
  var gameEventList: ListView = _

  //可能move,othermove,talk
  val messages: ArrayBuffer[AnyRef] = ArrayBuffer()

  var gameMessageAdapter: GameMessageAdapter = _

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_game)
    initView()
  }

  private def initView(): Unit = {
    chessView = findViewById(R.id.chview)
    gameEventList = findViewById(R.id.game_list)

    gameMessageAdapter = new GameMessageAdapter(this, messages)
    gameEventList.setAdapter(gameMessageAdapter)

    val cur = LocalService.currentUser
    val redPlayer = LocalService.currentRoom.players(0)
    val isRed = redPlayer exists (_.name == cur.name)
    chessView.init(isRed)
    chessView.setStepMoveListener(new OnStepMoveListener {
      override def onMove(fx: Int, fy: Int, tx: Int, ty: Int): Unit = {
        val from = Pos(fx, fy)
        val to = Pos(tx, ty)
        val move = Move(from, to)
        NetWorkService.movePiece(move, s => {
          //s should be ignored
          messages.append(move)
          chessView.confirmNextStep(fx, fy, tx, ty)
          gameMessageAdapter.notifyDataSetChanged()
        }, failure => {
          showText(failure.reason)
        })

      }
    })

    //初始化消息监听
    MessageService.setGameMsgListener(new MessageListener {
      override def onMessageArrive(anyRef: AnyRef): Unit = {
        anyRef match {
          case t: TalkMessage =>
            messages.append(t)
            gameMessageAdapter.notifyDataSetChanged()
          case o: OtherMove =>
            //进行移动
            val from = o.from
            val to = o.to
            chessView.confirmNextStep(from.x, from.y, to.x, to.y)
            messages.append(o)
            gameMessageAdapter.notifyDataSetChanged()

        }
      }
    })

  }

}

object GameActivity {
  def Start(ctx: Context): Unit = {
    val i = new Intent(ctx, classOf[GameActivity])
    ctx.startActivity(i)
  }
}
