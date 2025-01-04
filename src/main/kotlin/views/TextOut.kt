package views

import controllers.util.appendLn
import models.GameState

class TextOut : ViewOutput {
    fun push(gameState: GameState) {
        println("\n".repeat(20))
        println(generateGameInfo(gameState))
        println(generateBoardFrame(gameState))
        generatePlayerInfo(gameState).forEach { println(it) }
    }

    private fun generateGameInfo(gameState: GameState): String {
        val gameInfo = StringBuilder()
        gameInfo.appendLn("Turn: ${gameState.turnNum} Pass Streak: ${gameState.passStreak}")
        return gameInfo.toString()
    }

    private fun generateBoardFrame(gameState: GameState): String {
        val screen: StringBuilder = StringBuilder()
        val screenLength = 100
        screen.appendLn("-".repeat(screenLength))
        screen.appendLn(centerString("SCRABBLE", screenLength))
        screen.appendLn(centerString("*" + " - ".repeat(gameState.board.size()) + "*", screenLength))
        for (row in gameState.board.board) {
            screen.appendLn(centerString("|" + row.map{" " + (it.piece?.letter?:" ") + " "}.joinToString("") + "|", screenLength))
        }
        screen.appendLn(centerString("*" + " - ".repeat(gameState.board.size()) + "*", screenLength))
        return screen.toString()
    }

    private fun generatePlayerInfo(gameState: GameState): List<String> {
        val players = ArrayList<String>()
        for (player in gameState.players) {
            val playerString = StringBuilder()
            playerString.appendLn("Player: ${player.name} Score: ${player.score} Hand: ${player.hand.pieces.joinToString{it.letter.toString()}}")
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