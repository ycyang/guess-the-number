package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import play.api.libs.json.{JsValue, Json}

import scala.xml.Utility

/**
  * Created by ycyang on 2016/7/11.
  */
class UserActor(uid: String, board: ActorRef, out: ActorRef) extends Actor with ActorLogging {

    override def preStart() = {
        GameBoard() ! Join(uid)
    }

    def receive = LoggingReceive {
        case HintMsg(uid, guess, hint) if sender == board =>
            out ! Json.obj("type"->"message", "uid"->uid, "guess"->guess, "hint"->hint)
        case js: JsValue =>
            (js \ "guess").validate[String] map { Utility.escape(_) }  map { board ! GuessMsg(uid, _ ) }
        case other =>
            log.error("unhandled: " + other)
    }
}

object UserActor {
    def props(uid: String)(out: ActorRef) = Props(new UserActor(uid, GameBoard(), out))
}