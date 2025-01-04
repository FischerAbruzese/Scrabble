package views


import controllers.players.PlayerController
import exceptions.BoardPieceNotUsedException
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

class TextIn : PlayerController {

    companion object {
        const val WHITE_BOLD = "\u001B[1;37m"
        const val RED = "\u001B[31m"
        const val GREEN = "\u001B[32m"
        const val YELLOW = "\u001B[33m"
        const val BLUE = "\u001B[34m"
        const val PURPLE = "\u001B[35m"
        const val CYAN = "\u001B[36m"

        const val RESET = "\u001B[0m"
    }

    override fun getTurn(gameState: GameState, player: Player): Turn {
        return when (queryMoveType(player.name)) {
            "M" -> queryMove(gameState, player)
            "E" -> queryExchange(gameState, player)
            "P" -> return Pass()
            "Q" -> exitProcess(0)
            else -> return Pass()
        }
    }

    private fun queryMoveType(playerName: String): String {
        while (true) {
            ask("${playerName}, what would you like to do? (M)ove, (E)xchange, (P)ass, (Q)uit?")
            var input = readLine()
            if(input == null || input.isEmpty()) continue
            input = input.uppercase().first().toString()
            if (input == "M" || input == "E" || input == "P" || input == "Q") return input
            else printError("Invalid input: $input")
        }
    }

    private fun queryExchange(gameState: GameState, player: Player): Turn {
        while (true) {
            ask("Which pieces would you like to exchange? (format ex: AA_B)")
            var input = readLine()
            input = input?.trim()?.uppercase()
            if(input == null){
                printError("Invalid input: $input")
                continue
            }
            if(input.length > gameState.bag.size()){
                printError("Not enough pieces left in the bag")
                continue
            }
            val piecesToRemove = player.hand.containsPieces(input.toCharArray().toList())
            if(piecesToRemove == null)
                printError("You do not have the required pieces to exchange.")
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
                ask("Which pieces would you like to place(skip pieces already on the board but keep the same order as the word)? (format ex: SCRBLE if forming the word SCRABBLE and AB is already on the board)")
                var input = readLine()
                input = input?.trim()?.uppercase()
                if (input == null) {
                    printError("Invalid input: $input")
                    continue
                }
                val piecesToRemove = player.hand.containsPieces(input.toCharArray().toList())
                if (piecesToRemove == null) {
                    printError("You do not have the required pieces to play that.")
                    continue
                } else tiles = piecesToRemove
            }

            //Blank pieces
            var blankPieces = tiles.filter{it.letter == '_'}
            while(blankPieces.isNotEmpty()){
                ask("You have ${blankPieces.size} blank pieces. What letters do you want to fill them in with? Order matters. (format ex for 2 blank pieces: AB")
                var input = readLine()
                input = input?.trim()?.uppercase()

                //Input error check
                if (input == null) {
                    printError("Invalid input: $input")
                    continue
                }

                //Length error check
                if (input.length != blankPieces.size){
                    printError("You entered ${input.length} letters but have ${blankPieces.size} blank pieces to fill.")
                    continue
                }

                //Valid letter check
                if(input.any{it !in 'A'..'Z'}){
                    printError("All letters must be alphabetical.")
                    continue
                }

                for((index, tile) in blankPieces.withIndex()){
                    tile.letter = input[index]
                }
                blankPieces = listOf()
            }

            //Coordinates
            var coord: Coord? = null
            while (coord == null) {
                ask("Where would you like to place those pieces? (format: row,col   ex: 0,0)")
                var input = readLine()
                input = input?.filter { it.isDigit() || it == ',' }
                if (input == null) {
                    printError("Invalid input: $input")
                    continue
                }
                val coords: List<Int>
                try {
                    coords = input.split(",").map { it.toInt() }
                } catch (e: NumberFormatException) {
                    printError("Invalid input: $input")
                    continue
                }
                if (coords.size != 2) {
                    printError("Invalid input: $input")
                    continue
                }
                coord = Coord(x = coords[1], y= coords[0])
            }

            //Direction
            var direction: Direction? = null
            if(tiles.size == 1) direction = Direction.NONE
            while (direction == null){
                ask("What direction would you like to place the pieces? (D)own, (A)cross")
                var input = readLine()
                input = input?.uppercase()?.first().toString()
                if(input == "D") direction = Direction.DOWN
                else if(input == "A") direction = Direction.ACROSS
                else printError("Invalid input: $input")
            }

            try {
                val move = Move(coord, direction, tiles)
                val findMove = gameState.board.findMove(move) //check if move is valid
                val center = (gameState.board.size())/2
                if(gameState.turnNum == 0 && !findMove.first.contains(Coord(center, center))) //Check if the first move contains the center square
                    throw IllegalMoveException("First move must contain the center square $center,$center")

                return Move(coord, direction, tiles)
            } catch (e: BoardPieceNotUsedException){
                if(gameState.turnNum == 0){
                    val center = (gameState.board.size())/2
                    printError("First move must contain the center square $center,$center")
                } else
                    printError(e.message)
            } catch (e: Exception){
                printError(e.message)
            }
        }

    }

    private fun printError(message: String?) {
        message?.let{println(RED + message + RESET)} ?: println(RED + "Unknown error" + RESET)
    }

    private fun ask(message: String) {
        println(YELLOW + message + RESET)
    }

    override fun pushMessage(message: String){ //TODO: Move to personalized output
        println(CYAN + message + RESET)
    }
}