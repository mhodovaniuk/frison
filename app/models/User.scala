package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._


case class User(id: Long, email: String, password: String,activationToken:String ,active: Boolean) {
  lazy val files = UserFile.find(this)
}

object User {
  def apply(id: Long, email: String, password: String,active: Boolean):User = {
    User(id,email,password,null,active)
  }

  val activateSQL:SqlQuery = SQL("""
      UPDATE "Users"
      SET active=true
      WHERE activation_token={activationToken}
    """)

  val findByEmailSQL: SqlQuery = SQL("""
			Select * from "Users" where email={email}
		""")

  val findByIdSQL: SqlQuery = SQL("""
      Select * from "Users" where id={id}
    """)

  val findByEmailAndPasswordSQL: SqlQuery = SQL("""
      Select * from "Users" where email={email} and password={password}
    """)

  val saveSQL: SqlQuery = SQL("""
			INSERT INTO "Users"(email,password,activation_token) values ({email},{password},{activationToken})
		""")

  def find(id: Long): User = {
    DB.withConnection {
      implicit connection =>
        findByIdSQL.on("id" -> id).apply.headOption
    } match {
      case Some(row) => {
          User(row[Long]("id"), row[String]("email"), row[String]("password"),
                row[String]("activation_token"),row[Boolean]("active"))
        }
    }
  }

  def find(email: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        findByEmailSQL.on("email" -> email).apply.headOption
    } match {
      case Some(row) => {
          Some(User(row[Long]("id"), row[String]("email"), row[String]("password"),
                row[String]("activation_token"),row[Boolean]("active")))
        }
      case None => None
    }
  }

  def find(email:String,password:String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        findByEmailAndPasswordSQL.on("email" ->email,"password"->password).apply.headOption
    } match {
      case Some(row) => {
        Some(User(row[Long]("id"), row[String]("email"), row[String]("password"),
                    row[String]("activation_token"),row[Boolean]("active")))
      }
      case None => None
    }
  }

  def save(user: User): Boolean = {
    DB.withConnection {
      implicit connection =>
        saveSQL.on("email" -> user.email,
         "password" -> user.password,
         "activationToken" -> user.activationToken).executeInsert()
    } match {
      case None => false
      case _ => true
    }
  }

  def activate(activationToken:String):Boolean = {
    
    DB.withConnection {
      implicit connection => 
        activateSQL.on("activationToken"->activationToken).executeUpdate()==1
    }
    
  }  

  def isActive(email:String):Boolean = {
    find(email) match {
      case None => false
      case Some(user) => user.active
    }    
  }
}