package controllers

import controllers.players.Human
import controllers.util.isValidScrabbleWord
import controllers.util.perpendicular
import exceptions.IllegalMoveException
import exceptions.InvalidWordException
import models.GameState
import models.Player
import models.board.Coord
import models.board.Multiplier
import models.tiles.Bag
import models.tiles.Hand
import models.tiles.Piece
import models.turn.*
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

    /**
     * Checks if the game has ended and performs post-game actions
     */
    private fun checkGameOver(): Boolean {
        if (game.passStreak < game.players.size) return false
        game.players.forEach { it.gameEnd() }
        return true
    }

    private var previousMove: List<Coord>? = null

    fun undoMove() {
        for (coord in previousMove ?: throw IllegalArgumentException("No move to undo")) {
            game.board.removePieceAt(coord)
        }
    }

    private fun findMove(move: Move): Pair<List<Coord>, Int> {
        val placedSquares = LinkedList<Coord>()

        var currentLocation = move.start

        if (game.board[currentLocation].hasPiece())
            throw IllegalMoveException("Can not start move on a square with a piece")

        var placedWordScore = 0
        var placedWordMultiplier = 1

        //place all the tiles
        for (piece in move.pieces) {
            try {
                while (game.board.isValidCoordinate(currentLocation) && game.board[currentLocation].hasPiece()) {
                    val sq = game.board[currentLocation]
                    placedWordScore += when (sq.multiplier) {
                        Multiplier.NONE -> sq.piece!!.value
                        Multiplier.DOUBLE_LETTER -> sq.piece!!.value * 2
                        Multiplier.TRIPLE_LETTER -> sq.piece!!.value * 3
                        Multiplier.DOUBLE_WORD -> {
                            placedWordMultiplier *= 2
                            sq.piece!!.value
                        }

                        Multiplier.TRIPLE_WORD -> {
                            placedWordMultiplier *= 3
                            sq.piece!!.value
                        }
                    }
                    currentLocation = when (move.direction) {
                        Direction.ACROSS -> Coord(currentLocation.x + 1, currentLocation.y)
                        Direction.DOWN -> Coord(currentLocation.x, currentLocation.y + 1)
                        Direction.NONE -> throw IllegalStateException("Something has gone terribly wrong")
                    }
                }
                if (!game.board.isValidCoordinate(currentLocation)) {
                    throw IllegalMoveException("Move is out of bounds")
                }
            } catch (ex: Exception) {
                previousMove = placedSquares
                undoMove()
                throw ex
            }
            game.placePiece(currentLocation, piece)
            placedSquares.add(currentLocation)
        }

        previousMove = placedSquares

        var totalScore = 0

        //validate move and score
        val placedWord = game.board.findWordAt(move.start, move.direction)

        if (!placedWord.joinToString { it.letter.toString() }.isValidScrabbleWord()) {
            undoMove()
            throw IllegalMoveException("Invalid word: $placedWord")
        }

        //validate perpendicular moves
        for (coord in placedSquares) {
            val word =
                game.board.findWordAt(coord, move.direction.perpendicular())

            //score word
            var wordScore = 0
            var wordMultiplier = 1

            wordScore += word.sumOf { it.value }
            when (game.board[coord].multiplier) {
                Multiplier.DOUBLE_LETTER -> game.board[coord].piece!!.value
                Multiplier.TRIPLE_LETTER -> game.board[coord].piece!!.value * 2
                Multiplier.DOUBLE_WORD -> wordMultiplier *= 2
                Multiplier.TRIPLE_WORD -> wordMultiplier *= 3
                Multiplier.NONE -> {}
            }
            totalScore += (wordScore * wordMultiplier)

            if (!word.joinToString { it.letter.toString() }.isValidScrabbleWord()) {
                undoMove()
                throw IllegalMoveException("Invalid word: $word")
            }
        }

        undoMove()
        return placedSquares to totalScore
    }

    fun playMove(move: Move): Int {
        val (placedSquares, totalScore) = findMove(move)
        placedSquares.zip(move.pieces).forEach { game.placePiece(it.first, it.second) }
        return totalScore
    }
}