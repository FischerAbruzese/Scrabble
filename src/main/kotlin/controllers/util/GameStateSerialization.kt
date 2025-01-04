package controllers.util

import controllers.players.PlayerController
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.GameState
import models.Player
import models.board.Board
import models.board.Multiplier
import models.board.Square
import models.tiles.Bag
import models.tiles.Hand
import models.tiles.Piece

@Serializable
data class SerializedPlayer(
    val name: String,
    val hand: List<SerializedPiece>,
    val score: Int
)

@Serializable
data class SerializedPiece(
    val letter: Char,
    val value: Int
)

@Serializable
data class SerializedSquare(
    val multiplier: Multiplier,
    val piece: SerializedPiece?,
    val turnPlaced: Int?,
    val playerName: String?
)

@Serializable
data class SerializedGameState(
    val players: List<SerializedPlayer>,
    val bag: List<SerializedPiece>,
    val board: List<List<SerializedSquare>>,
    val turnNum: Int,
    val passStreak: Int
)

fun GameState.serialize(): String {
    val serializedPlayers = players.map { player ->
        SerializedPlayer(
            name = player.name,
            hand = player.hand.pieces.map { piece ->
                SerializedPiece(piece.letter, piece.value)
            },
            score = player.score
        )
    }

    val serializedBag = bag.pieces.map { piece ->
        SerializedPiece(piece.letter, piece.value)
    }

    val serializedBoard = board.board.map { row ->
        row.map { square ->
            SerializedSquare(
                multiplier = square.multiplier,
                piece = square.piece?.let { SerializedPiece(it.letter, it.value) },
                turnPlaced = square.turnPlaced,
                playerName = square.playerPlaced?.name
            )
        }
    }

    val serializedState = SerializedGameState(
        players = serializedPlayers,
        bag = serializedBag,
        board = serializedBoard,
        turnNum = turnNum,
        passStreak = passStreak
    )

    return Json.encodeToString(serializedState)
}

fun deserializeGameState(json: String, controllers: List<PlayerController>): GameState {
    val serializedState = Json.decodeFromString<SerializedGameState>(json)

    // Validate number of controllers matches number of players
    require(controllers.size == serializedState.players.size) {
        "Number of controllers (${controllers.size}) must match number of players (${serializedState.players.size})"
    }

    // Reconstruct players
    val players = serializedState.players.zip(controllers) { playerData, controller ->
        Player(
            name = playerData.name,
            playerController = controller,
            hand = Hand(playerData.hand.map { Piece(it.letter, it.value) }),
            score = playerData.score
        )
    }

    // Reconstruct bag
    val bag = Bag(serializedState.bag.map { Piece(it.letter, it.value) })

    // Reconstruct board
    val board = Board(serializedState.board.map { row ->
        row.map { squareData ->
            Square(
                multiplier = squareData.multiplier,
                piece = squareData.piece?.let { Piece(it.letter, it.value) },
                turnPlaced = squareData.turnPlaced,
                playerPlaced = squareData.playerName?.let { name ->
                    players.find { it.name == name }
                }
            )
        }.toTypedArray()
    }.toTypedArray())

    return GameState(
        players = players,
        bag = bag,
        board = board,
        turnNum = serializedState.turnNum,
        passStreak = serializedState.passStreak
    )
}