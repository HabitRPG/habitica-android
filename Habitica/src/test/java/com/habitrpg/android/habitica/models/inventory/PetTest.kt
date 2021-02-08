package com.habitrpg.android.habitica.models.inventory

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.getTranslatedType
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

private const val FAKE_STANDARD = "Standard"
private const val FAKE_PREMIUM = "premium"

class PetTest {
    @Mock
    private var mockContext: Context = mock(Context::class.java)
    private var pet: Pet = Pet()

    @Test
    fun testGetTranslatedStringReturnsStandard() {
        pet.type = "drop"
        `when`(mockContext.getString(R.string.standard)).thenReturn(FAKE_STANDARD)

        val result = pet.getTranslatedType(mockContext)

        assertThat(result).isEqualTo(FAKE_STANDARD)
    }

    @Test
    fun testGetTranslatedStringReturnsPremiumWhenContextIsNull() {
        pet.type = "premium"

        val result = pet.getTranslatedType(null)

        assertThat(result).isEqualTo(FAKE_PREMIUM)
    }
}