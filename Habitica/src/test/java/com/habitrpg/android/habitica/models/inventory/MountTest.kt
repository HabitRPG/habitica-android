package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.BaseAnnotationTestCase
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.getTranslatedType
import io.kotest.matchers.shouldBe
import io.mockk.every

private const val FAKE_STANDARD = "Standard"
private const val FAKE_PREMIUM = "premium"

class MountTest : BaseAnnotationTestCase() {
    private var mount: Mount = Mount()

    @Test
    fun testGetTranslatedStringReturnsStandard() {
        mount.type = "drop"
        every { mockContext.getString(R.string.standard) } returns FAKE_STANDARD

        val result = mount.getTranslatedType(mockContext)
        result shouldBe FAKE_STANDARD
    }

    @Test
    fun testGetTranslatedStringReturnsPremiumWhenContextIsNull() {
        mount.type = "premium"

        val result = mount.getTranslatedType(null)
        result shouldBe FAKE_PREMIUM
    }
}
