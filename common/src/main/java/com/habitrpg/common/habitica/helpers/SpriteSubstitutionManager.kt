package com.habitrpg.common.habitica.helpers

class SpriteSubstitutionManager {
    companion object {
        private var substitutions: Map<String, Map<String, String>> = emptyMap()

        fun setSubstitutions(subs: Map<String, Map<String, String>>) {
            substitutions = subs
        }

        fun substitute(imageName: String, context: String? = null): String {
            val subs = if (context != null) substitutions[context] else substitutions["general"]
            if (subs != null) {
                subs[imageName]?.let {
                    return it
                }
                for (entry in subs) {
                    if (imageName.startsWith(entry.key)) {
                        return entry.value
                    }
                }
                if ((imageName.isEmpty() || imageName == "Pet-") && context == "pets") {
                    return subs["noPetAndroid"] ?: subs["noPet"] ?: imageName
                }
                return subs["default"] ?: imageName
            }
            return imageName
        }
    }
}