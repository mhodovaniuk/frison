package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._

case class UserFile(id:Long,user:User,name:String, size:Long,public:Boolean) {
}

object UserFile {
	def apply(id:Long,name:String, size:Long,public:Boolean):UserFile={
		UserFile(id,null,name,size,public)
	}

  val removeFileByUserEmailAndFileNameSQL=SQL("""
    DELETE FROM "Files" WHERE user_id=(SELECT id FROM "Users" WHERE email={email}) and name={file_name}
    """)

  val searchFilesSQL=SQL("""
    SELECT * FROM "Files" where name like {name} and (public=true or user_id={user_id})
    """)

	val saveSQL=SQL("""
		INSERT INTO "Files"(user_id,name,size) values ({user_id},{name},{size})
		""")

	val findFilesByUserIdSQL=SQL("""
		SELECT * FROM "Files" WHERE user_id={user_id}
		""")

	val findFileByUserEmailAndFileNameSQL=SQL("""
		SELECT * FROM "Files" inner join "Users" on user_id="Users".id WHERE email={email} and name={file_name}
		""")

	val changeStatusSQL=SQL("""
			UPDATE "Files"
      SET public={status}
      WHERE id={id}
		""")

  def remove(email:String, fileName:String):Boolean = {
    DB.withConnection {
      implicit connection =>
        removeFileByUserEmailAndFileNameSQL.on("email" -> email,"file_name"->fileName).executeUpdate()==1
    }
  }

	def save(file:UserFile) = {
		DB.withConnection {
      implicit connection =>
        find(file.user.email,file.name) match {
          case None => saveSQL.on("user_id" -> file.user.id,
                        "name" -> file.name,
                        "size" -> file.size).executeInsert()
          case _ => None
        }
    } match {
      case None => false
      case _ => true
    }
	}

	def find(user:User):List[UserFile] = {
		DB.withConnection {
      implicit connection =>
        findFilesByUserIdSQL.on("user_id" ->user.id).apply.map{ row =>
        	UserFile(row[Long]("id"),user,row[String]("name"),row[Long]("size"),row[Boolean]("public"))	
        }.toList
    } 
	}

	def find(email:String, fileName:String):Option[UserFile] = {
    DB.withConnection {
      implicit connection =>
        findFileByUserEmailAndFileNameSQL.on("email" -> email,"file_name"->fileName).apply.headOption
    } match {
      case Some(row) => {
          Some(UserFile(row[Long]("id"), null, fileName,
                row[Long]("size"),row[Boolean]("public")))
        }
      case None => None
    }
  }

def search(fileNamePattern :String, user:Option[User]):List[UserFile] = {
  val userId=if (user.isEmpty) -1 else user.get.id
  DB.withConnection {
      implicit connection =>
        searchFilesSQL.on("name" ->fileNamePattern,"user_id"->userId).apply.map{ row =>
          UserFile(row[Long]("id"),User.find(row[Long]("user_id")),
            row[String]("name"),row[Long]("size"),row[Boolean]("public")) 
        }.toList
    } 
}

def changeStatus(id:Long,status:Boolean):Boolean = {
    DB.withConnection {
      implicit connection => 
        changeStatusSQL.on("id"->id,"status"->status).executeUpdate()==1
    }
  }	
}