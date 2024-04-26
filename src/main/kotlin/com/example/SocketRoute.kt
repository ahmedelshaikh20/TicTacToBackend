package com.example

import com.example.models.GameLogic
import com.example.models.MakeTurn
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

fun Route.socket(game: GameLogic) {
  route("/play") {
    webSocket {
      val player = game.connectPlayer(this)
      if (player == null) {
        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Already 2 Players Registered"))
        return@webSocket
      }

      // Do Reload Button
      try {
          incoming.consumeEach { frame ->
            println("lol$frame")
            if (frame is Frame.Text) {
              val action = extractActionFromMessage(frame.readText())
              game.finishTurn(player, action.x, action.y)
            }

        }
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        game.disconnectPlayer(player)

      }

    }
  }
  route("/lool") {

  }


}


fun extractActionFromMessage(message: String): MakeTurn {
  val type = message.substringBefore('#')
  val body = message.substringAfter('#')
  return if (type == "make_turn") {
    Json.decodeFromString(body)
  } else MakeTurn(-1, -1)

}
