package views.text

import views.TextInterpreter

class ConsolePlayerController : TextInterpreter {
    private constructor() : super() {}

    companion object {
        val INSTANCE = ConsolePlayerController()

        private const val WHITE_BOLD = "\u001B[1;37m"
        private const val RED = "\u001B[31m"
        private const val GREEN = "\u001B[32m"
        private const val YELLOW = "\u001B[33m"
        private const val BLUE = "\u001B[34m"
        private const val PURPLE = "\u001B[35m"
        private const val CYAN = "\u001B[36m"

        private const val RESET = "\u001B[0m"
    }

    override fun getNextInput(player: String): String {
        print("${PURPLE}<$player>${RESET} ")
        return readln()
    }

    override fun sendMessage(message: String, player: String) {
        println("$message")
    }

    override fun ask(message: String, player: String) {
        val message = YELLOW + message + RESET
        super.ask(message, player)
    }

    override fun printError(message: String?, player: String) {
        val message = message?.let { (RED + message + RESET) } ?: (RED + "Unknown error" + RESET)
        super.printError(message, player)
    }

    override fun pushMessage(message: String, player: String) {
        val message = CYAN + message + RESET
        super.pushMessage(message, player)
    }
}