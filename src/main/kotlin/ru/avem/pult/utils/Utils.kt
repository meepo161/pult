package ru.avem.pult.utils

import javafx.event.EventHandler
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import java.awt.Desktop
import java.io.*
import java.nio.file.Paths
import java.util.*
import kotlin.math.abs

fun formatRealNumber(num: Double): Double {
    val absNum = abs(num)

    var format = "%.0f"
    when {
        absNum == 0.0 -> format = "%.0f"
        absNum < 0.1f -> format = "%.5f"
        absNum < 1f -> format = "%.4f"
        absNum < 10f -> format = "%.3f"
        absNum < 100f -> format = "%.2f"
        absNum < 1000f -> format = "%.1f"
        absNum < 10000f -> format = "%.0f"
    }
    return String.format(Locale.US, format, num).toDouble()
}

fun openFile(file: File) {
    try {
        Desktop.getDesktop().open(file)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun copyFileFromStream(_inputStream: InputStream, dest: File) {
    _inputStream.use { inputStream ->
        try {
            val fileOutputStream = FileOutputStream(dest)
            val buffer = ByteArray(1024)
            var length = inputStream.read(buffer)
            while (length > 0) {
                fileOutputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
            }
        } catch (e: FileNotFoundException) {
        }
    }
}

fun ByteArray.toHexString(numBytesRead: Int = this.size): String {
    return buildString {
        for ((i, b) in this@toHexString.withIndex()) {
            if (i == numBytesRead) break
            append(Integer.toHexString(b.toInt() and 0xFF).padStart(2, '0') + ' ')
        }
    }.toUpperCase().trim()
}

fun Int.getRange(offset: Int, length: Int = 1) = (shr(offset) and getMask(length))

private fun getMask(length: Int) = (0xFFFFFFFF).shr(32 - length).toInt()

fun TextField.callKeyBoard() {
    onTouchPressed = EventHandler {
        Desktop.getDesktop()
            .open(Paths.get("C:/Program Files/Common Files/Microsoft Shared/ink/TabTip.exe").toFile())
        requestFocus()
    }
}

fun PasswordField.callKeyBoard() {
    onTouchPressed = EventHandler {
        Desktop.getDesktop()
            .open(Paths.get("C:/Program Files/Common Files/Microsoft Shared/ink/TabTip.exe").toFile())
        requestFocus()
    }
}

fun callKeyBoard() {
    Desktop.getDesktop()
        .open(Paths.get("C:/Program Files/Common Files/Microsoft Shared/ink/TabTip.exe").toFile())
}