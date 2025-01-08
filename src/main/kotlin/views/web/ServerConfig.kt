import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlinx.serialization.encodeToString

@Serializable
data class ServerConfig(
    val host: String = "localhost",
    val port: Int = 8080,
    val isProduction: Boolean = false,
    val productionUrl: String = "https://scrabbledockerbackend.onrender.com"
) {
    companion object {
        private const val CONFIG_FILE = "server-config.json"
        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        fun load(): ServerConfig {
            return try {
                // First try to load from resources
                val resourceStream = ServerConfig::class.java.classLoader.getResourceAsStream(CONFIG_FILE)
                if (resourceStream != null) {
                    val jsonString = resourceStream.bufferedReader().use { it.readText() }
                    json.decodeFromString<ServerConfig>(jsonString)
                } else {
                    println("Config file not found in resources, using defaults")
                    ServerConfig()
                }
            } catch (e: Exception) {
                println("Error loading config: ${e.message}")
                ServerConfig()
            }
        }

        // For development purposes, you might want to save a template config
        fun saveTemplate() {
            try {
                // Save to src/main/resources
                val resourcesDir = File("src/main/resources")
                if (!resourcesDir.exists()) {
                    resourcesDir.mkdirs()
                }

                val configFile = File(resourcesDir, CONFIG_FILE)
                val defaultConfig = ServerConfig()
                val jsonString = json.encodeToString(defaultConfig)
                configFile.writeText(jsonString)

                println("Template config saved to resources folder")
            } catch (e: Exception) {
                println("Error saving template config: ${e.message}")
            }
        }
    }
}