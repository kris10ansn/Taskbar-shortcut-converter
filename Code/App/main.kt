package App

import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.File

fun main(args: Array<String>) {

    try {
        shortcutDir = File("dir.txt").readText()
    } catch (e:Exception) {  }

    Application.launch(App::class.java)
}

class App : Application() {
    override fun start(stage: Stage?) {
        val root: Parent = FXMLLoader.load(javaClass.classLoader.getResource("FXML/home.fxml"))
        val scene = Scene(root)

        stage?.isResizable = false

        stage?.scene = scene
        stage?.title = "Taskbar shortcut converter"
        stage?.show()
    }
}