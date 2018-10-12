package App

import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.FileChooser.ExtensionFilter
import java.io.File


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

    fun chooseDir() {
        val dir = selectDir("Choose directory for shortcut files")
        if(dir != null) {
            shortcutDir = dir.absolutePath
            File("dir.txt").writeText(shortcutDir)
        }
    }

    fun openShortcuts() {
        Runtime.getRuntime().exec("explorer.exe $shortcutDir")
    }

    fun shortcutToThis() {

        // Creates directories if they're not already there
        File(shortcutDir).mkdirs()
        val shortcut = File("$shortcutDir\\Taskbar shortcut converter.lnk")

        val createPs = File("$shortcutDir\\create ${shortcut.nameWithoutExtension} shortcut.ps1")
        createPs.writeText("" +
                "\$ComObj = New-Object -ComObject WScript.Shell; " +
                // Shortcut variable                     // Directory for shortcut
                "\$ShortCut = \$ComObj.CreateShortcut(\"\$Env:${shortcut.absolutePath}\");" +
                // Opens cmd (when shortcut is run)
                "\$ShortCut.TargetPath = \"%SystemRoot%\\system32\\cmd.exe\"; " +
                // Description
                "\$ShortCut.Description = \"Taskbar shortcut converter\"; " +
                // Runs jar, then closes the cmd window
                "\$ShortCut.Arguments = \"/c \"\"${getPath()}\"\"\"; " +

                "\$ShortCut.FullName; \$ShortCut.WindowStyle = 7; " +
                // Icon path
                "\$ShortCut.IconLocation = \"$iconPath\";" +
                // Saves shortcut
                "\$ShortCut.Save();" +
                // Deletes ps1 file
                "\$path = \"${createPs.absolutePath}\";" +
                "Remove-Item \$path;" +
                // Deletes bat file
                "Remove-Item \"$shortcutDir\\run ${createPs.nameWithoutExtension}.bat\";"
        )

        val runPs = File("$shortcutDir\\run ${createPs.nameWithoutExtension}.bat")
        runPs.writeText("Powershell.exe -executionpolicy remotesigned -File \"$createPs\"")

        Runtime.getRuntime().exec(runPs.absolutePath)

        var i = 0
        // Waits until the file has been created
        while(!shortcut.exists()) {
            i++
            if(i >= 6000000) return
        }

        Runtime.getRuntime().exec("explorer.exe /select, ${shortcut.absolutePath}")

    }

    private fun getPath() : String {
        return File(App::class.java.protectionDomain.codeSource.location.toURI()).path
    }

}

fun selectFile(filter:ExtensionFilter, title:String, dir:File? = null): File? {
    val fc = FileChooser()

    fc.title = title
    fc.extensionFilters.add(filter)
    if(dir != null) fc.initialDirectory = dir

    return fc.showOpenDialog(null)
}

fun selectDir(title:String) : File? {
    val dc = DirectoryChooser()

    dc.title = title

    return dc.showDialog(null)
}
