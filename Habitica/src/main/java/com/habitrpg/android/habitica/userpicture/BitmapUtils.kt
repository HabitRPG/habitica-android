package com.habitrpg.android.habitica.userpicture

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object BitmapUtils {
    fun clearDirectoryContent(directory: String) {
        val dirFile = File(directory)
        dirFile.listFiles()?.forEach { it.delete() }
    }

    fun saveToShareableFile(directory: String, filename: String, bmp: Bitmap): File? {
        var name = filename
        try {
            name = "$directory/$name"
            val dirFile = File(directory)
            if (!dirFile.exists()) {
                dirFile.mkdir()
            }

            val out = FileOutputStream(name)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            return File(name)
        } catch (ignored: Exception) {
        }

        return null
    }
}
