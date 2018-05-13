package com.bupt.sworld.activity

import android.content.{Context, Intent}
import android.os.Bundle
import android.widget.ListView
import com.bupt.sworld.R
import com.bupt.sworld.adapter.{HistoryAdapter, JavaOnItemClickListener}
import com.bupt.sworld.service.{LocalService, NetWorkService}
import sse.xs.msg.game.{GameHistories, SimpleMatch}

/**
  * Created by xusong on 2018/5/6.
  * About:
  */
class HistoryActivity extends BaseActivity {
  var listView: ListView = _
  var adapter: HistoryAdapter = _

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_history)
    initView()
    requireData()
  }


  private def initView(): Unit = {
    listView = findViewById(R.id.history_list)
    adapter = new HistoryAdapter(null, this)
    listView.setAdapter(adapter)
    listView.setOnItemClickListener(new JavaOnItemClickListener {
      override protected def onItemClick(location: Int): Unit = {
        val current = adapter.getItem(location).asInstanceOf[SimpleMatch]
        WatchActivity.start(HistoryActivity.this, current)
      }
    })
  }

  private def requireData(): Unit = {
    showLoading()
    //增加点延迟，loading一会，避免屏幕闪动一下
    NetWorkService.getGameHistories(LocalService.currentUser.id, g => {
      handler.postDelayed(new Runnable {
        override def run(): Unit = {
          hideLoading()
          updateListView(g)
        }
      }, 1200)

    }, c => {
      handler.postDelayed(new Runnable {
        override def run(): Unit = {
          hideLoading()
          showText(c.reason)
        }
      }, 800)

    })
  }

  private def updateListView(g: GameHistories): Unit = {
    adapter.updateMatches(g)
  }

}

object HistoryActivity {
  def start(ctx: Context): Unit = {
    val intent = new Intent(ctx, classOf[HistoryActivity])
    ctx.startActivity(intent)
  }
}
