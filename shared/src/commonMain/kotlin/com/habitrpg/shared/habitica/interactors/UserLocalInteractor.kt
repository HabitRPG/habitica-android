package com.habitrpg.shared.habitica.interactors

import com.habitrpg.shared.habitica.models.user.User
import kotlin.random.Random

class UserLocalInteractor {
    companion object {
        fun revive(user: User): User {
            val stats = user.stats ?: return user

            // Health to max
            stats.hp = stats.maxHealth?.toDouble() ?: 50.0

            // Decrease level
            val userLvl = stats.lvl ?: 1
            stats.lvl = if (userLvl > 1) {
                userLvl - 1
            } else {
                1
            }

            stats.exp = 0.0

            // Decrease random stat
            val lostStatIndex = Random.nextInt(4)
            if (lostStatIndex == 0) {
                val str = stats.strength
                stats.strength = if (str != null) {
                    str - 1
                } else {
                    str
                }
            } else if (lostStatIndex == 1) {
                val int = stats.intelligence
                stats.intelligence = if (int != null) {
                    int - 1
                } else {
                    int
                }
            } else if (lostStatIndex == 2) {
                val per = stats.per
                stats.per = if (per != null) {
                    per - 1
                } else {
                    per
                }
            } else {
                val con = stats.constitution
                stats.constitution = if (con != null) {
                    con - 1
                } else {
                    con
                }
            }

            // Required to ensure that the modification to the user is saved
            user.versionNumber += 1
            return user
        }
    }
}