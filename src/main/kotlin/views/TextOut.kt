package views

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
        gameInfo.append("Turn: ${gameState.turnNum} Pass Streak: ${gameState.passStreak}")
        return gameInfo.toString()
    }

    private fun generateBoardFrame(gameState: GameState): String {
        val screen: StringBuilder = StringBuilder()
        val screenLength = 100
        screen.append("-".repeat(screenLength))
        screen.append(centerString("SCRABBLE", screenLength))
        screen.append("*" + "-".repeat(gameState.board.size()) + "*")
        for (row in gameState.board.board) {
            print(centerString("|" + row.joinToString("") + "|", screenLength))
        }
        screen.append("*" + "-".repeat(gameState.board.size()) + "*")
        return screen.toString()
    }

    private fun generatePlayerInfo(gameState: GameState): List<String> {
        val players = ArrayList<String>()
        for (player in gameState.players) {
            val playerString = StringBuilder()
            playerString.append("Player: ${player.name} Score: ${player.score} Hand: ${player.hand.pieces.joinToString("")}")
            players.add(playerString.toString())
        }
        return players
    }

    private fun centerString(text: String, length: Int, character: Char = ' '): String {
        if (length < text.length) throw IllegalArgumentException()
        val padding = (length - text.length) / 2
        return character.toString().repeat(padding) + text + character.toString().repeat(length - text.length - padding)
    }
}