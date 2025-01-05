package util

import java.util.*

class Dictionary() {
    companion object {
        private const val FILENAME = "dictionary.csv"
        val words: HashSet<String> = HashSet<String>()

        init {
            val inputStream = object {}.javaClass.getResourceAsStream(FILENAME)
            val dictionary = Scanner(inputStream)
            while (dictionary.hasNextLine()) {
                val word = dictionary.nextLine()
                words.add(word.lowercase())
            }
        }

        fun contains(word: String): Boolean = words.contains(word.lowercase())
    }
}