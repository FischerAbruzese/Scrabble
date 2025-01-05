package util

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.GameState
import models.board.Multiplier

fun serializeGameState(gameState: GameState): String {
    val game = SerializedGameState(
        gameState.players.map { player ->
            SerializedPlayer(
                player.name,
                SerializedHand(
                    player.hand.pieces.map {
                        SerializedPiece(
                            it.letter,
                            it.value
                        )
                    }
                ),
                player.score
            )
        },
        SerializedBoard(
            gameState.board.board.map { r ->
                r.map { s ->
                    SerializedSquare(
                        s.multiplier,
                        s.piece?.let { SerializedPiece(it.letter, it.value) },
                        s.turnPlaced,
                        s.playerPlaced?.let { player ->
                            SerializedPlayer(
                                player.name,
                                SerializedHand(
                                    s.playerPlaced.hand.pieces.map {
                                        SerializedPiece(
                                            it.letter,
                                            it.value
                                        )
                                    }
                                ),
                                s.playerPlaced.score
                            )
                        }
                    )
                }
            }
        ),
        gameState.turnNum,
        gameState.passStreak
    )

    return Json.encodeToString(game)
}

// Serialization data classes for all nested types
@Serializable
data class SerializedGameState(
    val players: List<SerializedPlayer>,
    val board: SerializedBoard,
    val turnNum: Int,
    val passStreak: Int
)

@Serializable
data class SerializedPlayer(
    val name: String,
    val hand: SerializedHand,
    val score: Int
)

@Serializable
data class SerializedHand(
    val pieces: List<SerializedPiece>
)

@Serializable
data class SerializedBoard(
    val squares: List<List<SerializedSquare>>
)

@Serializable
data class SerializedSquare(
    val multiplier: Multiplier,
    val piece: SerializedPiece?,
    val turnPlaced: Int?,
    val playerPlaced: SerializedPlayer?
)

@Serializable
data class SerializedPiece(
    val letter: Char,
    val value: Int
)

