package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models._
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import play.api.mvc.BodyParsers.parse.Multipart.PartHandler
import play.api.mvc.BodyParsers.parse.Multipart.handleFilePart
import play.api.mvc.BodyParsers.parse.Multipart.FileInfo
import play.api.mvc.BodyParsers.parse.multipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.BodyParser
import play.api.mvc.MultipartFormData



object FileControler extends Controller with Authentication {
	val statusForm = Form(
  tuple(
    "file_id" -> longNumber,
    "status" -> boolean
  )
)

	def search = AuthenticateMe { (auth,request)=>
		val fileName=request.queryString("file_name").head
		Ok(views.html.search(UserFile.search("%"+fileName+"%",auth),auth))
	}

	def files = AuthenticateMe { (auth,request) =>
		auth match {
			case Some(user) => 
				implicit val f=request.flash
				Ok(views.html.files(statusForm,user))			
			case None => Forbidden("Please log in.")
		}					
	} 

	def download(email:String, name:String) = {
		AuthenticateMe { (auth,request) =>
			UserFile.find(email,name) match {
				case Some(file:UserFile) => 
					if (file.public || (auth match {
							case Some(user) => user.email==email
							case None => false
						})
					)
						Ok.sendFile(
							content=new File("private/"+email+"/"+name),
							inline=true
						)
					else
						Ok(views.html.index("Fail to take file",auth))
				case None => BadRequest("Can't find such file.")
			}
		}
	}

	def remove(email:String, name:String) = {
		AuthenticateMe { (auth,request) =>
			if (UserFile.remove(email,name))
				Files.delete(Paths.get("private",email,name))
			Redirect(routes.FileControler.files())
		}
	}

	def upload = Action(parse.multipartFormData) { request =>
		val auth = request.session.get("user.email") match {
			case None => None
			case Some(email:String) => User.find(email)
		}
		request.body.file("file").map { file =>
 			val fileName = file.filename
 			val contentType = file.contentType.get
 			val resFile=new File("private/"+auth.get.email+"/"+fileName)
 			if (UserFile.save(UserFile(0,auth.get,fileName,file.ref.file.length,false))){
 				file.ref.moveTo(resFile)
 				Redirect( routes.FileControler.files()).flashing(
 					"success"->"File successfully added!")
 			} else 
 				Redirect( routes.FileControler.files()).flashing(
 					"error"->"File already exists!"
 					)

 		}.getOrElse {
			Redirect( routes.FileControler.files()).flashing(
 					"error"->"Please, select file!"
 					)
 		} 		
 }
	def status = AuthenticateMe {
    (auth, request) =>
      implicit val r=request
      statusForm.bindFromRequest.fold(
        formWithErrors => { 
        	Ok("ok")
          // BadRequest(views.html.login(formWithErrors)(auth))
        },
        (arg) => {
        	println(arg._1+"  "+arg._2)
          UserFile.changeStatus(arg._1,arg._2)
          Ok("Ok")
        })
  }
   // request.body.file("file").map { file =>
   //    val filename = file.filename 
   //  	val contentType = file.contentType
   //  	file.ref.moveTo(new File("/private/"+auth.get.email+"/"+filename))
   //  	Ok("File uploaded")
   // }.getOrElse {
   //  	Redirect(routes.Application.index).flashing(
   //    	"error" -> "Missing file"
   //  	)
   // }
	
}