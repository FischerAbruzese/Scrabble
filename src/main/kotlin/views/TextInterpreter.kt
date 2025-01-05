package views


import controllers.players.PlayerController
import exceptions.BoardPieceNotUsedException
import exceptions.IllegalMoveException
import models.GameState
import models.Player
import models.board.Coord
import models.tiles.Piece
import models.turn.*
import kotlin.system.exitProcess

abstract class TextInterpreter : PlayerController {

    abstract fun getNextInput(player: String): String

    abstract fun sendMessage(message: String, player: String)

    override fun getTurn(gameState: GameState, player: Player): Turn {
        return when (queryMoveType(player.name)) {
            "M" -> queryMove(gameState, player)
            "E" -> queryExchange(gameState, player)
            "P" -> return Pass()
            else -> exitProcess(0)
        }
    }

    private fun queryMoveType(playerName: String): String {
        while (true) {
            ask("${playerName}, what would you like to do? (M)ove, (E)xchange, (P)ass, (Q)uit?", playerName)
            var input = getNextInput(playerName)
            if (input == null || input.isEmpty()) continue
            input = input.uppercase().first().toString()
            if (input == "M" || input == "E" || input == "P" || input == "Q") return input
            else printError("Invalid input: $input", playerName)
        }
    }

    private fun queryExchange(gameState: GameState, player: Player): Turn {
        while (true) {
            ask("Which pieces would you like to exchange? (format ex: AA_B)", player.name)
            var input = getNextInput(player.name)
            input = input.trim().uppercase()
            if (input.length > gameState.bag.size()) {
                printError("Not enough pieces left in the bag", player.name)
                continue
            }
            val piecesToRemove = player.hand.containsPieces(input.toCharArray().toList())
            if (piecesToRemove == null)
                printError("You do not have the required pieces to exchange.", player.name)
            else {
                return Exchange(piecesToRemove)
            }
        }
    }

    private fun queryMove(gameState: GameState, player: Player): Turn {
        while (true) {
            //Tiles
            var tiles: List<Piece>? = null
            while (tiles == null) {
                ask(
                    "Which pieces would you like to place(skip pieces already on the board but keep the same order as the word)? (format ex: SCRBLE if forming the word SCRABBLE and AB is already on the board)",
                    player.name
                )
                var input = getNextInput(player.name)
                input = input.trim().uppercase()
                val piecesToRemove = player.hand.containsPieces(input.toCharArray().toList())
                if (piecesToRemove == null) {
                    printError("You do not have the required pieces to play that.", player.name)
                    continue
                } else tiles = piecesToRemove
            }

            //Blank pieces
            var blankPieces = tiles.filter { it.letter == '_' }
            while (blankPieces.isNotEmpty()) {
                ask(
                    "You have ${blankPieces.size} blank pieces. What letters do you want to fill them in with? Order matters. (format ex for 2 blank pieces: AB",
                    player.name
                )
                var input = getNextInput(player.name)
                input = input.trim().uppercase()

                //Length error check
                if (input.length != blankPieces.size) {
                    printError(
                        "You entered ${input.length} letters but have ${blankPieces.size} blank pieces to fill.",
                        player.name
                    )
                    continue
                }

                //Valid letter check
                if (input.any { it !in 'A'..'Z' }) {
                    printError("All letters must be alphabetical.", player.name)
                    continue
                }

                for ((index, tile) in blankPieces.withIndex()) {
                    tile.letter = input[index]
                }
                blankPieces = listOf()
            }

            //Coordinates
            var coord: Coord? = null
            while (coord == null) {
                ask("Where would you like to place those pieces? (format: row,col   ex: 0,0)", player.name)
                var input = getNextInput(player.name)
                input = input.filter { it.isDigit() || it == ',' }
                val coords: List<Int>
                try {
                    coords = input.split(",").map { it.toInt() }
                } catch (e: NumberFormatException) {
                    printError("Invalid input: $input", player.name)
                    continue
                }
                if (coords.size != 2) {
                    printError("Invalid input: $input", player.name)
                    continue
                }
                coord = Coord(x = coords[1], y = coords[0])
            }

            //Direction
            var direction: Direction? = null
            if (tiles.size == 1) direction = Direction.NONE
            while (direction == null) {
                ask("What direction would you like to place the pieces? (D)own, (A)cross", player.name)
                var input = getNextInput(player.name)
                input = input?.uppercase()?.first().toString()
                if (input == "D") direction = Direction.DOWN
                else if (input == "A") direction = Direction.ACROSS
                else printError("Invalid input: $input", player.name)
            }

            try {
                val move = Move(coord, direction, tiles)
                val findMove = gameState.board.findMove(move) //check if move is valid
                val center = (gameState.board.size()) / 2
                if (gameState.turnNum == 0 && !findMove.first.contains(
                        Coord(
                            center,
                            center
                        )
                    )
                ) //Check if the first move contains the center square
                    throw IllegalMoveException("First move must contain the center square $center,$center")

                return Move(coord, direction, tiles)
            } catch (e: BoardPieceNotUsedException) {
                if (gameState.turnNum == 0) {
                    val center = (gameState.board.size()) / 2
                    printError("First move must contain the center square $center,$center", player.name)
                } else
                    printError(e.message, player.name)
            } catch (e: Exception) {
                printError(e.message, player.name)
            }
        }

    }

    open fun printError(message: String?, player: String) {
        message?.let { sendMessage(message, player) } ?: sendMessage("Unknown error", player)
    }

    open fun ask(message: String, player: String) {
        sendMessage(message, player)
    }

    override fun pushMessage(message: String, player: String) {
        sendMessage(message, player)
    }
}