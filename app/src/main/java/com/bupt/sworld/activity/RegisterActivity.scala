package com.bupt.sworld.activity


import android.content.{Context, Intent}
import android.os.{Bundle, Handler}
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import com.bupt.sworld.R
import com.bupt.sworld.service.{LocalService, NetWorkService}

/**
  * Created by xusong on 2018/4/21.
  * About:
  */
class RegisterActivity extends BaseActivity {

  var myHandler: Handler = _

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_register)
    val name: EditText = findViewById(R.id.name_edittext)
    val pwd: EditText = findViewById(R.id.pwd_edittext)
    val confirm: View = findViewById(R.id.confirm_btn)
    myHandler = new Handler
    confirm.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        val a = name.getText.toString.trim
        val b = pwd.getText.toString.trim
        if (a == "" || b == "") {
          showText("账户或密码不合法，请检查！")
        } else {
          showLoading("LineScalePulseOut")
          myHandler.postDelayed(new Runnable {
            override def run(): Unit = {
              NetWorkService.register(a, b, s => {
                hideLoading()
                LocalService.currentUser = s.user
                HomePageActivity.start(RegisterActivity.this, s.user)
              }, f => {
                hideLoading()
                showText(f.reason)
              })
            }
          }, 1000)
        }
      }

      myHandler.postDelayed(new Runnable {
        override def run(): Unit = {

        }
      }, 1000)

    })

  }

}

object RegisterActivity {
  def Start(ctx: Context): Unit = {
    val i = new Intent(ctx, classOf[RegisterActivity])
    ctx.startActivity(i)
  }
}