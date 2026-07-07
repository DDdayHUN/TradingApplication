package gui.utils

import javafx.scene.Scene
import javafx.scene.layout.Region
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style


fun applyTheme(scene: Scene, root: Region) {
    root.styleClass.add(JMetroStyleClass.BACKGROUND)
    JMetro(Style.DARK).setScene(scene)

    val customCss = object {}.javaClass.getResource("/style.css")
    if (customCss != null) {
        scene.stylesheets.add(customCss.toExternalForm())
    }
}
