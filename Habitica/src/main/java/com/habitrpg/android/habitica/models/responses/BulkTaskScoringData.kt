package com.habitrpg.android.habitica.models.responses

import com.habitrpg.android.habitica.models.user.Buffs
import com.habitrpg.android.habitica.models.user.Training
import com.habitrpg.shared.habitica.models.responses.TaskDirectionData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class BulkTaskScoringData {
    @SerialName("con")
    var constitution: Int? = null

    @SerialName("str")
    var strength: Int? = null

    @SerialName("per")
    var per: Int? = null

    @SerialName("int")
    var intelligence: Int? = null
    var training: Training? = null
    var buffs: Buffs? = null
    var points: Int? = null
    var lvl: Int? = null

    @SerialName("class")
    var habitClass: String? = null
    var gp: Double? = null
    var exp: Double? = null
    var mp: Double? = null
    var hp: Double? = null

    var tasks: List<TaskDirectionData> = listOf()
}
