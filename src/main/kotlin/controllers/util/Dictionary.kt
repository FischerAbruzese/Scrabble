package controllers.util

import java.util.Scanner

class Dictionary(filePath: String) {
    private val words: HashSet<String> = HashSet<String>()

    init{
        val dictionary = Scanner(java.io.File(filePath))
        while (dictionary.hasNextLine()) {
            val word = dictionary.nextLine()
            words.add(word)
        }
    }

    fun contains(word: String): Boolean = words.contains(word)
}