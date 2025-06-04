package com.habitrpg.android.habitica.ui.fragments.skills

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.SkillMemberActivity
import com.habitrpg.android.habitica.ui.activities.SkillTasksActivity
import com.habitrpg.android.habitica.ui.adapter.SkillsRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.Companion.showSnackbar
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.graphics.drawable.toDrawable
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.views.PixelArtView

@AndroidEntryPoint
class SkillsFragment : BaseMainFragment<FragmentRecyclerviewBinding>() {
    internal var adapter: SkillsRecyclerViewAdapter? = null
    private var selectedSkill: Skill? = null

    override var binding: FragmentRecyclerviewBinding? = null

    @Inject
    lateinit var userViewModel: MainUserViewModel

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRecyclerviewBinding {
        return FragmentRecyclerviewBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adapter = SkillsRecyclerViewAdapter()
        adapter?.onUseSkill = { onSkillSelected(it) }

        this.tutorialStepIdentifier = "skills"
        this.tutorialTexts = listOf(getString(R.string.tutorial_skills))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let { checkUserLoadSkills(it) }
        }
        binding?.recyclerView?.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(mainActivity)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
    }

    private fun checkUserLoadSkills(user: User) {
        if (adapter == null) {
            return
        }
        adapter?.mana = user.stats?.mp ?: 0.0
        adapter?.level = user.stats?.lvl ?: 0
        adapter?.specialItems = user.items?.special
        lifecycleScope.launchCatching {
            userRepository.getSkills(user)
                .combine(userRepository.getSpecialItems(user)) { skills, items ->
                    val allEntries = mutableListOf<Skill>()
                    for (skill in skills) {
                        allEntries.add(skill)
                    }
                    for (item in items) {
                        allEntries.add(item)
                    }
                    return@combine allEntries
                }.collect { skills -> adapter?.setSkillList(skills) }
        }
    }

    private fun onSkillSelected(skill: Skill) {
        val context = context ?: return
        val resourceIconDrawable: Drawable = HabiticaIconsHelper.imageOfMagic().toDrawable(context.resources)
        val skillIdentifier = "shop_"

        val bottomSheet = SkillDialogBottomSheetFragment.newInstance(
            skillTitle = skill.text,
            skillDescription = skill.notes ?: "",
            skillKey = skill.key,
            skillPath = skillIdentifier,
            skillMpCost = "${skill.mana?.toInt() ?: 0} MP",
            resourceIcon = resourceIconDrawable,
            onUseSkill = {
                when {
                    "special" == skill.habitClass -> {
                        selectedSkill = skill
                        val intent = Intent(mainActivity, SkillMemberActivity::class.java)
                        memberSelectionResult.launch(intent)
                    }

                    skill.target == "task" -> {
                        selectedSkill = skill
                        val intent = Intent(mainActivity, SkillTasksActivity::class.java)
                        taskSelectionResult.launch(intent)
                    }

                    else -> useSkill(skill)
                }
            }
        )
        bottomSheet.show(childFragmentManager, "SkillDialogBottomSheet")


    }

    private fun displaySkillResult(
        usedSkill: Skill?,
        response: SkillResponse
    ) {
        if (!isAdded) return
        adapter?.mana = response.user?.stats?.mp ?: 0.0
        val activity = mainActivity ?: return
        if ("special" == usedSkill?.habitClass) {
            showSnackbar(
                activity.snackbarContainer,
                context?.getString(R.string.used_skill_without_mana, usedSkill.text),
                HabiticaSnackbar.SnackbarDisplayType.BLUE
            )
        } else {
            context?.let {
                showSnackbar(
                    activity.snackbarContainer,
                    null,
                    context?.getString(R.string.used_skill_without_mana, usedSkill?.text),
                    BitmapDrawable(resources, HabiticaIconsHelper.imageOfMagic()),
                    ContextCompat.getColor(it, R.color.blue_10),
                    "-" + usedSkill?.mana,
                    HabiticaSnackbar.SnackbarDisplayType.BLUE
                )
            }
        }
        if (response.damage > 0) {
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                delay(2000L)
                if (!isAdded) return@launch
                showSnackbar(
                    activity.snackbarContainer,
                    null,
                    context?.getString(R.string.caused_damage),
                    BitmapDrawable(resources, HabiticaIconsHelper.imageOfDamage()),
                    ContextCompat.getColor(activity, R.color.green_10),
                    "+%.01f".format(response.damage),
                    HabiticaSnackbar.SnackbarDisplayType.SUCCESS
                )
            }
        }
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(true)
        }
    }

    private val taskSelectionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                useSkill(selectedSkill, it.data?.getStringExtra("taskID"))
            }
        }

    private val memberSelectionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                useSkill(selectedSkill, it.data?.getStringExtra("member_id"))
            }
        }

    private fun useSkill(
        skill: Skill?,
        taskId: String? = null
    ) {
        if (skill == null) {
            return
        }
        lifecycleScope.launchCatching {
            val skillResponse =
                if (taskId != null) {
                    userRepository.useSkill(skill.key, skill.target, taskId)
                } else {
                    userRepository.useSkill(skill.key, skill.target)
                }
            if (skillResponse != null) {
                displaySkillResult(skill, skillResponse)
            }
        }
    }
}
