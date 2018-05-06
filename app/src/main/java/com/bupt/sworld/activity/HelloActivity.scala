package com.bupt.sworld.activity

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSelection, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import android.content.pm.PackageManager
import android.os.Build.VERSION_CODES
import android.os.{Bundle, Handler}
import android.view.View
import android.view.View.OnClickListener
import android.widget.{EditText, LinearLayout, RelativeLayout, TextView}
import com.bupt.sworld.R
import com.bupt.sworld.actor.common.Actors
import com.bupt.sworld.convert.Implicit._
import com.bupt.sworld.service.NetWorkService

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by xusong on 2018/3/12.
  * About:
  */
class HelloActivity extends BaseActivity {

  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  var root: View = _

  var header: View = _

  var moverView: View = _

  var accountEditText: EditText = _ //手机号码域

  var pwdEditText: EditText = _

  var submitTextView: View = _ //登录按钮

  var registerTextView: View = _ //注册文字

  var otherWay: View = _


  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    //初始化这个世界
    initialiseTheWorld()
    //
    initView()
    //尝试连接至服务器
    //connect()

  }

  def initialiseTheWorld(): Unit = {
    handler = new Handler()
    Actors.init(handler)
  }

  def initView(): Unit = {
    root = findViewById(R.id.innerroot)
    header = findViewById(R.id.header)
    moverView = findViewById(R.id.content_container)
    accountEditText = findViewById(R.id.login_account)
    pwdEditText = findViewById(R.id.login_pwd)
    submitTextView = findViewById(R.id.login_submit)
    registerTextView = findViewById(R.id.login_register)
    registerTextView.setOnClickListener(new OnClickListener {
      override def onClick(view: View): Unit = {
        RegisterActivity.Start(HelloActivity.this)
      }
    })
    otherWay = findViewById(R.id.login_other_container)
    setTitle("登录")
    otherWay setOnClickListener { v: View =>
      showText("暂时不支持其它登录方式!")
    }
    submitTextView setOnClickListener { v: View =>
      showLoading()
      val account = accountEditText.getText.toString
      val pwd = pwdEditText.getText.toString
      NetWorkService.login(account, pwd, x => {
        //登陆成功
        hideLoading()
        showText("LOGIN SUCCESS!" + account + pwd)
        HomePageActivity.start(this, x.user)
      }, x => {
        //登陆失败
        hideLoading()
        showConfirmCancelDialog(Seq(x.reason, "Retry", "cancel"), connect(), {})
        //showText(x.reason)
      })
    }

  }

  //判断能否连接至服务器
  def connect(): Unit = {
    showLoading()
    val future = NetWorkService.testConnection({
      hideLoading()
      showText("连接至服务器成功!")
    }, {
      hideLoading()
      showConfirmCancelDialog(Seq("连接服务器失败", "retry", "cancel"), connect(), {})
    })
  }

}

object MetaData {
  var ip: String = "10.209.8.196"
  var port: String = "2552"
  var systemName: String = "nice"
}