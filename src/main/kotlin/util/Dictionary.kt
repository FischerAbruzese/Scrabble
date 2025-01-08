package util

import java.io.FileInputStream
import java.io.InputStream
import java.util.*
import kotlin.util.Trie

class Dictionary {
    companion object {
        private const val FILENAME = "dictionary.csv"
        val words: HashSet<String> = HashSet<String>()
        val wordsTrie: Trie = Trie()


        init {
            val inputStream: InputStream = object {}.javaClass.getResourceAsStream(FILENAME)
                ?: FileInputStream("src/main/resources/dictionary.csv")

            val dictionary = Scanner(inputStream)
            while (dictionary.hasNextLine()) {
                val word = dictionary.nextLine()
                words.add(word.lowercase())
                wordsTrie.add(word.lowercase())
            }
        }

        fun contains(word: String): Boolean = words.contains(word.lowercase())
    }
}