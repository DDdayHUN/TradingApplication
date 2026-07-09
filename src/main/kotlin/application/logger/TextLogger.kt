package application.logger

import domain.interfaces.ILogger
import javafx.application.Platform
import javafx.scene.control.TextArea

class TextLogger: ILogger {

    private val m_TextArea: TextArea

    override fun log(message: String) {
        Platform.runLater{
            m_TextArea.appendText(message)
        }
    }

    constructor(textArea: TextArea) {
        m_TextArea = textArea
    }
}