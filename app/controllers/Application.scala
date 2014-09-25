package controllers

import play.api._
import play.api.mvc._


object Application extends Controller with Authentication {

  def index = AuthenticateMe {
		(auth,request) => Ok(views.html.index("Free file hosting.",auth))
	}
}