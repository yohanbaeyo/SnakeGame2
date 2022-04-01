import Constants.Companion.CELL_HORIZONTAL_CNT
import Constants.Companion.CELL_VERTICAL_CNT
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.math.exp


class GameController : JFrame("SnakeGame"){
    private val mainPanel = MainPanel()
    private val uiController = UIController()
    private var gameThread = Thread(GameThread())

    private var tmpDirection = Direction.NONE
    private val snakeBody: ArrayDeque<Cell> = ArrayDeque()
    private val isOccupied = Array(CELL_HORIZONTAL_CNT) {
        Array(CELL_VERTICAL_CNT) { false }
    }
    private val remainPosition = HashSet<Position>()
    private val target = Cell(Position(-1, -1), CellType.TARGET)

    var milliSecondsPerFrame: Long = 400
    var finished = true

    private fun updateTarget() {
        target.position = remainPosition.random()
        remainPosition.remove(target.position)
    }

    fun initializeGame() {
        snakeBody.clear()
        snakeBody.addFirst(Cell(Position(CELL_HORIZONTAL_CNT/2, CELL_VERTICAL_CNT/2), CellType.HEAD, Direction.UP, Direction.UP))

        isOccupied.forEach {
            it.fill(false)
        }
        isOccupied[CELL_HORIZONTAL_CNT/2][CELL_VERTICAL_CNT/2] = true

        remainPosition.clear()
        for(i in 0 until CELL_HORIZONTAL_CNT) {
            for(j in 0 until CELL_VERTICAL_CNT) {
                if (!isOccupied[i][j]) {
                    remainPosition.add(Position(i, j))
                }
            }
        }
        tmpDirection = Direction.UP

        updateTarget()
        finished = false
    }

    fun startGame() {
        gameThread.start()
    }

    private fun gameLoop() {
        initializeGame()
        while(!finished) {
            val (tmpFinished, tmpMilli) = tick()
            finished = tmpFinished
            milliSecondsPerFrame= tmpMilli

            Thread.sleep(milliSecondsPerFrame)
        }
    }

    init {
        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if(e==null) return

                if(e.keyCode == KeyEvent.VK_R) {
                    if(!finished) {
                        initializeGame()
                    } else {
                        gameThread = Thread(GameThread())
                        gameThread.start()
                    }
                    return
                }
                tmpDirection = when (e.keyCode) {
                    KeyEvent.VK_LEFT, KeyEvent.VK_A -> Direction.LEFT
                    KeyEvent.VK_RIGHT, KeyEvent.VK_D -> Direction.RIGHT
                    KeyEvent.VK_UP, KeyEvent.VK_W -> Direction.UP
                    KeyEvent.VK_DOWN, KeyEvent.VK_S -> Direction.DOWN
                    else -> tmpDirection
                }
            }
        })

        mainPanel.modifyLabelText("Press R to Begin Game.")
    }

    private fun tick(): Pair<Boolean, Long> {
        val tmpPos = snakeBody[0].position + when(tmpDirection) {
            Direction.RIGHT -> Position(1, 0)
            Direction.LEFT -> Position(-1, 0)
            Direction.DOWN -> Position(0, 1)
            Direction.UP -> Position(0, -1)
            Direction.NONE -> throw error("NONE shouldn't be a direction of snake body")
        }

//        println(isOccupied[tmpPos.x][tmpPos.y])

        if(tmpPos.isInBoundary() && !isOccupied[tmpPos.x][tmpPos.y]) {

            snakeBody[0].type = CellType.BODY
            snakeBody[0].outputDirection = tmpDirection

            snakeBody.addFirst(Cell(tmpPos, CellType.HEAD, tmpDirection, tmpDirection))
            isOccupied[tmpPos.x][tmpPos.y] = true
            remainPosition.remove(tmpPos)

            if (target.position != tmpPos) {
                isOccupied[snakeBody.last().position.x][snakeBody.last().position.y] = false
                remainPosition.add(snakeBody.last().position)

                snakeBody.removeLast()
                snakeBody.last().type = CellType.END
            } else {
                updateTarget()
            }
            milliSecondsPerFrame = (350/ exp((snakeBody.size-1).toDouble()/20) + 50).toLong()

            uiController.updateScreen()

            return false to milliSecondsPerFrame
        } else {

            mainPanel.modifyLabelText("Game Over!")
            return true to 0
        }
    }

    inner class UIController {
        init {
//        setSize(814, 636)
//        size = Dimension(Constants.width, Constants.height)
            rootPane.preferredSize = Dimension(Constants.width, Constants.height)
            pack()
            add(mainPanel)
            isVisible = true

            defaultCloseOperation = EXIT_ON_CLOSE
        }

        fun updateScreen() {
            mainPanel.repaint()
            mainPanel.modifyLabelText()
        }
    }

    inner class MainPanel: JPanel() {
        private val frameRateLabel = JLabel("")

        init {
            frameRateLabel.font = Font(name, Font.PLAIN, 18)
            add(frameRateLabel)
        }

        override fun paintComponent(g: Graphics?) {
            super.paintComponent(g)
            if (g == null) return

            g.color = Color.BLACK
//            g.drawRect(1, 1, i*10, j*10)
            snakeBody.forEach { cell ->
                cell.drawCell(g)
            }

            g.color = Color.RED
            target.drawCell(g)
//            g.fillRect(mainUI.targetPosition.x * CELL_SIZE, mainUI.targetPosition.y * CELL_SIZE, CELL_SIZE, CELL_SIZE)
        }

        fun modifyLabelText() {
            frameRateLabel.text = String.format("BlockPerSecond: %.3f", 1000/milliSecondsPerFrame.toDouble())
        }
        fun modifyLabelText(newText: String) {
            frameRateLabel.text = newText
        }
    }

    inner class GameThread : Runnable {
        override fun run() {
            gameLoop()
        }
    }
}