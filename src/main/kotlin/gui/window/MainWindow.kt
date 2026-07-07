package gui.window

import gui.utils.applyTheme
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.stage.Stage
import javafx.scene.control.Label
import javafx.scene.layout.VBox

class MainWindow: Application() {
    override fun start(stage: Stage) {
        val title = Label("Trading  App")
        val runButton = Button("Run").apply{
            prefWidth = 400.0
        }


        runButton.setOnAction{
            title.text = "Running..."
        }

        val root = VBox(12.0, title, runButton)

        val scene = Scene(root,1300.0, 700.0)

        applyTheme(scene, root)

        stage.title = "Trading"
        stage.scene = scene
        stage.show()
    }
}