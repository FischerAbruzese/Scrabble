package controllers.util

import models.turn.Direction
import models.turn.Direction.ACROSS
import models.turn.Direction.DOWN

fun String.isValidScrabbleWord(): Boolean {
    return true //todo
}

fun Direction.perpendicular(): Direction {
    if (this == DOWN) return ACROSS
    return DOWN
}

fun StringBuilder.appendLn(text: String) = append(text).append("\n")