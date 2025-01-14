package controllers

import kotlinx.coroutines.runBlocking
import models.GameState
import models.tiles.Bag
import models.turn.Exchange
import models.turn.Move
import models.turn.Pass
import models.turn.Turn
import util.parsePieceFile
import views.BoardController
import java.io.FileInputStream
import kotlin.random.Random

//import views.WebOut

class GameController(private val random: Random = Random) {
    private lateinit var game: GameState

    constructor(gameState: GameState): this() {
        this.game = gameState
    }

    fun startGame(boardController: BoardController) {

        val bag = Bag(parsePieceFile(
            this::class.java.classLoader.getResourceAsStream("characters.csv")
                ?: FileInputStream("src/main/resources/characters.csv")
        ), random)

        val players = boardController.getPlayers()
        for (player in players) {
            player.hand.pieces.addAll(bag.draw(7))
        }
        game = GameState(players, bag)
        println("Game starting!")

        while (!game.gameOver()) {
            boardController.push(game)
            nextMove()
        }
        boardController.push(game)

        runBlocking { boardController.closeAllConnections() }
    }

    fun nextMove() {
        val currentPlayer = game.currentPlayer()
        val turn = currentPlayer.playerController.getTurn(game, currentPlayer)
        makeTurn(turn)
    }

    /**
     * Executes a turn if valid
     */
    private fun makeTurn(turn: Turn) {
        when (turn) {
            is Move -> {
                game.passStreak = 0
                game.currentPlayer().run {
                    score += playMove(turn)
                    val pulled = hand.usePieces(game.bag, turn.pieces)
                    playerController.pushMessage(
                        "Used ${turn.pieces.map { it.letter }} pieces, pulled ${pulled.map { it.letter }}",
                        game.currentPlayer().name
                    )
                }
            }

            is Exchange -> {
                game.passStreak = 0
                game.currentPlayer().run {
                    val pulled = hand.exchangePieces(game.bag, turn.exchangePieces)
                    playerController.pushMessage(
                        "Exchanged ${turn.exchangePieces.map { it.letter }} pieces for ${pulled.map { it.letter }}",
                        game.currentPlayer().name
                    )
                }
            }

            is Pass -> {
                game.passStreak++
            }

            else -> throw IllegalArgumentException("Unimplemented turn type")
        }
        game.turnNum++
    }

    fun playMove(move: Move): Int {
        val (placedSquares, totalScore) = game.board.findMove(move)
        placedSquares.zip(move.pieces).forEach { game.placePiece(it.first, it.second) }
        return totalScore
    }
}