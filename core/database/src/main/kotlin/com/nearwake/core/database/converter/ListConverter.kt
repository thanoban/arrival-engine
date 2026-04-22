package com.nearwake.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class ListConverter {
    private val json = Json
    private val serializer = ListSerializer(String.serializer())

    @TypeConverter
    fun fromStringList(value: List<String>): String =
        json.encodeToString(serializer, value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        json.decodeFromString(serializer, value)
}
