package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.event.LoggingReceive
import play.libs.Akka

/**
  * Created by ycyang on 2016/7/11.
  */
class GameBoard extends Actor with ActorLogging {
    var users = Set[ActorRef]()
    var answer = genMagicNumber

    def receive = LoggingReceive {
        case Join(uid) => {
            users += sender
            context watch sender
        }
        case GuessMsg(uid, g) => {
            var hint = ""
            makeGuess(g) match {
                case Some(guess) => hint = getHint(guess)
                case None => hint = "invalid (it's 4 unique digits)"
            }
            users map {_ ! HintMsg(uid, g, hint)}
            hint match {
                case "4 A 4 B" => {
                    answer = genMagicNumber
                    users map {_ ! HintMsg(s"@$uid congrats ! you got it !", "magic number changed ...", "keep guessing !")}
                }
                case _ => ;
            }
        }
        case Terminated(user) => users -= user
    }

    def genMagicNumber: List[Int] = {
        val x = scala.util.Random.shuffle((0 to 9).toList).take(4)
        log.info("magic number is " + x.mkString(""))
        x
    }

    def getHint(guess: List[Int]):String = {
        val A = (for (x <- answer.zip(guess) if x._1 == x._2) yield 1).length
        val B = 4 - (answer diff guess).length
        s"$A A $B B"
    }

    def makeGuess(s:String): Option[List[Int]] = {
        if (s.length != 4) None
        else {
            try {
                val guess = (s.toCharArray map {_.toString.toInt}).toList
                if (guess.toSet.size == 4) Some(guess) else None
            } catch {
                case e:Exception => None
            }
        }
    }
}

object GameBoard {
    lazy val board = Akka.system().actorOf(Props[GameBoard])
    def apply() = board
}

case class GuessMsg(uid: String, guess: String)
case class HintMsg(uid: String, guess: String, hint: String)
case class Join(uid: String)