package sse.xs.msg

/**
  * Created by xusong on 2018/3/17.
  * Email:xusong@bupt.edu.cn
  * Email:xusongnice@gmail.com
  */
sealed trait CommonMsg

case class CommonFailure(reason: String) extends CommonMsg

