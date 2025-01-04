package controllers

import controllers.util.parsePieceFile
import models.GameState
import models.Player
import models.board.Coord
import models.tiles.Bag
import models.tiles.Hand
import models.turn.Exchange
import models.turn.Move
import models.turn.Pass
import models.turn.Turn
import views.ViewOutput
import views.web.WebOut
import views.text.TextIn

//import views.WebOut

class GameController {
    private var game: GameState
    private var out: ViewOutput = WebOut()

    constructor(gameState: GameState) {
        this.game = gameState
    }

    //this is where you change the default game type
    constructor() {
        val bag = Bag(parsePieceFile("src/main/kotlin/resources/characters.csv"))


        val players = listOf(
            Player(
                "Mari",
                TextIn(),
                Hand(bag.draw(7))
            ),
            Player(
                "Sky",
                TextIn(),
                Hand(bag.draw(7))
            )
        )

        this.game = GameState(players, bag)
    }


    fun startGame() {
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