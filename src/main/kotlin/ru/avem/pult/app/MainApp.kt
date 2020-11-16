package ru.avem.pult.app

import javafx.scene.image.Image
import javafx.scene.input.KeyCombination
import javafx.stage.Stage
import ru.avem.pult.communication.model.CommunicationModel
import ru.avem.pult.database.connectToDB
import ru.avem.pult.database.repairDB
import ru.avem.pult.view.AuthenticationView
import ru.avem.pult.view.Styles
import tornadofx.App
import tornadofx.FX

class MainApp : App(AuthenticationView::class, Styles::class) {
    companion object {
        var isAppRunning = true
    }

    override fun init() {
        connectToDB()
        if (parameters.unnamed.contains("--init")) {
            repairDB()
        }
    }

    override fun start(stage: Stage) {
        super.start(stage)
        initializeSingletons()
        FX.primaryStage.icons += Image("app_icon.png")
        FX.primaryStage.fullScreenExitKeyCombination = KeyCombination.NO_MATCH
        FX.primaryStage.fullScreenExitHint = null
        if (!parameters.unnamed.contains("--windowed")) {
            FX.primaryStage.isFullScreen = true
        }
    }

    private fun initializeSingletons() {
        CommunicationModel
    }

    override fun stop() {
        isAppRunning = false
    }
}
