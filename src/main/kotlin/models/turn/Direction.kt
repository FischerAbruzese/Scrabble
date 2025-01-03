package models.turn

enum class Direction {
    DOWN, ACROSS;

    companion object {
        fun perpendicularTo(direction: Direction): Direction {
            if (direction == Direction.DOWN) return Direction.ACROSS
            return Direction.DOWN
        }
    }
}