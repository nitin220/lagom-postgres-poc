package com.example.hello.impl.repositories

import akka.Done
import com.example.hello.api.Hello
import com.lightbend.lagom.scaladsl.api.transport.{ExceptionMessage, NotAcceptable, TransportErrorCode}
import com.lightbend.lagom.scaladsl.persistence.jdbc.JdbcSession

import scala.concurrent.Future


/**
  * Created by knoldus on 31/8/17.
  */
class HelloRepo(session: JdbcSession) {

  import JdbcSession.tryWith

  def addNewMessage(hello: Hello): Future[Done] = {
    val sql =
      """
        |INSERT INTO hello (id, msg)
        |VALUES
        |(?, ?)
        |ON DUPLICATE KEY UPDATE id=?;
      """.stripMargin

    session.withConnection(con => {
      tryWith(con.prepareStatement(sql)) { statement => {
        statement.setLong(1, hello.id)
        statement.setString(2, hello.message)
        if (!statement.execute()) Done
        else throw
          new NotAcceptable(TransportErrorCode.UnsupportedData, new ExceptionMessage("Invalid Data", "User properties invalid"))
      }}
    })
  }

  def getMessage(id: Long): Future[Hello] = {
    val sql =
      s"""
        |SELECT * FROM hello where id = $id;
      """.stripMargin

    session.withConnection(con => {
      tryWith(con.prepareStatement(sql)) { statement => {
        val res = statement.executeQuery()
        Hello(res.getLong(0),res.getString(1))
      }}
    })
  }

}
