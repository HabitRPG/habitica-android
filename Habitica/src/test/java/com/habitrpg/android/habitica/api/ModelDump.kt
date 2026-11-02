package com.habitrpg.android.habitica.api

import java.lang.reflect.Modifier
import java.util.Date
import java.util.IdentityHashMap

/**
 * Flattens a parsed model graph into sorted "path = value" lines, so two parsers can be
 * compared field by field without either of them being involved in the comparison.
 */
object ModelDump {
    fun dump(value: Any?): String {
        val lines = mutableListOf<String>()
        walk("", value, lines, IdentityHashMap())
        return lines.joinToString("\n")
    }

    private fun walk(
        path: String,
        value: Any?,
        lines: MutableList<String>,
        visiting: IdentityHashMap<Any, Unit>,
    ) {
        when (value) {
            null -> lines += "$path = null"
            is String -> lines += "$path = \"${value.replace("\n", "\\n")}\""
            is Number, is Boolean, is Char, is Enum<*> -> lines += "$path = $value"
            is Date -> lines += "$path = date(${value.time})"
            is Collection<*> -> {
                if (value.isEmpty()) lines += "$path = []"
                value.forEachIndexed { index, item -> walk("$path[$index]", item, lines, visiting) }
            }
            is Map<*, *> -> {
                if (value.isEmpty()) lines += "$path = {}"
                value.entries
                    .sortedBy { it.key.toString() }
                    .forEach { walk("$path{${it.key}}", it.value, lines, visiting) }
            }
            else -> {
                if (value.javaClass.name.startsWith("java.") || value.javaClass.name.startsWith("kotlin.")) {
                    lines += "$path = $value"
                    return
                }
                if (visiting.containsKey(value)) {
                    lines += "$path = <cycle>"
                    return
                }
                visiting[value] = Unit
                val fields = fieldsOf(value.javaClass)
                if (fields.isEmpty()) lines += "$path = ${value.javaClass.simpleName}()"
                for (field in fields) {
                    walk(if (path.isEmpty()) field.name else "$path.${field.name}", field.get(value), lines, visiting)
                }
                visiting.remove(value)
            }
        }
    }

    private fun fieldsOf(type: Class<*>): List<java.lang.reflect.Field> =
        generateSequence(type) { it.superclass }
            .takeWhile { it != Any::class.java && it.name != "io.realm.RealmObject" }
            .flatMap { it.declaredFields.asSequence() }
            .filterNot {
                Modifier.isStatic(it.modifiers) ||
                    it.isSynthetic ||
                    it.name.endsWith("\$delegate") ||
                    it.type.name.startsWith("android.") ||
                    Function::class.java.isAssignableFrom(it.type)
            }.onEach { it.isAccessible = true }
            .sortedBy { it.name }
            .toList()
}
