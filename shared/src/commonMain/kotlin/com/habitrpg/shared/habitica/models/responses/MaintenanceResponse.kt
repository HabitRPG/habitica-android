package com.habitrpg.shared.habitica.models.responses

import kotlinx.serialization.Serializable

@Serializable
class MaintenanceResponse {
    var activeMaintenance: Boolean? = null
    var minBuild: Int? = null
    var title: String? = null
    var imageUrl: String? = null
    var description: String? = null
}
