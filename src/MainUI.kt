import java.awt.Color
import java.awt.Graphics
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import Constants.Companion.CELL_SIZE;
import Constants.Companion.CELL_HORIZONTAL_CNT;
import Constants.Companion.CELL_VERTICAL_CNT;
import Constants.Companion.height
import Constants.Companion.width
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class MainUI : JFrame("SnakeGame") {
    val snakeBody: ArrayDeque<Cell> = ArrayDeque()
    private val mainPanel = MainPanel(this)
    var tmpDirection = Direction.UP
    private val isOccupied = Array(CELL_HORIZONTAL_CNT){
        Array(CELL_VERTICAL_CNT){
            false
        }
    }
    private var remainPosition = HashSet<Position>()
    lateinit var targetPosition: Position

    private fun updateTarget() {
        targetPosition = remainPosition.random()
        remainPosition.remove(targetPosition)
    }

    init {
        snakeBody.addFirst(Cell(Position(CELL_HORIZONTAL_CNT/2, CELL_VERTICAL_CNT/2), SnakeCellType.HEAD, Direction.UP))
        isOccupied[CELL_HORIZONTAL_CNT/2][CELL_VERTICAL_CNT/2] = true

        for(i in 0 until CELL_HORIZONTAL_CNT) {
            for(j in 0 until CELL_VERTICAL_CNT) {
                if (!isOccupied[i][j]) {
                    remainPosition.add(Position(i, j))
                }
            }
        }

        updateTarget()

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                tmpDirection = when(e!!.keyCode) {
                    KeyEvent.VK_LEFT, KeyEvent.VK_A ->  Direction.LEFT
                    KeyEvent.VK_RIGHT, KeyEvent.VK_D -> Direction.RIGHT
                    KeyEvent.VK_UP, KeyEvent.VK_W -> Direction.UP
                    KeyEvent.VK_DOWN, KeyEvent.VK_S -> Direction.DOWN
                    else -> tmpDirection
                }
            }
        })

//        setSize(814, 636)
//        size = Dimension(Constants.width, Constants.height)
        rootPane.preferredSize = Dimension(Constants.width, Constants.height)
        pack()
        add(mainPanel)
        isVisible = true

        defaultCloseOperation = EXIT_ON_CLOSE
    }

    fun tick(): Pair<Boolean, Long> {
        val tmpPos = snakeBody[0].position + when(tmpDirection) {
            Direction.RIGHT -> Position(1, 0)
            Direction.LEFT -> Position(-1, 0)
            Direction.DOWN -> Position(0, 1)
            Direction.UP -> Position(0, -1)
        }

        if(tmpPos.isInBoundary(Position(0, 0), Position(CELL_HORIZONTAL_CNT, CELL_VERTICAL_CNT))
            && !isOccupied[tmpPos.x][tmpPos.y]) {
            snakeBody[0].type = SnakeCellType.BODY
            /*mainUI.snakeBody[0].shape = when(mainUI.tmpDirection) {
                Direction.LEFT ->
            }*/
            snakeBody.addFirst(Cell(tmpPos, SnakeCellType.HEAD, tmpDirection))
            isOccupied[tmpPos.x][tmpPos.y] = true
            remainPosition.remove(tmpPos)

            if (targetPosition != tmpPos) {
                isOccupied[snakeBody.last().position.x][snakeBody.last().position.y] = false
                remainPosition.add(snakeBody.last().position)

                snakeBody.removeLast()
                snakeBody.last().type = SnakeCellType.END
            } else {
                updateTarget()
            }

//                    println(snakeBody[0].position)
            mainPanel.repaint()

            val milliSecondsPerFrame = (350/Math.exp((snakeBody.size-1).toDouble()/20)+ 50).toLong()
            mainPanel.modifyLabelText(milliSecondsPerFrame)
            return false to milliSecondsPerFrame
        } else {
            return true to 0
        }
    }

    class MainPanel(private val mainUI: MainUI): JPanel() {
        private val frameRateLabel = JLabel("")

        init {
            add(frameRateLabel)
        }

        override fun paintComponent(g: Graphics?) {
            super.paintComponent(g)
            if (g == null) return

            g.color = Color.BLACK
//            g.drawRect(1, 1, i*10, j*10)
            mainUI.snakeBody.forEach { cell ->
                cell.position.drawCell(g)
            }

            g.color = Color.RED
            mainUI.targetPosition.drawCell(g)
//            g.fillRect(mainUI.targetPosition.x * CELL_SIZE, mainUI.targetPosition.y * CELL_SIZE, CELL_SIZE, CELL_SIZE)
        }

        fun modifyLabelText(milliSecondsPerFrame: Long) {
            frameRateLabel.text = String.format("BlockPerSecond: %.3f", 1000/milliSecondsPerFrame.toDouble())
        }
    }
}