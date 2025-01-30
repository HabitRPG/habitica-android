package com.habitrpg.buildlogic.model

import com.google.gson.annotations.SerializedName

data class HabiticaFlavor(
    @SerializedName("dimension") val dimension: String,
    @SerializedName("flavors") val flavors: List<Flavor>
)
