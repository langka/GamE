package com.bupt.sworld.activity

import android.app.Dialog
import android.content.{Context, Intent}
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.{EditText, ImageView, TextView}
import com.bupt.sworld.R
import com.bupt.sworld.service.{LocalService, NetWorkService}
import sse.xs.msg.user.User

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
    updateView()
    val unclickable = new OnClickListener {
      override def onClick(view: View): Unit = {
        showText("此项信息无法更改！")
      }
    }
    name.setOnClickListener(unclickable)
    id.setOnClickListener(unclickable)
    mail.setOnClickListener(unclickable)

    desc.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        showModifyDialog("修改个人信息-签名", newDesc => {
          if (newDesc == "") {
            showText("签名不可以为空！")
          } else if (newDesc == LocalService.currentUser.description) {
            showText("修改成功！")
          } else {
            val user = LocalService.currentUser.modifyDesc(newDesc)
            NetWorkService.modifyUserInfo(user, newUser => {
              LocalService.currentUser = newUser
              updateView()
            }, reason => {
              showText(reason.reason)
            })

          }

        })
      }
    })

    sex.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        showModifyDialog("修改个人信息-年龄", newAge => {
          try {
            val age = newAge.toInt
            val user = LocalService.currentUser.modifyAge(age)
            NetWorkService.modifyUserInfo(user, newUser => {
              LocalService.currentUser = newUser
              updateView()
            }, f => {
              showText(f.reason)
            })

          } catch {
            case e: Exception =>
              showText("非法的年龄")
          }
        })
      }
    })
    registerTime.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        HistoryActivity.start(AccountActivity.this)
      }
    })


  }


  def updateView(): Unit = {
    initRowView(name, R.drawable.ic_exposure_plus_1_black_30dp, "昵称", LocalService.currentUser.name)
    initRowView(desc, R.drawable.ic_wb_incandescent_black_30dp, "个性签名", if (LocalService.currentUser.description.trim == "no descrption")
      "暂无描述" else LocalService.currentUser.description)
    initRowView(sex, R.drawable.ic_face_black_30dp, "年龄", LocalService.currentUser.age + "")
    initRowView(id, R.drawable.ic_perm_identity_black_30dp, "账号", stringOfId(LocalService.currentUser.id))
    initRowView(mail, R.drawable.ic_email_black_30dp, "密码", "********")
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

  private def showModifyDialog(title: String, onConfirm: String => Unit): Unit = {
    val dialog = new Dialog(this, R.style.dialog)
    val layout = getLayoutInflater.inflate(R.layout.dialog_modify, null)
    val titleView: TextView = layout.findViewById(R.id.dialog_modify_title)
    val content: EditText = layout.findViewById(R.id.dialog_modify_content)
    val confirm: View = layout.findViewById(R.id.dialog_modify_confirm)
    val cancel: View = layout.findViewById(R.id.dialog_modify_cancel)
    titleView.setText(title)
    cancel.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        dialog.dismiss()
      }
    })
    confirm.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        dialog.dismiss()
        val text = content.getText.toString
        onConfirm(text)
      }
    })
    dialog.setCancelable(false)
    dialog.setContentView(layout)
    dialog.show()
  }

  private def stringOfId(x: Int) = {
    val str = x.toString
    if (str.length >= 8)
      str
    else {
      val count = 8 - str.length
      val prefix = List.fill(count)("0").foldLeft("")(_ + _)
      prefix + str
    }
  }

}

object AccountActivity {
  def Start(ctx: Context): Unit = {
    val i = new Intent(ctx, classOf[AccountActivity])
    ctx.startActivity(i)
  }
}
