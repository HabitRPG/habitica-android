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

class MountTest {
    @Mock
    private var mockContext: Context = mock(Context::class.java)
    private var mount: Mount = Mount()

    @Test
    fun testGetTranslatedStringReturnsStandard() {
        mount.type = "drop"
        `when`(mockContext.getString(R.string.standard)).thenReturn(FAKE_STANDARD)

        val result = mount.getTranslatedType(mockContext)

        assertThat(result).isEqualTo(FAKE_STANDARD)
    }

    @Test
    fun testGetTranslatedStringReturnsPremiumWhenContextIsNull() {
        mount.type = "premium"

        val result = mount.getTranslatedType(null)

        assertThat(result).isEqualTo(FAKE_PREMIUM)
    }
}