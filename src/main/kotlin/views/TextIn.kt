package views


import controllers.players.PlayerController
import exceptions.IllegalMoveException
import models.turn.Direction
import models.GameState
import models.Player
import models.board.Coord
import models.tiles.Piece
import models.turn.Exchange
import models.turn.Move
import models.turn.Pass
import models.turn.Turn
import kotlin.system.exitProcess

class TextIn : ViewInput, PlayerController {

    companion object {
        const val WHITE_BOLD = "\u001B[1;37m"
        const val RESET = "\u001B[0m"
    }

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
            println("${playerName}, what would you like to do? (M)ove, (E)xchange, (P)ass, (Q)uit?")
            var input = readLine()
            input = input?.toUpperCase()?.first().toString()
            if (input == "M" || input == "E" || input == "P" || input == "Q") return input
            else println("Invalid input: $input")
        }
    }

    private fun queryExchange(gameState: GameState, player: Player): Turn {
        while (true) {
            println("Which pieces would you like to exchange? (format ex: AA_B)")
            var input = readLine()
            input = input?.trim()?.uppercase()
            if(input == null){
                println("Invalid input: $input")
                continue
            }
            if(input!!.length > gameState.bag.size()){
                println("Not enough pieces left in the bag")
                continue
            }
            val piecesToRemove = player.hand.containsPieces(input!!.toCharArray().toList())
            if(piecesToRemove == null)
                println("You do not have the required pieces to exchange.")
            else {
                return Exchange(piecesToRemove)
            }
        }
    }

    private fun queryMove(gameState: GameState, player: Player): Turn {
        while(true) {
            //Tiles
            var tiles: List<Piece>? = null
            while (tiles == null) {
                println("Which pieces would you like to place(skip pieces already on the board but keep the same order as the word)? (format ex: SCRBLE if forming the word SCRABBLE and AB is already on the board)")
                var input = readLine()
                input = input?.trim()?.uppercase()
                if (input == null) {
                    println("Invalid input: $input")
                    continue
                }
                val piecesToRemove = player.hand.containsPieces(input!!.toCharArray().toList())
                if (piecesToRemove == null) {
                    println("You do not have the required pieces to play that.")
                    continue
                } else tiles = piecesToRemove
            }

            //Coordinates
            var coord: Coord? = null
            while (coord == null) {
                println("Where would you like to place those pieces? (format: row,col   ex: 0,0)")
                var input = readLine()
                input = input?.filter { it.isDigit() || it == ',' }
                if (input == null) {
                    println("Invalid input: $input")
                    continue
                }
                val coords: List<Int>
                try {
                    coords = input.split(",").map { it.toInt() }
                } catch (e: NumberFormatException) {
                    println("Invalid input: $input")
                    continue
                }
                if (coords.size != 2) {
                    println("Invalid input: $input")
                    continue
                }
                coord = Coord(x = coords[1], y= coords[0])
            }

            //Direction
            var direction: Direction? = null
            if(tiles.size == 1) direction = Direction.NONE
            while (direction == null){
                println("What direction would you like to place the pieces? (D)own, (A)cross")
                var input = readLine()
                input = input?.toUpperCase()?.first().toString()
                if(input == "D") direction = Direction.DOWN
                else if(input == "A") direction = Direction.ACROSS
                else println("Invalid input: $input")
            }

            try {
                val move = Move(coord, direction, tiles)
                val findMove = gameState.board.findMove(move) //check if move is valid
                val center = (gameState.board.size())/2
                if(gameState.turnNum == 0 && !findMove.first.contains(Coord(center, center))) //Check if the first move contains the center square
                    throw IllegalMoveException("First move must contain the center square $center,$center")

                return Move(coord, direction, tiles)
            } catch (e: IllegalMoveException){
                println(e.message)
            }
        }

    }
}