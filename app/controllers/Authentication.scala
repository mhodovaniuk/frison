package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models._

trait Authentication {
 	def AuthenticateMe(f: (Option[User],Request[Any]) => Result) = Action { implicit request =>
		val auth:Option[User] = request.session.get("user.email") match {
			case None => None
			case Some(email:String) => User.find(email)
		}
		f(auth,request)		
	} 	
}

// object Conditions {
// 	type Condition = (User => Either[String, Unit])
// 	def isPremiumUser:Condition = {
// 		user => if(user.isPremium)
// 			Right()
// 		else
// 			Left("User must be premium")
// 	}
// 	def balanceGreaterThan(required:Int):Condition = {
// 		user => if(user.balance > required)
// 			Right()
// 		else
// 			Left(s"User balance must be > $required")	
// 	}
// }
