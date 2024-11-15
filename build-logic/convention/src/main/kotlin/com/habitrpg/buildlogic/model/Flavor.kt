package com.habitrpg.buildlogic.model

import com.google.gson.annotations.SerializedName

data class Flavor(
    @SerializedName("testingLevel") val testingLevel: String?,
    @SerializedName("appName") val appName: String?,
    @SerializedName("name") val name: String,
    @SerializedName("versionCodeIncrement") val versionCodeIncrement: Int?
)
