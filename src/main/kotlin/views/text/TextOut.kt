package views.text

import models.GameState
import util.appendLn
import views.ViewOutput

class TextOut : ViewOutput {
    override fun push(game: GameState) {
        println("\n".repeat(2))
        println(generateGameInfo(game))
        println(generateBoardFrame(game))
        generatePlayerInfo(game).forEach { print(it) }
    }

    private fun generateGameInfo(gameState: GameState): String {
        val gameInfo = StringBuilder()
        gameInfo.appendLn("Turn: ${gameState.turnNum} | Pass Streak: ${gameState.passStreak}")
        gameInfo.appendLn("Bag(${gameState.bag.size()}): ${gameState.bag.pieces.joinToString(separator = "") { it.letter.toString() }}")
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
        screen.appendLn(centerString("$columnNumbers", screenLength))

        // Top board
        screen.appendLn(centerString("*" + "---".repeat(gameState.board.size()) + "*", screenLength))

        //Board
        for ((index, row) in gameState.board.board.withIndex()) {
            screen.appendLn(
                centerString(
                    "" +
                            index + " |" +
                            row.map { " " + (it.piece?.letter ?: "·") + " " }.joinToString("") +
                            "| " + index,
                    screenLength
                )
            )
        }

        //Bottom board
        screen.appendLn(centerString("*" + "---".repeat(gameState.board.size()) + "*", screenLength))

        //Bottom numbers
        screen.appendLn(centerString("$columnNumbers", screenLength))

        //Bottom border
        screen.appendLn("-".repeat(screenLength))

        return screen.toString()
    }

    private fun generatePlayerInfo(gameState: GameState): List<String> {
        val players = ArrayList<String>()
        for (player in gameState.players) {
            val playerString = StringBuilder()
            playerString.appendLn("Player: ${player.name} | Score: ${player.score} | Hand: ${player.hand.pieces.joinToString { it.letter.toString() }}")
            players.add(playerString.toString())
        }
        return players
    }

    private fun centerString(text: String, length: Int, character: Char = ' '): String {
        if (length < text.length) throw IllegalArgumentException("Length of $length is less than length of \"$text\"")
        val padding = (length - text.length) / 2
        return character.toString().repeat(padding) + text + character.toString().repeat(length - text.length - padding)
    }
}