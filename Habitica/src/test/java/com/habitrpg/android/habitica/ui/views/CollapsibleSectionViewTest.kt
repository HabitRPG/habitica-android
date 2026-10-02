package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.testing.HiltTestActivity
import com.habitrpg.android.habitica.testing.withHiltActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class CollapsibleSectionViewTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val preferences by lazy {
        ApplicationProvider
            .getApplicationContext<Context>()
            .getSharedPreferences("collapsible_sections", Context.MODE_PRIVATE)
    }

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun HiltTestActivity.attachSection(identifier: String? = "stats"): Pair<CollapsibleSectionView, List<View>> {
        val attributes =
            Robolectric
                .buildAttributeSet()
                .addAttribute(R.attr.title, "Stats")
                .apply { if (identifier != null) addAttribute(R.attr.identifier, identifier) }
                .build()
        val section = CollapsibleSectionView(this, attributes)
        val rows = listOf(TextView(this), TextView(this))
        rows.forEach { section.addView(it) }
        setContentView(section, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        return section to rows
    }

    private val CollapsibleSectionView.header: View
        get() = findViewById(R.id.section_title_view)

    @Test
    fun showsTheTitleFromTheLayout() =
        withHiltActivity { activity ->
            val (section, _) = activity.attachSection()
            section.title.toString() shouldBe "Stats"
        }

    @Test
    fun startsExpanded() =
        withHiltActivity { activity ->
            val (_, rows) = activity.attachSection()
            rows.forEach { it.visibility shouldBe View.VISIBLE }
        }

    @Test
    fun collapsesAndExpandsWhenTheHeaderIsTapped() =
        withHiltActivity { activity ->
            val (section, rows) = activity.attachSection()

            section.header.performClick()
            rows.forEach { it.visibility shouldBe View.GONE }
            section.header.visibility shouldBe View.VISIBLE

            section.header.performClick()
            rows.forEach { it.visibility shouldBe View.VISIBLE }
        }

    @Test
    fun remembersTheCollapsedState() =
        withHiltActivity { activity ->
            val (section, _) = activity.attachSection()
            section.header.performClick()
            preferences.getBoolean("stats", false) shouldBe true

            val (_, rows) = activity.attachSection()
            rows.forEach { it.visibility shouldBe View.GONE }
        }

    @Test
    fun remembersWhenItWasExpandedAgain() =
        withHiltActivity { activity ->
            val (section, _) = activity.attachSection()
            section.header.performClick()
            section.header.performClick()
            preferences.getBoolean("stats", true) shouldBe false
        }

    @Test
    fun keepsSectionsWithDifferentIdentifiersApart() =
        withHiltActivity { activity ->
            val (section, _) = activity.attachSection("stats")
            section.header.performClick()

            val (_, rows) = activity.attachSection("equipment")
            rows.forEach { it.visibility shouldBe View.VISIBLE }
        }

    @Test
    fun doesNotPersistSectionsWithoutIdentifier() =
        withHiltActivity { activity ->
            val (section, _) = activity.attachSection(identifier = null)
            section.header.performClick()
            preferences.all.isEmpty() shouldBe true
        }
}
