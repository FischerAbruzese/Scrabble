package util

import java.util.*

class Dictionary() {
    companion object {
        private const val FILEPATH = "src/main/kotlin/resources/dictionary.csv"
        val words: HashSet<String> = HashSet<String>()

        init {
            val dictionary = Scanner(java.io.File(FILEPATH))
            while (dictionary.hasNextLine()) {
                val word = dictionary.nextLine()
                words.add(word.lowercase())
            }
        }

        fun contains(word: String): Boolean = words.contains(word.lowercase())
    }
}