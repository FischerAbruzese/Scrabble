package controllers

import models.GameState
import models.board.Coord
import models.tiles.Bag
import models.turn.Exchange
import models.turn.Move
import models.turn.Pass
import models.turn.Turn
import util.parsePieceFile
import views.BoardOutput
import views.web.WebOut
import java.io.File
import kotlin.system.exitProcess

//import views.WebOut

class GameController {
    private lateinit var game: GameState
    private var out: BoardOutput = WebOut()

    constructor(gameState: GameState) {
        this.game = gameState
    }

    //this is where you change the default game type
    constructor() {
    }


    fun startGame() {
        println("Waiting for players...")
        out.waitForPlayers(1)

        val bag : Bag
        try {
            bag = Bag(parsePieceFile("/kotlin/resources/characters.csv"))
        } catch (e: Exception) {
            println("PATH: " + java.nio.file.Paths.get(".").toAbsolutePath().toString())
            println("LS: " + File(".").listFiles())

            val currentDirectory = File(".")
            val files = currentDirectory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile) {
                        println("File name: " + file.name)
                    }
                }
            }
            println()
            e.printStackTrace()//TODO: Remove
            exitProcess(2)
        }
        val players = out.getPlayers()
        for (player in players) {
            player.hand.pieces.addAll(bag.draw(7))
        }
        game = GameState(players, bag)
        println("Game starting!")

        while (!game.gameOver()) {
            out.push(game)
            nextMove()
        }

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
                    playerController.pushMessage("Used ${turn.pieces.map { it.letter }} pieces, pulled ${pulled.map { it.letter }}")
                }
            }

            is Exchange -> {
                game.passStreak = 0
                game.currentPlayer().run {
                    val pulled = hand.exchangePieces(game.bag, turn.exchangePieces)
                    playerController.pushMessage("Exchanged ${turn.exchangePieces.map { it.letter }} pieces for ${pulled.map { it.letter }}")
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

    private var previousMove: List<Coord>? = null

    private fun undoMove() {
        for (coord in previousMove ?: throw IllegalArgumentException("No move to undo")) {
            game.board.removePieceAt(coord)
        }
    }
}