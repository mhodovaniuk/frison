package controllers

import play.api.i18n.Messages
import play.api.data.Forms._
import play.api._
import play.api.data._
import play.api.mvc._
import models._
import java.util.UUID

import play.api.Play.current
import com.typesafe.plugin._

object Account extends Controller with Authentication {
  val loginForm: Form[User] = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText)((email: String, password: String) => User(0, email, password, false))((u: User) => Some(u.email, u.password)) 
      verifying(Messages("error.credentials"),user => user match {
      	case user =>  User.find(user.email,user.password) match {
      		case Some(u) =>  u.active
      		case None => false
      	}
      }
      ))

  val registerForm: Form[(String,String,String)] = Form(
    tuple (
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "password2" -> nonEmptyText
      ) verifying("Passwords different!", ur => ur match {
          case (e,p1,p2) => p1==p2 
          case _ => true
        }))

  def registerPage = AuthenticateMe { (auth, request)=>
    Ok(views.html.register(registerForm)(auth))
  }

  def register = AuthenticateMe { (auth,request) =>
    implicit val r=request
    registerForm.bindFromRequest().fold(
      formWithErrors => { 
        BadRequest(views.html.register(formWithErrors)(auth))
      },
      success = { ur =>
        val user=User(0, ur._1, ur._2, UUID.randomUUID().toString(), false)
        val activationLink="http://localhost:9000/activate/"+user.activationToken
        println(user.activationToken)
        val mail = use[MailerPlugin].email
        mail.setSubject("activation")
        mail.setRecipient("mykhailo.hodovaniuk@gmail.com")
        mail.setFrom("antey007@meta.ua")
        mail.send("Follow the activation link activate/"+user.activationToken)
        User.save(user)
        Ok(views.html.index("Activation Link: " + activationLink,auth))
      })
    
  }

  def loginPage = AuthenticateMe { (auth,request) =>
    Ok(views.html.login(loginForm)(auth))
  }

  def logout = AuthenticateMe { (auth,request) =>
      Ok(views.html.index("Loged out.",None)).withNewSession    
  }


  def login = AuthenticateMe {
    (auth, request) =>
      implicit val r=request
      loginForm.bindFromRequest.fold(
        formWithErrors => { 
          BadRequest(views.html.login(formWithErrors)(auth))
        },
        user => {
          Ok(views.html.index("Loged in",Some(user))).withSession("user.email"->user.email)
        })
  }

  def activate(token: String) = AuthenticateMe { (auth, request) =>
    val res = User.activate(token)
    if (res)
      Ok(views.html.index("Successfully activated.",auth))
    else
      Ok(views.html.index("Error during activation.",auth))
  }
}