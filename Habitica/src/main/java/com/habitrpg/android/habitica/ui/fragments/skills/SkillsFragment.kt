package com.habitrpg.android.habitica.ui.fragments.skills

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentSkillsBinding
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.responses.SkillResponse
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.SkillMemberActivity
import com.habitrpg.android.habitica.ui.activities.SkillTasksActivity
import com.habitrpg.android.habitica.ui.adapter.SkillsRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.Companion.showSnackbar
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable

class SkillsFragment : BaseMainFragment<FragmentSkillsBinding>() {

    private val TASK_SELECTION_ACTIVITY = 10
    private val MEMBER_SELECTION_ACTIVITY = 11

    internal var adapter: SkillsRecyclerViewAdapter? = null
    private var selectedSkill: Skill? = null


    override var binding: FragmentSkillsBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSkillsBinding {
        return FragmentSkillsBinding.inflate(inflater, container, false)
    }

    override var user: User? = null
        set(value) {
            field = value
            checkUserLoadSkills()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        adapter = SkillsRecyclerViewAdapter()
        adapter?.useSkillEvents?.subscribeWithErrorHandler { onSkillSelected(it) }?.let { compositeSubscription.add(it) }
        checkUserLoadSkills()

        this.tutorialStepIdentifier = "skills"
        this.tutorialText = getString(R.string.tutorial_skills)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
    }

    private fun checkUserLoadSkills() {
        if (user == null || adapter == null) {
            return
        }
        adapter?.mana = this.user?.stats?.mp ?: 0.0
        adapter?.level = this.user?.stats?.lvl ?: 0
        adapter?.specialItems = this.user?.items?.special
        user?.let { user ->
            Flowable.combineLatest(userRepository.getSkills(user),
                    userRepository.getSpecialItems(user), { skills, items ->
                val allEntries = mutableListOf<Skill>()
                for (skill in skills) {
                    allEntries.add(skill)
                }
                for (item in items) {
                    allEntries.add(item)
                }
                return@combineLatest allEntries
            })
                    .subscribe({ skills -> adapter?.setSkillList(skills) }, RxErrorHandler.handleEmptyError())
        }
    }

    private fun onSkillSelected(skill: Skill) {
        when {
            "special" == skill.habitClass -> {
                selectedSkill = skill
                val intent = Intent(activity, SkillMemberActivity::class.java)
                startActivityForResult(intent, MEMBER_SELECTION_ACTIVITY)
            }
            skill.target == "task" -> {
                selectedSkill = skill
                val intent = Intent(activity, SkillTasksActivity::class.java)
                startActivityForResult(intent, TASK_SELECTION_ACTIVITY)
            }
            else -> useSkill(skill)
        }
    }

    private fun displaySkillResult(usedSkill: Skill?, response: SkillResponse) {
        adapter?.mana = response.user.stats?.mp ?: 0.0
        val activity = activity ?: return
        if ("special" == usedSkill?.habitClass) {
            showSnackbar(activity.snackbarContainer, context?.getString(R.string.used_skill_without_mana, usedSkill.text), HabiticaSnackbar.SnackbarDisplayType.BLUE)
        } else {
            context?.let {
                showSnackbar(activity.snackbarContainer, null,
                        context?.getString(R.string.used_skill_without_mana, usedSkill?.text),
                        BitmapDrawable(resources, HabiticaIconsHelper.imageOfMagic()),
                        ContextCompat.getColor(it, R.color.blue_10), "-" + usedSkill?.mana,
                        HabiticaSnackbar.SnackbarDisplayType.BLUE)
            }
        }
        compositeSubscription.add(userRepository.retrieveUser(false).subscribe({ }, RxErrorHandler.handleEmptyError()))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            when (requestCode) {
                TASK_SELECTION_ACTIVITY -> {
                    if (resultCode == Activity.RESULT_OK) {
                        useSkill(selectedSkill, data.getStringExtra("taskID"))
                    }
                }
                MEMBER_SELECTION_ACTIVITY -> {
                    if (resultCode == Activity.RESULT_OK) {
                        useSkill(selectedSkill, data.getStringExtra("member_id"))
                    }
                }
            }
        }
    }

    private fun useSkill(skill: Skill?, taskId: String? = null) {
        if (skill == null) {
            return
        }
        val observable: Flowable<SkillResponse> = if (taskId != null) {
            userRepository.useSkill(skill.key, skill.target, taskId)
        } else {
            userRepository.useSkill(skill.key, skill.target)
        }
        compositeSubscription.add(observable.subscribe({ skillResponse -> this.displaySkillResult(skill, skillResponse) },
                RxErrorHandler.handleEmptyError()))
    }
}
