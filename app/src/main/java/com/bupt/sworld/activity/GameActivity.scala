package com.bupt.sworld.activity

import java.text.SimpleDateFormat
import java.time.{LocalDate, LocalDateTime}
import java.util.stream.Collectors

import android.app.Dialog
import android.content.{Context, Intent}
import android.os.Bundle
import android.view.{LayoutInflater, View}
import android.view.View.OnClickListener
import android.widget.{EditText, ListView, TextView}
import com.bupt.sworld.R
import com.bupt.sworld.adapter.GameMessageAdapter
import com.bupt.sworld.custom.ChessView
import com.bupt.sworld.custom.ChessView.OnStepMoveListener
import com.bupt.sworld.service.MessageService.ConfirmMsg
import com.bupt.sworld.service._
import sse.xs.msg.room._

import scala.collection.mutable.ArrayBuffer
import scala.util.Random


/**
  * Created by xusong on 2018/4/2.
  * About:
  */
class GameActivity extends BaseActivity {

  var chessView: ChessView = _
  var gameEventList: ListView = _
  var surrenderView: View = _
  var sendView: View = _
  var msgContentEdit: EditText = _
  var testView: TextView = _

  var gameEnded = false

  var iswatch = true
  //可能move,othermove,talk
  val messages: ArrayBuffer[AnyRef] = ArrayBuffer()

  var gameMessageAdapter: GameMessageAdapter = _

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_game)
    iswatch = getIntent.getBooleanExtra("iswatch", true)
    initView()
  }

  private def initView(): Unit = {
    chessView = findViewById(R.id.chview)
    if(iswatch){
      chessView.setOk(false)
    }
    gameEventList = findViewById(R.id.game_list)
    testView = findViewById(R.id.test_pop)
    surrenderView = findViewById(R.id.surrender)
    sendView = findViewById(R.id.send)
    msgContentEdit = findViewById(R.id.msg_content)
    if(iswatch){
      surrenderView.setOnClickListener(new OnClickListener {
        override def onClick(view: View): Unit = {
          showText("你没有权限!")
        }
      })
    }else{
      surrenderView.setOnClickListener(new OnClickListener {
        override def onClick(view: View): Unit = {
          showConfirmCancelDialog(Seq("你要投降吗?", "确认", "取消"), {
            NetWorkService.surrender()
            finish()
          }, {})
        }
      })
      testView.setOnClickListener(new OnClickListener {
        override def onClick(view: View): Unit = {
          val r = new Random()
          showAnalyseDialog(r.nextBoolean())
        }
      })

    }


    sendView.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        val str = msgContentEdit.getText.toString.trim
        if (str == "") {
          showText("无法发送空消息!")
        } else {
          msgContentEdit.setText("")
          val m = TalkMessage(LocalService.currentUser.name, str, IdService.nextId())
          NetWorkService.sendGameMessage(m)
        }
      }
    })


    gameMessageAdapter = new GameMessageAdapter(this, messages,iswatch)
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
          chessView.getGameState match {
            case 0 => //do nothing,go on the game
            case 1 => //red win the game
              // TODO: 添加其他代码，如弹出对话框，对战统计等等
              showAnalyseDialog(true)
              NetWorkService.endGame(true)
            case 2 =>
              showAnalyseDialog(false)
              NetWorkService.endGame(false)
          }
        }, failure => {
          showAnalyseDialog(true)
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
            chessView.getGameState match {
              case 0 => //do nothing,go on the game
              case 1 => //red win the game
                // TODO: 添加其他代码，如弹出对话框，对战统计等等
                showAnalyseDialog(true)
                NetWorkService.endGame(true)
              case 2 =>
                showAnalyseDialog(false)
                NetWorkService.endGame(false)
            }

          case c: ConfirmMsg =>
            gameMessageAdapter.confirms.append(c.id)
            gameMessageAdapter.notifyDataSetChanged()

          case OtherSurrender =>
            val redWin = LocalService.currentRoom.players(0).exists(_.name == LocalService.currentUser.name)
            showAnalyseDialog(redWin)

          case _ =>
            showText("unknown message received!")
        }
      }
    })

  }


  override def onBackPressed(): Unit = {
    if (!gameEnded) {
      //游戏尚未结束
      showConfirmCancelDialog(Seq("退出游戏会被判定为失败!", "确认", "取消"), {
        NetWorkService.surrender()
        finish()
      }, {})
    } else {
      //do nothing
    }

  }

  def showAnalyseDialog(redWin: Boolean): Unit = {
    val tip = if (redWin) {
      val base = "红方 "
      val name = LocalService.currentRoom.players(0).map(_.name) getOrElse ("Unknown")
      base + name
    } else {
      val base = "黑方 "
      val name = LocalService.currentRoom.players(1).map(_.name) getOrElse ("Unknown")
      base + name
    }
    val dialog = new Dialog(this, R.style.dialog)
    val layout = LayoutInflater.from(this).inflate(R.layout.dialog_analyse, null)
    val time = longToTime(System.currentTimeMillis())
    val timeView: TextView = layout.findViewById(R.id.analyse_time)
    timeView.setText(time)
    val winnerView: TextView = layout.findViewById(R.id.dialog_winner)
    winnerView.setText(tip)
    val redView: TextView = layout.findViewById(R.id.dialog_content1)
    val blackView: TextView = layout.findViewById(R.id.dialog_content2)
    val redText = "红方失子:" + chessView.analyse(chessView.getRedDead)
    val blackText = "黑方失子:" + chessView.analyse(chessView.getBlackDead)
    redView.setText(redText)
    blackView.setText(blackText)
    val watchMore: View = layout.findViewById(R.id.dialog_more)
    val confirmView: View = layout.findViewById(R.id.dialog_confirm)
    confirmView.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        dialog.dismiss()
        GameActivity.this.finish()
      }
    })
    watchMore.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        WatchActivity.start(GameActivity.this)
        dialog.dismiss()

        GameActivity.this.finish()
      }
    })
    dialog.setCancelable(true)
    dialog.setContentView(layout)
    dialog.show()


  }

  def stringOfLost(): Unit = {

  }

  private def longToTime(l: Long) = {
    val format = new SimpleDateFormat("HH:mm")
    val time = format.format(l)
    time
  }

}

object GameActivity {
  def Start(ctx: Context, iswatch: Boolean): Unit = {
    val i = new Intent(ctx, classOf[GameActivity])
    i.putExtra("iswatch", iswatch)
    ctx.startActivity(i)
  }
}
