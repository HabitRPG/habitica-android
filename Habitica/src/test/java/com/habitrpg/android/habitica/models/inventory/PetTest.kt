package com.habitrpg.android.habitica.models.inventory

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.getTranslatedType
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

private const val FAKE_STANDARD = "Standard"
private const val FAKE_PREMIUM = "premium"

class PetTest : WordSpec({
    val pet: Pet = Pet()
    lateinit var mockContext: Context
    beforeEach {
        mockContext = mockk()
    }
    "getTranslatedType" should {
        "return standard" {
            pet.type = "drop"
            every { mockContext.getString(R.string.standard) } returns FAKE_STANDARD

            val result = pet.getTranslatedType(mockContext)
            result shouldBe FAKE_STANDARD
        }

        "return premium without context" {
            pet.type = "premium"

            val result = pet.getTranslatedType(null)
            result shouldBe FAKE_PREMIUM
        }
    }
})
