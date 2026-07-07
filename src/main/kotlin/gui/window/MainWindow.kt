package gui.window

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.stage.Stage
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style

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

        root.styleClass.add(JMetroStyleClass.BACKGROUND)


        val scene = Scene(root,1300.0, 700.0)

        val jMetro = JMetro(Style.DARK)
        jMetro.setScene(scene)

        stage.title = "Trading"
        stage.scene = scene
        stage.show()
    }
}