import java.awt.Graphics
import Constants.Companion.CELL_SIZE;

enum class SnakeCellType {
    HEAD,BODY, END
}

enum class Direction {
    LEFT, RIGHT, DOWN, UP
}

data class Position(var x: Int, var y: Int) {
    operator fun plus(b: Position): Position {
        return Position(x+b.x, y+b.y)
    }

    fun isInBoundary(p1: Position, p2: Position) = (p1.x<=x && x<p2.x && p1.y<=y && y<p2.y)
}

data class Cell(var position: Position, var type: SnakeCellType, var direction: Direction) {
    fun drawCell(g: Graphics) {
        g.fillRect(position.x * CELL_SIZE, position.y * CELL_SIZE, CELL_SIZE, CELL_SIZE)
    }
}

interface Constants {
    companion object {
        const val CELL_SIZE = 20
        const val CELL_HORIZONTAL_CNT = 40
        const val CELL_VERTICAL_CNT = 30
        const val width = CELL_SIZE * CELL_HORIZONTAL_CNT
        const val height = CELL_SIZE * CELL_VERTICAL_CNT
    }
}