package com.example.models

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class GameLogic {

  private val state = MutableStateFlow(GameState())
  private val playerSockets = ConcurrentHashMap<Char, WebSocketSession>()
  private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private var delayGameJob: Job? = null

  init {
    // That mean on each emission of state do the broadcast function
    state.onEach(::broadcastState).launchIn(gameScope)

  }

  fun connectPlayer(playerSession: WebSocketSession): Char? {
    val isPlayerX = state.value.connectedPlayers.any { it == 'X' }
    val player = if(isPlayerX) 'O' else 'X'

    state.update {
      if(state.value.connectedPlayers.contains(player)) {
        return null
      }
      if(!playerSockets.containsKey(player)) {
        playerSockets[player] = playerSession
      }

      it.copy(
        connectedPlayers = it.connectedPlayers + player
      )
    }
    return player
  }

  fun disconnectPlayer(player: Char) {
    playerSockets.remove(player)
    state.update {
      it.copy(
        connectedPlayers = it.connectedPlayers.minus(player)
      )
    }
  }


  private suspend fun broadcastState(state: GameState) {
    playerSockets.values.forEach { socket ->
      socket.send(
        Json.encodeToString(state)
      )
    }
  }

  fun finishTurn(player: Char, x: Int, y: Int) {
    // We check if the clicked space is not empty
    if (state.value.field[y][x] != null || state.value.winningPlayer != null) {
      return
    }
    // We check if the player in turn will do the move
    if (state.value.playerAtTurn != player) {
      return
    }

    state.update {
      val newField = it.field.also { field ->
        field[y][x] = player
      }
      val isBoardFull = newField.all { it.all { it != null } }

      if (isBoardFull) {
        startNewGame()
      }
      it.copy(
        playerAtTurn = if (player == 'X') 'O' else 'X', field = newField,
        isBoardFull = isBoardFull, winningPlayer = getWinningPlayer()?.also {
          startNewGame()
        }
      )


    }

  }

  private fun startNewGame() {
    delayGameJob?.cancel()
    delayGameJob = gameScope.launch {
      delay(5000L)
      state.update {
        it.copy(
          playerAtTurn = 'X',
          field = GameState.getEmptyField(),
          winningPlayer = null,
          isBoardFull = false,
        )
      }
    }
  }

  private fun getWinningPlayer(): Char? {
    val field = state.value.field
    return if (field[0][0] != null && field[0][0] == field[0][1] && field[0][1] == field[0][2]) {
      field[0][0]
    } else if (field[1][0] != null && field[1][0] == field[1][1] && field[1][1] == field[1][2]) {
      field[1][0]
    } else if (field[2][0] != null && field[2][0] == field[2][1] && field[2][1] == field[2][2]) {
      field[2][0]
    } else if (field[0][0] != null && field[0][0] == field[1][0] && field[1][0] == field[2][0]) {
      field[0][0]
    } else if (field[0][1] != null && field[0][1] == field[1][1] && field[1][1] == field[2][1]) {
      field[0][1]
    } else if (field[0][2] != null && field[0][2] == field[1][2] && field[1][2] == field[2][2]) {
      field[0][2]
    } else if (field[0][0] != null && field[0][0] == field[1][1] && field[1][1] == field[2][2]) {
      field[0][0]
    } else if (field[0][2] != null && field[0][2] == field[1][1] && field[1][1] == field[2][0]) {
      field[0][2]
    } else null
  }

}
