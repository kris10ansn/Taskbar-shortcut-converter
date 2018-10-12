package App

import java.io.File

var shortcutDir = "${System.getProperty("user.home")}\\TaskbarShortcuts"
var iconPath:String = "icon.png"

fun createShortcut(f:File, iconpath:String) {
    val ftext = f.readText()

    val start =ftext.indexOf("URL=") + 4
    var end = ftext.indexOf("IconFile=")

    if(end == -1) end = ftext.length-1

    val url = ftext.substring(start until end)

    // Creates directories if they're not already there
    File(shortcutDir).mkdirs()


    val shortcutBat = File("$shortcutDir\\${f.nameWithoutExtension}.bat")
    shortcutBat.writeText("start $url")
    // Hides bat file
    Runtime.getRuntime().exec("attrib +H $shortcutBat")

    val shortcut = File("$shortcutDir\\${shortcutBat.nameWithoutExtension}.lnk")

    val createPs = File("$shortcutDir\\create ${f.nameWithoutExtension} shortcut.ps1")
    createPs.writeText("" +
            "\$ComObj = New-Object -ComObject WScript.Shell; " +
            // Shortcut variable                     // Directory for shortcut
            "\$ShortCut = \$ComObj.CreateShortcut(\"\$Env:${shortcut.absolutePath}\");" +
            // Opens cmd (when shortcut is run)
            "\$ShortCut.TargetPath = \"%windir%\\system32\\cmd.exe\"; " +
            // Description
            "\$ShortCut.Description = \"${f.nameWithoutExtension}\"; " +

            // Runs shortcutBat, then closes the cmd window (/c can be changed with /k to stay open)
            // (when shortcut is run)
            "\$ShortCut.Arguments = \"/c \"\"$shortcutBat\"\"\"; " +

            "\$ShortCut.FullName; \$ShortCut.WindowStyle = 7; " +
            // Icon path
            "\$ShortCut.IconLocation = \"$iconpath\";" +
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
        if(i >= 6000000) {
            // If it takes too long, an error message is printed out, and the script is stopped
            println("Shortcut might not have been created. Check out $shortcutDir")
            return
        }
    }


    Runtime.getRuntime().exec("explorer.exe /select, ${shortcut.absolutePath}")

}