package com.habitrpg.android.habitica.ui.fragments.skills

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentSkillsBinding
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.launchCatching
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
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SkillsFragment : BaseMainFragment<FragmentSkillsBinding>() {
    internal var adapter: SkillsRecyclerViewAdapter? = null
    private var selectedSkill: Skill? = null

    override var binding: FragmentSkillsBinding? = null

    @Inject
    lateinit var userViewModel: MainUserViewModel

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSkillsBinding {
        return FragmentSkillsBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adapter = SkillsRecyclerViewAdapter()
        adapter?.useSkillEvents?.subscribeWithErrorHandler { onSkillSelected(it) }?.let { compositeSubscription.add(it) }

        this.tutorialStepIdentifier = "skills"
        this.tutorialTexts = listOf(getString(R.string.tutorial_skills))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let { checkUserLoadSkills(it) }
        }
        binding?.recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
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
        Flowable.combineLatest(
            userRepository.getSkills(user),
            userRepository.getSpecialItems(user)
        ) { skills, items ->
            val allEntries = mutableListOf<Skill>()
            for (skill in skills) {
                allEntries.add(skill)
            }
            for (item in items) {
                allEntries.add(item)
            }
            return@combineLatest allEntries
        }.subscribe({ skills -> adapter?.setSkillList(skills) }, ExceptionHandler.rx())
    }

    private fun onSkillSelected(skill: Skill) {
        when {
            "special" == skill.habitClass -> {
                selectedSkill = skill
                val intent = Intent(activity, SkillMemberActivity::class.java)
                memberSelectionResult.launch(intent)
            }
            skill.target == "task" -> {
                selectedSkill = skill
                val intent = Intent(activity, SkillTasksActivity::class.java)
                taskSelectionResult.launch(intent)
            }
            else -> useSkill(skill)
        }
    }

    private fun displaySkillResult(usedSkill: Skill?, response: SkillResponse) {
        if (!isAdded) return
        adapter?.mana = response.user?.stats?.mp ?: 0.0
        val activity = activity ?: return
        if ("special" == usedSkill?.habitClass) {
            showSnackbar(activity.snackbarContainer, context?.getString(R.string.used_skill_without_mana, usedSkill.text), HabiticaSnackbar.SnackbarDisplayType.BLUE)
        } else {
            context?.let {
                showSnackbar(
                    activity.snackbarContainer, null,
                    context?.getString(R.string.used_skill_without_mana, usedSkill?.text),
                    BitmapDrawable(resources, HabiticaIconsHelper.imageOfMagic()),
                    ContextCompat.getColor(it, R.color.blue_10), "-" + usedSkill?.mana,
                    HabiticaSnackbar.SnackbarDisplayType.BLUE
                )
            }
        }
        if (response.damage > 0) {
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                delay(2000L)
                if (!isAdded) return@launch
                showSnackbar(
                    activity.snackbarContainer, null,
                    context?.getString(R.string.caused_damage),
                    BitmapDrawable(resources, HabiticaIconsHelper.imageOfDamage()),
                    ContextCompat.getColor(activity, R.color.green_10), "+%.01f".format(response.damage),
                    HabiticaSnackbar.SnackbarDisplayType.SUCCESS
                )
            }
        }
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(true)
        }
    }

    private val taskSelectionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            useSkill(selectedSkill, it.data?.getStringExtra("taskID"))
        }
    }

    private val memberSelectionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            useSkill(selectedSkill, it.data?.getStringExtra("member_id"))
        }
    }

    private fun useSkill(skill: Skill?, taskId: String? = null) {
        if (skill == null) {
            return
        }
        lifecycleScope.launchCatching {
            val skillResponse = if (taskId != null) {
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
