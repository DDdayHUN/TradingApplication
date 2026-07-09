package gui.window

import applyTheme
import javafx.animation.PauseTransition
import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.stage.Stage
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import kotlin.random.Random
import javafx.util.Duration

class DemoWindow: Application() {
    override fun start(stage: Stage) {
        val title = Label("Trading App").apply {
            style = "-fx-font-size: 20px;"
        }
        var counter = 0

        val closeButton = Button("Close").apply{
            prefWidth = 100.0
            isVisible = false

            setOnMouseEntered{
                counter++

                if(counter <= 10){
                    translateX = Random.nextDouble(-500.0, 500.0)
                    translateY = Random.nextDouble(-300.0, 300.0)
                    if(counter == 5){
                        title.text = "PRESS THE BUTTON!!!"
                    }
                }else{
                    title.text = "IDIOT"
                    translateX = 0.0
                    translateY = 0.0
                }
            }

            setOnAction{
                text = "Closing..."
                title.text = "LOL"
                val delay = PauseTransition(Duration.seconds(1.0))
                delay.setOnFinished {
                    stage.close()
                }
                delay.play()
            }
        }

        val runButton = Button("Run").apply {
            prefWidth = 300.0

            setOnAction {
                title.text = "GAY"

                title.style = """
                    -fx-font-size: 50px;
                    -fx-font-weight: bold;
                    -fx-text-fill: red;
                """.trimIndent()
                visibleProperty().set(false)
                closeButton.isVisible = true
            }
        }

        val root = VBox(12.0, title, runButton, closeButton).apply{
            alignment = Pos.CENTER
        }

        val scene = Scene(root,1300.0, 700.0)

        applyTheme(scene, root)

        stage.title = "Trading"
        stage.scene = scene
        stage.show()
    }
}