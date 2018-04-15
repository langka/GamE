package com.bupt.sworld.activity

import android.content.{Context, Intent}
import android.os.Bundle
import android.view.View
import android.widget.{ImageView, TextView}
import com.bupt.sworld.R
import com.bupt.sworld.service.LocalService

/**
  * Created by xusong on 2018/3/24.
  * About:
  */
class AccountActivity extends BaseActivity {

  var name: View = _
  var desc: View = _
  var sex: View = _
  var id: View = _
  var mail: View = _
  var registerTime: View = _


  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_account)
    setTitle("账号信息")
    initView()
  }

  def initView(): Unit = {
    name = findViewById(R.id.frag_nickname)
    desc = findViewById(R.id.frag_description)
    sex = findViewById(R.id.frag_sex)
    id = findViewById(R.id.frag_account_id)
    mail = findViewById(R.id.frag_email)
    registerTime = findViewById(R.id.frag_registration_time)
    initRowView(name, R.drawable.ic_exposure_plus_1_black_30dp, "昵称", LocalService.currentUser.name)
    initRowView(desc, R.drawable.ic_wb_incandescent_black_30dp, "个性签名", LocalService.currentUser.description)
    initRowView(sex, R.drawable.ic_face_black_30dp, "年龄", LocalService.currentUser.age + "")
    initRowView(id, R.drawable.ic_perm_identity_black_30dp, "账号",  LocalService.currentUser.id+"")
    initRowView(mail,R.drawable.ic_email_black_30dp,"密码","********")
    initRowView(registerTime, R.drawable.ic_access_time_black_30dp, "胜利统计", LocalService.currentUser.win + "")
  }


  private def initRowView(v: View, imgId: Int, text: String, detail: String) = {
    val icon = v.findViewById(R.id.item_profile_icon).asInstanceOf[ImageView]
    val textView = v.findViewById(R.id.item_profile_text).asInstanceOf[TextView]
    val detailView = v.findViewById(R.id.item_profile_righttext).asInstanceOf[TextView]
    icon.setImageDrawable(getResources.getDrawable(imgId))
    textView.setText(text)
    detailView.setText(detail)
  }


}

object AccountActivity {
  def Start(ctx: Context): Unit = {
    val i = new Intent(ctx, classOf[AccountActivity])
    ctx.startActivity(i)
  }
}
