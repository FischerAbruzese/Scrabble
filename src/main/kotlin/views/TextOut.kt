package views

import models.GameState

class TextOut : ViewOutput {
    fun push(gameState: GameState) {

    }

    fun generateFrame(gameState: GameState) {
        val screen: StringBuilder = StringBuilder()
        val screenLength = 100

        screen.append("\n".repeat(20))
        screen.append("-".repeat(screenLength))
        screen.append(centerString("SCRABBLE", screenLength))
    }

    fun centerString(text: String, length: Int, character: Char = ' '): String {
        if (length < text.length) throw IllegalArgumentException()
        val padding = (length - text.length) / 2
        return character.toString().repeat(padding) + text + character.toString().repeat(length - text.length - padding)
    }
}