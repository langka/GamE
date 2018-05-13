package com.bupt.sworld.adapter

import android.content.Context
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, TextView}
import com.bupt.sworld.R
import com.bupt.sworld.activity.BaseActivity
import sse.xs.msg.user.User

/**
  * Created by xusong on 2018/5/13.
  * About:
  */
class AudienceAdapter(data:List[User],ctx:Context) extends BaseAdapter{
  override def getItem(i: Int): AnyRef = {
    data(i)
  }

  override def getItemId(i: Int): Long = 0

  override def getView(i: Int, view: View, viewGroup: ViewGroup): View = {
    var holder:Aholder = null
    var v:View = null
    if(view==null){
      v = LayoutInflater.from(ctx).inflate(R.layout.item_audience,null)
      holder = new Aholder
      holder.name = v.findViewById(R.id.audience_name)
      holder.des = v.findViewById(R.id.audience_desc)
      v.setTag(holder)
    }else{
      v = view
      holder = v.getTag().asInstanceOf[Aholder]
    }
    val current = getItem(i).asInstanceOf[User]
    holder.name.setText(""+current.name)
    holder.des.setText(""+current.description)

    v
  }

  override def getCount: Int = {
    data.length
  }
}
class Aholder{
  var name:TextView = _
  var des:TextView = _
}
