package com.bupt.sworld.activity
import android.app.Dialog
import com.bupt.sworld.R
import android.os.{Bundle, Handler, PersistableBundle}
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.{TextView, Toast}
import com.wang.avi.AVLoadingIndicatorView
import sse.xs.msg.user.User

/**
  * Created by xusong on 2018/3/14.
  * About:
  */
class BaseActivity extends AppCompatActivity {

  var handler:Handler = _
  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    handler = new Handler()
  }

  def setTitle(title: String): Unit = {
    val textView = findViewById(R.id.header_title).asInstanceOf[TextView]
    textView.setVisibility(View.VISIBLE)
    if (title != null) textView.setText(title)
    else textView.setText("")
  }

  def post(f: =>Unit){
    val r = new Runnable {
      override def run(): Unit = f
    }
    handler.post(r)
  }

  def showLoading(): Unit = {
    val v:View = findViewById(R.id.loadingview)
    v.setVisibility(View.VISIBLE)
    val loadingIndicatorView = v.findViewById(R.id.loading_loader).asInstanceOf[AVLoadingIndicatorView]
    loadingIndicatorView.setIndicator("PacmanIndicator")
    loadingIndicatorView.show()
  }
  def showLoading(name:String): Unit = {
    val v:View = findViewById(R.id.loadingview)
    val loadingIndicatorView = v.findViewById(R.id.loading_loader).asInstanceOf[AVLoadingIndicatorView]
    loadingIndicatorView.setIndicator(name)
    loadingIndicatorView.show()
    v.setVisibility(View.VISIBLE)

  }

  def hideLoading(): Unit = {
    val v:View = findViewById(R.id.loadingview)
    v.setVisibility(View.INVISIBLE)
    val loadingIndicatorView = v.findViewById(R.id.loading_loader).asInstanceOf[AVLoadingIndicatorView]
    loadingIndicatorView.hide()
  }

  def showText(text:String): Unit ={
    Toast.makeText(this,text,Toast.LENGTH_SHORT).show()
  }

  def showConfirmCancelDialog(tips:Seq[String], onConfirm: =>Unit, onCancel: =>Unit): Unit ={
    val dialog = new Dialog(this, R.style.dialog)
    val layout = getLayoutInflater.inflate(R.layout.dialog_confirm_cancel,null)
    // set the dialog title
    val titleView = layout.findViewById(R.id.dialog_confirmcancel_title).asInstanceOf[TextView]
    titleView.setText("Tips")
    val confirmView = layout.findViewById(R.id.dialog_confirmcancel_confirm).asInstanceOf[TextView]
    confirmView.setText(tips(2))
    val cancel = layout.findViewById(R.id.dialog_confirmcancel_cancel).asInstanceOf[TextView]
    cancel.setText(tips(1))
    val textView = layout.findViewById(R.id.dialog_confirmcancel_detail).asInstanceOf[TextView]
    textView.setVisibility(View.VISIBLE)
    textView.setText(tips.head)

    confirmView.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View): Unit = {
        dialog.dismiss()
        onCancel
      }
    })

    cancel.setOnClickListener(new View.OnClickListener() {
      override def onClick(v: View): Unit = {
        dialog.dismiss()
        onCancel
      }
    })
    dialog.setCancelable(false)
    dialog.setContentView(layout)
    dialog.show()
  }

}
