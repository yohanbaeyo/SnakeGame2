import Constants.Companion.CELL_HORIZONTAL_CNT
import Constants.Companion.CELL_VERTICAL_CNT
import kotlinx.coroutines.*
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.io.FileWriter
import java.util.Scanner
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.math.exp
import kotlin.math.max


class GameController : JFrame("SnakeGame"){
    private val mainPanel = MainPanel()
    private val uiController = UIController()
    private val scoreController = ScoreController()

    private var tmpDirection = Direction.NONE
    private val snakeBody = ArrayDeque<Cell>()
    private val isOccupied = Array(CELL_HORIZONTAL_CNT) {
        Array(CELL_VERTICAL_CNT) { false }
    }
    private val remainPosition = HashSet<Position>()
    private val target = Cell(Position(-1, -1), CellType.TARGET)

    var milliSecondsPerFrame: Long = 400
    var finished = true
    private var score = 0

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
        score = 0
    }

    fun startGame() {
        CoroutineScope(Dispatchers.IO).launch {
            gameLoop()
        }
    }

    private suspend fun  gameLoop() {
        initializeGame()
        while(!finished) {
            val (tmpFinished, tmpMilli) = tick()
            finished = tmpFinished
            milliSecondsPerFrame= tmpMilli

            delay(milliSecondsPerFrame)
        }
        score = snakeBody.size
        println(score)
        scoreController.updateScore()
        scoreController.scoreShow("Game Over!")
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            scoreController.loadRecord()
        }

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if(e==null) return

                if(e.keyCode == KeyEvent.VK_R) {
                    if(!finished) {
                        initializeGame()
                    } else {
                        startGame()
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

            g.color = Color.GRAY
//            g.drawRect(1, 1, i*10, j*10)
            snakeBody.forEach { cell ->
                cell.drawCell(g)
            }

            g.color = Color(241,109,109)
            target.drawCell(g)
//            g.fillRect(mainUI.targetPosition.x * CELL_SIZE, mainUI.targetPosition.y * CELL_SIZE, CELL_SIZE, CELL_SIZE)
        }

        fun modifyLabelText() {
            frameRateLabel.text = String.format("BPS: %.1f", 1000/milliSecondsPerFrame.toDouble())
        }
        fun modifyLabelText(newText: String) {
            frameRateLabel.text = newText
        }
    }

    inner class ScoreController {
        private var bestScore = -1
        private var latestScore = -1

        fun loadRecord() {
//            val recordFile = javaClass.getResourceAsStream("data.dat") ?: throw error("File Not Found")
//            println(javaClass.getResource("data.dat").path)
//            println(javaClass.getResource("data.dat").path)
            val scanner = Scanner((javaClass.getResourceAsStream("data.dat") ?: throw error("File Not Found")))

            latestScore = scanner.nextInt()
            bestScore = scanner.nextInt()

            println(latestScore)
            println(bestScore)

            scanner.close()
        }

        fun updateScore() {
            this.latestScore = score
            this.bestScore = max(this.bestScore, score)

//            val writer = DataOutputStream(FileOutputStream((javaClass.getResource("data.dat")?:throw error("File Not Found")).path))
//
            val writer = FileWriter(javaClass.getResource("data.dat")?.path.toString())

            writer.write(score.toString())
            writer.write("\n")
            writer.write(bestScore.toString())

            writer.close()
        }

        fun scoreShow(prefix: String) {
            mainPanel.modifyLabelText(prefix + " (latest score: ${latestScore}, best score: ${bestScore})")
        }
    }
}