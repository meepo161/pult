package ru.avem.pult.view

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import ru.avem.pult.protocol.Saver.saveProtocolAsWorkbook
import ru.avem.pult.viewmodels.MainViewModel
import tornadofx.*
import java.io.File
import kotlin.concurrent.thread

class ProtocolsSaveProgressView : View("") {
    private val model: MainViewModel by inject()
    lateinit var dir: File

    private var totalLabel: Label by singleAssign()
    private var saveProgress: ProgressBar by singleAssign()
    private var executingLabel: Label by singleAssign()

    override fun onDock() {
        super.onDock()

        thread {
            startSaving()
            runLater {
                close()
            }
        }
    }

    override val root = vbox {
        prefWidth = 400.0
        prefHeight = 200.0

        alignment = Pos.CENTER
        totalLabel = label("Сохранение протоколов")
        saveProgress = progressbar()
        executingLabel = label()
    }

    private fun startSaving() {
        val totalFiles = model.protocols.size
        model.protocols.forEachIndexed { index, protocol ->
            val file = File(dir, "${protocol.date}_${protocol.time.replace(":", "")}.xlsx")
            saveProtocolAsWorkbook(protocol, file.absolutePath)
            runLater {
                totalLabel.text = "Cохранение протокола ${index + 1}/${totalFiles}"
                executingLabel.text = file.absolutePath
                saveProgress.progress = index.toDouble() / totalFiles
            }
        }
    }
}
