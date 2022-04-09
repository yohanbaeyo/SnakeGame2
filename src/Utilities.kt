import Constants.Companion.CELL_HORIZONTAL_CNT
import java.awt.Graphics
import Constants.Companion.CELL_SIZE
import Constants.Companion.CELL_VERTICAL_CNT

enum class CellType {
    HEAD,BODY, END, TARGET
}

enum class Direction {
    LEFT, RIGHT, DOWN, UP, NONE;

    fun isOpposite(other: Direction): Boolean {
        return when(other) {
            LEFT -> this==RIGHT;
            RIGHT -> this==LEFT;
            DOWN -> this==UP;
            UP -> this==DOWN;
            NONE -> false;
        }
    }
}

data class Position(var x: Int, var y: Int) {
    companion object {
        val leftBottomPosition = Position(0,0)
        val rightTopPosition = Position(CELL_HORIZONTAL_CNT, CELL_VERTICAL_CNT)
    }

    operator fun plus(b: Position): Position {
        return Position(x+b.x, y+b.y)
    }

    fun isInBoundary(p1: Position, p2: Position) = (p1.x<=x && x<p2.x && p1.y<=y && y<p2.y)
    fun isInBoundary() = isInBoundary(leftBottomPosition, rightTopPosition)
}

data class Cell(var position: Position, var type: CellType, var inputDirection: Direction, var outputDirection: Direction) {
    constructor(position: Position, type: CellType) : this(position, type, Direction.NONE, Direction.NONE) {
        if(type != CellType.TARGET) {
            throw error("None-Target cells must have valid direction.")
        }
    }
    fun drawCell(g: Graphics) {
        g.fillRect(position.x * CELL_SIZE, position.y * CELL_SIZE, CELL_SIZE, CELL_SIZE)
    }
}