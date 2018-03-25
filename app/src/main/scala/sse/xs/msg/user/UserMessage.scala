package sse.xs.msg.user

/**
  * Created by xusong on 2018/3/20.
  * About:
  */
sealed trait UserMessage

@SerialVersionUID(100L)
class User(val name: String, val pwd: String) extends Serializable {
  override def equals(obj: scala.Any) = {
    obj match {
      case o: User =>
        name == o.name
      case _ => false
    }
  }
}

object User {

  def apply(name: String, pwd: String): User = new User(name, pwd)
}

case class LoginRequest(account: String, pwd: String) extends UserMessage

case class LoginSuccess(user: User) extends UserMessage

case class LoginFailure(reason:String ) extends UserMessage

case class RegisterRequest(user:String,pwd:String) extends UserMessage

case class RegisterSuccess(user:User) extends UserMessage

case class RegisterFailure(reason:String) extends UserMessage