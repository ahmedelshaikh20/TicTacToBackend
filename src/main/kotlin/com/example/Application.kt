package com.example

import com.example.models.GameLogic
import com.example.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
  val game = GameLogic()
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureRouting(game)
}
