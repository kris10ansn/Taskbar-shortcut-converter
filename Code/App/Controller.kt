package App

import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import java.io.File

var shortcutDir = "${System.getProperty("user.home")}\\TaskbarShortcuts"
var iconPath:String = "icon.png"

// TODO: Change icon when icon is chosen, image file converter
class Controller {

    fun iconButtonClicked() {
        val icon = selectFile(ExtensionFilter("Icon files", "*.ico"), "Select icon")
        if(icon != null) {
            iconPath = icon.absolutePath
        }
    }

    fun createButtonClicked() {
        createShortcut(
                selectFile(
                        ExtensionFilter(".url shortcut file", "*.url"),
                        "Select .url shortcut file"
                ) as File,
                iconPath)
    }

    fun chooseDirMenuBtn() {
        val dir = selectDir("Choose directory for shortcut files")
        if(dir != null) {
            shortcutDir = dir.absolutePath
            File("dir.txt").writeText(shortcutDir)
        }
    }

    fun openShortcutsMenuBtn() { Runtime.getRuntime().exec("explorer.exe $shortcutDir") }

    private fun createShortcut(f:File, iconpath:String) {
        val ftext = f.readText()

        val start = ftext.indexOf("URL=") + 4 // Length of "URL=" is 4

        var end = ftext.indexOf("IconFile=")
        if(end == -1) end = ftext.length-1

        val url = ftext.substring(start until end)

        // Creates directories if they're not already there
        File(shortcutDir).mkdirs()

        val shortcut = File("$shortcutDir\\${f.nameWithoutExtension}.lnk")

        val command = "" +
                "\$ComObj = New-Object -ComObject WScript.Shell; " +
                // Shortcut variable                     // Directory for shortcut
                "\$ShortCut = \$ComObj.CreateShortcut(\\\"\$Env:${shortcut.absolutePath}\\\"); " +
                // Opens cmd (when shortcut is run)
                "\$ShortCut.TargetPath = \\\"%windir%\\system32\\cmd.exe\\\"; " +
                // Description
                "\$ShortCut.Description = \\\"${f.nameWithoutExtension}\\\"; " +
                // Runs command then closes prompt ("/c")
                "\$ShortCut.Arguments = \\\"/c \\\"\\\"start $url\\\"\\\"\\\"; " +
                // Stuff i don't quite know what do, and don't bother to look up atm
                "\$ShortCut.FullName; \$ShortCut.WindowStyle = 7; " +
                // Icon path
                "\$ShortCut.IconLocation = \\\"$iconpath\\\"; " +
                // Saves shortcut
                "\$ShortCut.Save(); "

        Runtime.getRuntime().exec("Powershell.exe -executionpolicy remotesigned -command \"$command\"")


        var i = 0
        // Waits until the file has been created
        while(!shortcut.exists()) {
            i++
            if(i >= 3000000) {
                // If it takes too long, an error message is printed out, and the script is stopped
                println("Shortcut might not have been created. Check out $shortcutDir")
                return
            }
        }

        Runtime.getRuntime().exec("explorer.exe /select, ${shortcut.absolutePath}")

    }

    private fun selectFile(filter:ExtensionFilter, title:String, dir:File? = null): File? {
        val fc = FileChooser()

        fc.title = title
        fc.extensionFilters.add(filter)
        if(dir != null) fc.initialDirectory = dir

        return fc.showOpenDialog(null)
    }

    private fun selectDir(title:String) : File? {
        val dc = DirectoryChooser()

        dc.title = title

        return dc.showDialog(null)
    }

}
