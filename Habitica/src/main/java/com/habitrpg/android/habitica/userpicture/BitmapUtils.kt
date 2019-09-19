package com.habitrpg.android.habitica.userpicture

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object BitmapUtils {
    fun saveToShareableFile(directory: String, filename: String, bmp: Bitmap): File? {
        var filename = filename
        try {
            filename = "$directory/$filename"

            val out = FileOutputStream(filename)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            return File(filename)
        } catch (ignored: Exception) {
        }

        return null
    }
}
