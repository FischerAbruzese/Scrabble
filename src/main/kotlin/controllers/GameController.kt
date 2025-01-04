package controllers

import controllers.players.Human
import exceptions.InvalidWordException
import models.GameState
import models.Player
import models.board.Coord
import models.tiles.Bag
import models.tiles.Hand
import models.tiles.Piece
import models.turn.Exchange
import models.turn.Move
import models.turn.Pass
import models.turn.Turn
import java.util.*

class GameController {
    private lateinit var game: GameState

    constructor(gameState: GameState) {
        this.game = gameState
    }

    //this is where you change the default game type
    constructor() {
        val bag = Bag(parsePieceFile("resources/characters.csv"))

        val players = listOf(
            Player(
                "Player 1",
                Human(),
                Hand(bag.draw(7))
            ),
            Player(
                "Player 2",
                Human(),
                Hand(bag.draw(7))
            )
        )

        this.game = GameState(players, bag)
    }

    fun parsePieceFile(path: String): List<Piece> {
        val pieces = mutableListOf<Piece>()
        val pieceFile = Scanner(java.io.File(path))
        while (pieceFile.hasNextLine()) {
            val line = pieceFile.nextLine().split(",")
            val pieceToAdd = Piece(line[0][0], line[2].toInt())
            repeat(line[1].toInt()) { pieces.add(pieceToAdd.copy()) }
        }
        return pieces
    }

    fun startGame() {
        while (!game.gameOver()) {
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
                try {
                    game.currentPlayer().score += playMove(turn)
                } catch (ex: InvalidWordException) {
                    game.currentPlayer()
                }
            }

            is Exchange -> {
                game.passStreak = 0
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