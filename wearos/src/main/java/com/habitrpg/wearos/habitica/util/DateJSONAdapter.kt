package com.habitrpg.wearos.habitica.util

import android.os.Build
import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.TaskType
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

var customDateAdapter: Any = object : Any() {

    @ToJson
    @Synchronized
    fun dateToJson(d: Date?): String? {
        return d?.let { dateFormats[0].format(it) }
    }

    @FromJson
    @Synchronized
    @Throws(ParseException::class)
    fun dateFromJson(s: String?): Date? {
        var date: Date? = null
        var index = 0
        while (index < dateFormats.size && date == null) {
            try {
                date = s?.let { dateFormats[index].parse(it) }
            } catch (_: ParseException) {}
            index += 1
        }
        return date
    }

    private var dateFormats = mutableListOf<DateFormat>()

    init {
        addFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        addFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        addFormat("E MMM dd yyyy HH:mm:ss zzzz")
        addFormat("yyyy-MM-dd'T'HH:mm:sszzz")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addFormat("yyyy-MM-dd'T'HH:mmX")
        } else {
            addFormat("yyyy-MM-dd'T'HH:mm")
        }
        addFormat("yyyy-MM-dd")
    }

    private fun addFormat(s: String) {
        val dateFormat = SimpleDateFormat(s, Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        dateFormats.add(dateFormat)
    }
}

class FrequencyAdapter {
    @ToJson
    fun toJson(type: Frequency): String = type.value

    @FromJson
    fun fromJson(value: String): Frequency? = Frequency.from(value)
}

class TaskTypeAdapter {
    @ToJson
    fun toJson(type: TaskType): String = type.value

    @FromJson
    fun fromJson(value: String): TaskType? = TaskType.from(value)
}

class AttributeAdapter {
    @ToJson
    fun toJson(type: Attribute): String = type.value

    @FromJson
    fun fromJson(value: String): Attribute? = Attribute.from(value)
}
