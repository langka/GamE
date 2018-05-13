package com.bupt.sworld.activity

import android.content.{Context, Intent}
import android.os.Build.VERSION_CODES
import android.os.{Bundle, Handler, Message}
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import com.bupt.sworld.R
import com.bupt.sworld.custom.{ChessView, Piece}
import org.json.{JSONArray, JSONObject}
import sse.xs.msg.game.SimpleMatch
import sse.xs.msg.room.{Move, Pos}
import sse.xs.msg.user.User

import scala.collection.mutable

/**
  * Created by xusong on 2018/5/6.
  * About:
  */
class WatchActivity extends BaseActivity {

  var chessView: ChessView = _
  var auto: TextView = _
  var previous: View = _
  var next: View = _

  @volatile var isAuto = false


  var rname: String = _
  var rid: Int = -1
  var bname: String = _
  var bid: Int = -1
  var steps: String = _

  var currentIndex = 0

  val deadMap = new mutable.HashMap[Int, Piece]

  var end = false

  var autoHandler:Handler = _

  private def initView(): Unit = {
    chessView = findViewById(R.id.chessview)
    auto = findViewById(R.id.auto)
    previous = findViewById(R.id.previous)
    next = findViewById(R.id.next)
    chessView.init(true)
    chessView.setOk(false)
    auto.setText("手动状态")
    auto.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        if (isAuto) {
          //原来自动的，点击以下切换为手动
          auto.setText("手动状态")
          next.setClickable(true)
          previous.setClickable(true)
          isAuto = !isAuto
        } else {
          auto.setText("自动播放中")
          next.setClickable(false)
          previous.setClickable(false)
          isAuto = !isAuto
        }
      }
    })
  }

  private def initData(): Unit = {
    val intent = getIntent
    rname = intent.getStringExtra("rname")
    rid = intent.getIntExtra("rid", -1)
    bname = intent.getStringExtra("bname")
    bid = intent.getIntExtra("bid", -1)
    steps = intent.getStringExtra("moves")
  }

  private def checkData(): Option[JSONObject] = {
    if (steps == null)
      None
    else {
      try {
        val json = new JSONObject(steps)
        Some(json)
      }
      catch {
        case e: Exception =>
          None
      }
    }

  }

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_watch)
    initData()
    initView()
    val op = checkData()
    if (op.isDefined) {
      val steps = op.get.getJSONArray("step")

      def toMove(s: JSONArray): Move = {
        val fx = s.getInt(0)
        val fy = s.getInt(1)
        val tx = s.getInt(2)
        val ty = s.getInt(3)
        Move(Pos(fx, fy), Pos(tx, ty))
      }

      val size = steps.length()
      val movelist = (0 until size).map { i =>
        val c = steps.getJSONArray(i)
        toMove(c)
      }.toList

      autoHandler = new Handler {
        override def handleMessage(msg: Message): Unit = {
          if (isAuto) {
            if (currentIndex <= movelist.length - 1) {
              val step = movelist(currentIndex)
              step match {
                case Move(Pos(-1, -1), k) =>
                  val su = if (currentIndex % 2 == 0) "黑方" else "红方"
                  showText(su + "在这一步投降!")
                case m: Move =>
                  val dead = chessView.getPieceAt(m.to.x, m.to.y)
                  if (dead != null) {
                    deadMap.put(currentIndex, dead)
                  }
                  chessView.confirmNextStep(m.from.x, m.from.y, m.to.x, m.to.y)
                  currentIndex += 1
              }

            } else {

            }
          }
          if (!end)
            this.sendEmptyMessageDelayed(1, 3000)
        }
      }
      autoHandler.sendEmptyMessage(1)


      next.setOnClickListener(new OnClickListener {
        override def onClick(view: View): Unit = {
          if (currentIndex <= movelist.length - 1) {
            val step = movelist(currentIndex)
            step match {
              case Move(Pos(-1, -1), k) =>
                val su = if (currentIndex % 2 == 0) "黑方" else "红方"
                showText(su + "在这一步投降!")
              case m: Move =>
                val dead = chessView.getPieceAt(m.to.x, m.to.y)
                if (dead != null) {
                  deadMap.put(currentIndex, dead)
                }
                chessView.confirmNextStep(m.from.x, m.from.y, m.to.x, m.to.y)
                currentIndex += 1
            }

          } else {
            showText("已经是最后一步了！")
          }
        }
      })


      previous.setOnClickListener(new OnClickListener {
        override def onClick(view: View): Unit = {
          if (currentIndex == 0) {
            showText("已经是第一步了!")
          } else {
            val preIndex = currentIndex - 1
            val step = movelist(preIndex)
            chessView.setPieceAt(step.from.x, step.from.y, chessView.getPieceAt(step.to.x, step.to.y))
            val killed = deadMap.get(preIndex)
            chessView.setPieceAt(step.to.x, step.to.y, killed.orNull)
            chessView.invalidate()
            currentIndex = preIndex
          }
        }

      })


    } else {
      showConfirmCancelDialog(Seq("数据错误", "取消", "确认"), finish(), finish())
    }
  }

  override def onDestroy(): Unit = {
    super.onDestroy()
    end = true
  }


}

object WatchActivity {
  def start(ctx: Context): Unit = {
    val intent = new Intent(ctx, classOf[WatchActivity])
    ctx.startActivity(intent)
  }

  def start(ctx: Context, m: SimpleMatch): Unit = {
    val intent = new Intent(ctx, classOf[WatchActivity])
    intent.putExtra("rname", m.red.name)
    intent.putExtra("rid", m.red.id)
    intent.putExtra("bname", m.black.name)
    intent.putExtra("bid", m.black.id)
    intent.putExtra("moves", m.moves)
    ctx.startActivity(intent)
  }

}
