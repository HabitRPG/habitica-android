package com.habitrpg.android.habitica.ui.activities

import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.ActivityAdventureGuideBinding
import com.habitrpg.android.habitica.databinding.AdventureGuideItemBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import io.reactivex.functions.Consumer
import javax.inject.Inject


class AdventureGuideActivity : BaseActivity() {
    private lateinit var binding: ActivityAdventureGuideBinding


    private lateinit var achievementTitles: Map<String, String>
    private lateinit var achievementDescriptions: Map<String, String>

    @Inject
    internal lateinit var userRepository: UserRepository

    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }

    override fun getContentView(): View {
        binding = ActivityAdventureGuideBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(binding.toolbar)

        achievementTitles = mapOf(
                Pair("createdTask", getString(R.string.create_task_title)),
                Pair("completedTask", getString(R.string.complete_task_title)),
                Pair("hatchedPet", getString(R.string.hatch_pet_title)),
                Pair("fedPet", getString(R.string.feedPet_title)),
                Pair("purchasedEquipment", getString(R.string.purchase_equipment_title))
        )
        achievementDescriptions = mapOf(
                Pair("createdTask", getString(R.string.create_task_description)),
                Pair("completedTask", getString(R.string.complete_task_description)),
                Pair("hatchedPet", getString(R.string.hatch_pet_description)),
                Pair("fedPet", getString(R.string.feedPet_description)),
                Pair("purchasedEquipment", getString(R.string.purchase_equipment_description))
        )

        val descriptionText = getString(R.string.adventure_guide_description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.descriptionView.setText(Html.fromHtml(descriptionText,  Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
        } else {
            binding.descriptionView.setText(Html.fromHtml(descriptionText), TextView.BufferType.SPANNABLE)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        compositeSubscription.add(userRepository.getUser().subscribe(Consumer {
            updateUser(it)
        }, RxErrorHandler.handleEmptyError()))
    }

    private fun updateUser(user: User) {
        val achievements = user.onboardingAchievements
        val completed = achievements.count { it.earned }
        binding.progressBar.max = achievements.size
        binding.progressBar.progress = completed

        if (completed > 0) {
            binding.progressTextview.text = getString(R.string.percent_completed, ((completed / achievements.size.toFloat()) * 100).toInt())
            binding.progressTextview.setTextColor(ContextCompat.getColor(this, R.color.yellow_5))
        }

        binding.achievementContainer.removeAllViews()
        for (achievement in achievements) {
            val itemBinding = AdventureGuideItemBinding.inflate(layoutInflater, binding.achievementContainer, true)
            itemBinding.titleView.text = achievementTitles[achievement.key]
            itemBinding.descriptionView.text = achievementDescriptions[achievement.key]

            val iconName = if (achievement.earned) {
                "achievement-" + achievement.key + "2x"
            } else {
                "achievement-unearned2x"
            }
            DataBindingUtils.loadImage(itemBinding.iconView, iconName)
            if (achievement.earned) {
                itemBinding.titleView.paintFlags = itemBinding.titleView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                itemBinding.titleView.setTextColor(ContextCompat.getColor(this, R.color.gray_200))
                itemBinding.descriptionView.setTextColor(ContextCompat.getColor(this, R.color.gray_200))
            } else {
                itemBinding.titleView.setTextColor(ContextCompat.getColor(this, R.color.gray_50))
                itemBinding.descriptionView.setTextColor(ContextCompat.getColor(this, R.color.gray_50))
                itemBinding.iconView.alpha = 0.5f
            }
        }
    }

}