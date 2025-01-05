package kotlin.util

class Trie {
    /**
     * A node in a Trie
     *
     * Class invariant: if a node has no children, exists is true
     */
    class TrieNode (var exists: Boolean, var children: Array<TrieNode?> = Array(26){null}) {
        private fun charToIndex(c: Char): Int = c.code - 'a'.code

        operator fun get(c: Char): TrieNode? {
            return children[charToIndex(c)]
        }
        operator fun set(c: Char, node: TrieNode) {
            children[charToIndex(c)] = node
        }
    }
    private val root: TrieNode = TrieNode(false)

    /**
     * Inserts a string into the Trie
     *
     * @param node the starting node
     * @param text the string to insert
     * @return true if the string was already in the tree
     */
    private fun insertTraversal(node: TrieNode, text: String): Boolean {
        var currNode = node
        text.forEach{c -> currNode = currNode[c] ?: TrieNode(false).also{n -> currNode[c] = n}}
        return currNode.exists.also{currNode.exists = true}
    }

    /**
     * Traverses the Trie to find a node with the given text
     *
     * @param node the starting node
     * @param text the string to find
     * @return the node if found, null if not
     */
    fun getNode(node: TrieNode, text: String): TrieNode? {
        var currNode = node
        text.forEach{currNode = currNode[it] ?: return null}
        return currNode
    }

    /**
     * Checks if a string is in the Trie
     */
    fun get(text: String): Boolean {
        return getNode(root, text) != null
    }

    /**
     * Adds a string to the Trie
     *
     * @return true if the string was already in the Trie
     */
    fun add(text: String): Boolean {
        return insertTraversal(root, text)
    }
}