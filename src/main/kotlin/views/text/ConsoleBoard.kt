package views.text

import models.GameState
import models.Player
import models.board.Multiplier
import util.appendLn
import views.BoardController

class ConsoleBoard(private val playerList: List<Player>) : BoardController {
    private companion object {
        const val RED = "\u001B[31m"
        const val GREEN = "\u001B[32m"
        const val YELLOW = "\u001B[33m"
        const val BLUE = "\u001B[34m"
        const val PURPLE = "\u001B[35m"
        const val CYAN = "\u001B[36m"

        // Bright text colors
        const val BRIGHT_BLACK: String = "\u001B[30;1m"
        const val BRIGHT_RED: String = "\u001B[31;1m"
        const val BRIGHT_GREEN: String = "\u001B[32;1m"
        const val BRIGHT_YELLOW: String = "\u001B[33;1m"
        const val BRIGHT_BLUE: String = "\u001B[34;1m"
        const val BRIGHT_MAGENTA: String = "\u001B[35;1m"
        const val BRIGHT_CYAN: String = "\u001B[36;1m"
        const val BRIGHT_WHITE: String = "\u001B[37;1m"

        const val RESET = "\u001B[0m"
    }

    override fun getPlayers(): List<Player> {
        return playerList
    }

    override fun push(game: GameState) {
        println("\n".repeat(2))
        println(generateGameInfo(game))
        println(generateBoardFrame(game))
        generatePlayerInfo(game).forEach { print(it) }
    }

    private fun generateGameInfo(gameState: GameState): String {
        val gameInfo = StringBuilder()
        gameInfo.appendLn("Turn: ${gameState.turnNum} | Pass Streak: ${gameState.passStreak}")
        gameInfo.appendLn("Bag: ${gameState.bag}")
        return gameInfo.toString()
    }

    private fun generateBoardFrame(gameState: GameState): String {
        val screen: StringBuilder = StringBuilder()
        val screenLength = 100

        // Top border
        screen.appendLn("-".repeat(screenLength))

        // Title
        screen.appendLn(centerString("SCRABBLE", screenLength))


        // Column Numbers
        val columnWidth = 3 // " - " is 3 characters wide
        val columnNumbers = (0 until gameState.board.size()).joinToString("") {
            it.toString().padEnd((columnWidth + 1) / 2).padStart(columnWidth)
        }
        screen.appendLn(centerString(columnNumbers, screenLength))

        // Top board
        screen.appendLn(centerString("*" + "---".repeat(gameState.board.size()) + "*", screenLength))

        //Board
        for ((index, row) in gameState.board.board.withIndex()) {
            screen.appendLn(
                centerString(
                    "" +
                            index + " |" +
                            row.joinToString("") {
                                " " + (it.piece?.letter ?: ((when (it.multiplier) {
                                    Multiplier.TRIPLE_WORD -> BRIGHT_RED
                                    Multiplier.DOUBLE_WORD -> YELLOW
                                    Multiplier.TRIPLE_LETTER -> BRIGHT_MAGENTA
                                    Multiplier.DOUBLE_LETTER -> BRIGHT_BLUE
                                    Multiplier.NONE -> ""
                                }) + "·" + RESET)) + " "
                            } +
                            "| " + index,
                    screenLength
                )
            )
        }

        //Bottom board
        screen.appendLn(centerString("*" + "---".repeat(gameState.board.size()) + "*", screenLength))

        //Bottom numbers
        screen.appendLn(centerString(columnNumbers, screenLength))

        //Bottom border
        screen.appendLn("-".repeat(screenLength))

        return screen.toString()
    }

    private fun generatePlayerInfo(gameState: GameState): List<String> {
        val players = ArrayList<String>()
        for (player in gameState.players) {
            val playerString = StringBuilder()
            playerString.appendLn("Player: ${player.name} | Score: ${player.score} | Hand: ${player.hand.pieces.joinToString { "${it.letter}(${it.value})" }}")
            players.add(playerString.toString())
        }
        return players
    }

    private fun centerString(text: String, length: Int, character: Char = ' '): String {
        val textLength = getVisibleStringLength(text)
        if (length < textLength) throw IllegalArgumentException("Length of $length is less than length of \"$text\"")
        val padding = (length - textLength) / 2
        return character.toString().repeat(padding) + text + character.toString().repeat(length - textLength - padding)
    }

    private fun getVisibleStringLength(input: String): Int {
        // Regex to match ANSI escape codes
        val ansiEscapeRegex = "\\u001B\\[[;\\d]*m".toRegex()

        // Remove ANSI escape sequences
        val cleanString = input.replace(ansiEscapeRegex, "")

        // Return the visible length of the string
        return cleanString.length
    }
}