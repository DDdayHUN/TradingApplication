import javafx.scene.Scene
import javafx.scene.layout.Region
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

/**
 * Formats this Double into a String with a fixed number of decimal places.
 *
 * @param value number of digits after the decimal point.
 * @return string representation of the double rounded to the specified precision.
 */
fun Double.format(value: Int): String {
    return "%.${value}f".format(this)
}
 @OptIn(ExperimentalPathApi::class)
 fun clearTestFolder() {
     val testDir = Path.of("src", "main", "resources", "traders", "test")

     if (!testDir.exists()) return

     Files.list(testDir).use { files ->
         files.forEach { file ->
             file.deleteRecursively()
         }
     }
 }

fun applyTheme(scene: Scene, root: Region) {
    root.styleClass.add(JMetroStyleClass.BACKGROUND)
    JMetro(Style.DARK).setScene(scene)

    val customCss = object {}.javaClass.getResource("/style.css")
    if (customCss != null) {
        scene.stylesheets.add(customCss.toExternalForm())
    }
}