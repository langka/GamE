package com.bupt.sworld.adapter

import android.content.Context
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, ImageView, TextView}
import com.bupt.sworld.R
import com.bupt.sworld.service.LocalService
import sse.xs.msg.game.{GameHistories, SimpleMatch}

import scala.collection.mutable

/**
  * Created by xusong on 2018/5/7.
  * About:
  */
class HistoryAdapter(g: GameHistories, ctx: Context) extends BaseAdapter {
  private var matches = if (g == null) List() else g.matches

  def updateMatches(g: GameHistories): Unit = {
    matches = if (g == null) List() else g.matches
    notifyDataSetChanged()
  }

  override def getItem(i: Int): AnyRef = matches(i)

  override def getItemId(i: Int): Long = 0

  override def getView(i: Int, view: View, viewGroup: ViewGroup): View = {
    val returnedView = if (view == null) {
      val tempView = LayoutInflater.from(ctx).inflate(R.layout.item_history, null)
      val tempH = new HistoryHolder
      tempH.winImage = tempView.findViewById(R.id.item_history_win)
      tempH.red = tempView.findViewById(R.id.item_history_red)
      tempH.black = tempView.findViewById(R.id.item_history_black)
      tempH.count = tempView.findViewById(R.id.item_history_count)
      tempView.setTag(tempH)
      tempView
    } else {
      view
    }
    val holder = returnedView.getTag().asInstanceOf[HistoryHolder]
    val current = getItem(i).asInstanceOf[SimpleMatch]
    if (LocalService.currentUser.id == current.winner) {
      holder.winImage.setImageDrawable(ctx.getResources.getDrawable(R.drawable.ic_victory))
    } else {
      holder.winImage.setImageDrawable(ctx.getResources.getDrawable(R.drawable.ic_failure))
    }
    holder.red.setText("红方: " + current.red.name)
    holder.black.setText("黑方: " + current.black.name)
    holder.count.setText("总步数: " + computeSteps(current.moves))

    returnedView
  }

  override def getCount: Int = matches.size


  val map = new mutable.HashMap[String, Int]

  private def computeSteps(s: String) = {
    map.getOrElse(s, {
      val i = s.count(_=='[')-1
      map.put(s, i)
      i
    })
  }

}

class HistoryHolder {
  var winImage: ImageView = _
  var red: TextView = _
  var black: TextView = _
  var count: TextView = _


}