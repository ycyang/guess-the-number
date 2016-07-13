package controllers

import actors.UserActor

import scala.concurrent.Future
import play.api.libs.json.JsValue
import play.api.mvc._
import play.api.Play.current


object Application extends Controller {

    def index = Action { implicit request =>
        request.getQueryString("uid") match {
            case None =>
                Ok(views.html.Application.login())
            case Some(uid) =>
                Ok(views.html.Application.board(uid)).withSession {
                request.session + ("uid"->uid)
            }
        }
    }

    def ws = WebSocket.tryAcceptWithActor[JsValue, JsValue] { implicit request =>
        Future.successful(request.session.get("uid") match {
            case None => Left(Forbidden)
            case Some(uid) => Right(UserActor.props(uid))
        })
    }
}
