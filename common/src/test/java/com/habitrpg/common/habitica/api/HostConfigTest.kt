package com.habitrpg.common.habitica.api

import android.content.SharedPreferences
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class HostConfigTest :
    WordSpec({
        "loadAPIKey" should {
            "keep the plain token if it can't be encrypted" {
                val editor = mockk<SharedPreferences.Editor>(relaxed = true)
                val prefs =
                    mockk<SharedPreferences> {
                        every { contains("user") } returns false
                        every { getString("APIToken", null) } returns "token"
                        every { edit() } returns editor
                    }
                HostConfig("user", "").loadAPIKey(prefs, null) shouldBe "token"
                verify(exactly = 0) { editor.remove("APIToken") }
            }
        }
    })
