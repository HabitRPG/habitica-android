package com.habitrpg.android.habitica.ui.activities

import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityAdventureGuideBinding
import com.habitrpg.android.habitica.databinding.AdventureGuideItemBinding
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.extensions.fromHtml
import com.habitrpg.common.habitica.extensions.loadImage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdventureGuideActivity : BaseActivity() {
    private lateinit var binding: ActivityAdventureGuideBinding

    @Inject
    lateinit var userViewModel: MainUserViewModel

    private lateinit var achievementTitles: Map<String, String>
    private lateinit var achievementDescriptions: Map<String, String>

    override fun getLayoutResId() = R.layout.activity_main

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityAdventureGuideBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(binding.toolbar)

        achievementTitles =
            mapOf(
                Pair("createdTask", getString(R.string.create_task_title)),
                Pair("completedTask", getString(R.string.complete_task_title)),
                Pair("hatchedPet", getString(R.string.hatch_pet_title)),
                Pair("fedPet", getString(R.string.feedPet_title)),
                Pair("purchasedEquipment", getString(R.string.purchase_equipment_title)),
            )
        achievementDescriptions =
            mapOf(
                Pair("createdTask", getString(R.string.create_task_description)),
                Pair("completedTask", getString(R.string.complete_task_description)),
                Pair("hatchedPet", getString(R.string.hatch_pet_description)),
                Pair("fedPet", getString(R.string.feedPet_description)),
                Pair("purchasedEquipment", getString(R.string.purchase_equipment_description)),
            )

        val descriptionText = getString(R.string.adventure_guide_description_new)
        binding.descriptionView.setText(descriptionText.fromHtml(), TextView.BufferType.SPANNABLE)

        Analytics.sendNavigationEvent("adventure guide screen")

        userViewModel.user.observe(this) {
            it?.let { updateUser(it) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun updateUser(user: User) {
        val achievements = user.onboardingAchievements
        val completed = achievements.count { it.earned }
        binding.progressBar.max = achievements.size
        binding.progressBar.progress = completed
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            binding.progressBar.progressBackgroundTintMode = PorterDuff.Mode.SRC_OVER
        }

        if (completed > 0) binding.progressTextview.text =
            getString(
                R.string.percent_completed,
                ((completed / achievements.size.toFloat()) * 100).toInt(),
            )


        binding.achievementContainer.removeAllViews()
        for (achievement in achievements) {
            val itemBinding =
                AdventureGuideItemBinding.inflate(
                    layoutInflater,
                    binding.achievementContainer,
                    true,
                )
            itemBinding.titleView.text = achievementTitles[achievement.key]
            itemBinding.descriptionView.text = achievementDescriptions[achievement.key]

            val iconName = if (achievement.earned)
                "achievement-" + achievement.key + "2x"
            else "achievement-unearned2x"

            itemBinding.iconView.loadImage(iconName)
            if (achievement.earned) {
                itemBinding.titleView.paintFlags =
                    itemBinding.titleView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                itemBinding.titleView.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.text_ternary,
                    ),
                )
                itemBinding.descriptionView.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.text_ternary,
                    ),
                )
            } else {
                itemBinding.titleView.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.text_primary,
                    ),
                )
                itemBinding.descriptionView.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.text_primary,
                    ),
                )
                itemBinding.iconView.alpha = 0.5f
            }
        }
    }
}
