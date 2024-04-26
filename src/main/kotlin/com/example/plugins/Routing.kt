package com.example.plugins

import com.example.models.GameLogic
import com.example.models.GameState
import com.example.socket
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(gameLogic: GameLogic) {


  routing {

    socket(game = gameLogic)
    get("/") {
      call.respondText("Hello World!")
    }
    get("/lol") {
      call.respondText("Fuck Life!")
    }
  }
}
