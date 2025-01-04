package controllers.util

import controllers.players.PlayerController
import models.GameState
import models.Player
import models.board.Board
import models.board.Multiplier
import models.board.Square
import models.tiles.Bag
import models.tiles.Hand
import models.tiles.Piece

// Serialization data classes for all nested types
data class SerializedGameState(
    val players: List<SerializedPlayer>,
    val bag: SerializedBag,
    val board: SerializedBoard,
    val turnNum: Int,
    val passStreak: Int
)

data class SerializedPlayer(
    val name: String,
    val controllerClassName: String,
    val hand: SerializedHand,
    val score: Int
)

data class SerializedHand(
    val pieces: List<SerializedPiece>
)

data class SerializedBag(
    val pieces: List<SerializedPiece>
)

data class SerializedBoard(
    val squares: List<List<SerializedSquare>>
)

data class SerializedSquare(
    val multiplier: Multiplier,
    val piece: SerializedPiece?,
    val turnPlaced: Int?,
    val playerPlaced: SerializedPlayer?
)

data class SerializedPiece(
    val letter: Char,
    val value: Int
)

// Extension functions for serializing/deserializing each type
fun GameState.serialize(): SerializedGameState {
    return SerializedGameState(
        players = players.map { it.serialize() },
        bag = bag.serialize(),
        board = board.serialize(),
        turnNum = turnNum,
        passStreak = passStreak
    )
}

fun Player.serialize(): SerializedPlayer {
    return SerializedPlayer(
        name = name,
        controllerClassName = playerController::class.java.name,
        hand = hand.serialize(),
        score = score
    )
}

fun Hand.serialize(): SerializedHand {
    return SerializedHand(
        pieces = pieces.map { it.serialize() }
    )
}

fun Bag.serialize(): SerializedBag {
    return SerializedBag(
        pieces = pieces.map { it.serialize() }
    )
}

fun Board.serialize(): SerializedBoard {
    return SerializedBoard(
        squares = board.map { row ->
            row.map { it.serialize() }
        }
    )
}

fun Square.serialize(): SerializedSquare {
    return SerializedSquare(
        multiplier = multiplier,
        piece = piece?.serialize(),
        turnPlaced = turnPlaced,
        playerPlaced = playerPlaced?.serialize()
    )
}

fun Piece.serialize(): SerializedPiece {
    return SerializedPiece(
        letter = letter,
        value = value
    )
}

// Deserialization extension functions
fun SerializedGameState.deserialize(): GameState {
    return GameState(
        players = players.map { it.deserialize() },
        bag = bag.deserialize(),
        board = board.deserialize(),
        turnNum = turnNum,
        passStreak = passStreak
    )
}

fun SerializedPlayer.deserialize(): Player {
    try {
        val controllerClass = Class.forName(controllerClassName)
        val controller = controllerClass.getDeclaredConstructor().newInstance() as PlayerController

        return Player(
            name = name,
            playerController = controller,
            hand = hand.deserialize(),
            score = score
        )
    } catch (e: Exception) {
        throw IllegalStateException("Failed to controllers.util.deserialize player controller: $controllerClassName", e)
    }
}

fun SerializedHand.deserialize(): Hand {
    return Hand(pieces.map { it.deserialize() })
}

fun SerializedBag.deserialize(): Bag {
    return Bag(pieces.map { it.deserialize() })
}

fun SerializedBoard.deserialize(): Board {
    return Board(squares.map { row ->
        row.map { it.deserialize() }.toTypedArray()
    }.toTypedArray())
}

fun SerializedSquare.deserialize(): Square {
    return Square(
        multiplier = multiplier,
        piece = piece?.deserialize(),
        turnPlaced = turnPlaced,
        playerPlaced = playerPlaced?.deserialize()
    )
}

fun SerializedPiece.deserialize(): Piece {
    return Piece(letter, value)
}