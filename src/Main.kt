fun main() {
    val mainUI = MainUI()
    var finished = false
    var milliSecondsPerFrame: Long = 0

    while(!finished) {
        val (tmpFinished, tmpMilli) = mainUI.tick()
        finished = tmpFinished
        milliSecondsPerFrame= tmpMilli

        Thread.sleep(milliSecondsPerFrame)
    }
}