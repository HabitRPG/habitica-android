package com.habitrpg.android.habitica.ui.fragments.skills

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.events.commands.UseSkillCommand
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
import kotlinx.android.synthetic.main.fragment_skills.*
import org.greenrobot.eventbus.Subscribe
import rx.Observable
import rx.functions.Action1

class SkillsFragment : BaseMainFragment() {

    private val TASK_SELECTION_ACTIVITY = 10
    private val MEMBER_SELECTION_ACTIVITY = 11

    internal var adapter: SkillsRecyclerViewAdapter? = null
    private var selectedSkill: Skill? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        adapter = SkillsRecyclerViewAdapter()
        checkUserLoadSkills()

        this.tutorialStepIdentifier = "skills"
        this.tutorialText = getString(R.string.tutorial_skills)

        return inflater.inflate(R.layout.fragment_skills, container, false)
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.invalidateItemDecorations()
        recyclerView.addItemDecoration(DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = SafeDefaultItemAnimator()
    }

    private fun checkUserLoadSkills() {
        if (user == null || adapter == null) {
            return
        }

        adapter?.mana = this.user?.stats?.getMp()

        user?.let {
            Observable.concat<Skill>(userRepository.getSkills(it).first().flatMap( { Observable.from(it) }), userRepository.getSpecialItems(it).first().flatMap( { Observable.from(it) }))
                    .toList()
                    .subscribe(Action1 { skills -> adapter?.setSkillList(skills) }, RxErrorHandler.handleEmptyError())
        }
    }

    override fun setUser(user: User?) {
        super.setUser(user)

        checkUserLoadSkills()
    }

    @Subscribe
    fun onEvent(command: UseSkillCommand) {
        val skill = command.skill

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
        removeProgressDialog()
        adapter?.setMana(response.user.stats.mp)
        val activity = activity ?: return
        if ("special" == usedSkill?.habitClass) {
            showSnackbar(activity.getFloatingMenuWrapper(), context!!.getString(R.string.used_skill_without_mana, usedSkill.text), HabiticaSnackbar.SnackbarDisplayType.BLUE)
        } else {
            showSnackbar(activity.getFloatingMenuWrapper(), null,
                    context?.getString(R.string.used_skill_without_mana, usedSkill?.text),
                    BitmapDrawable(resources, HabiticaIconsHelper.imageOfMagic()),
                    ContextCompat.getColor(context!!, R.color.blue_10), "-" + usedSkill?.mana,
                    HabiticaSnackbar.SnackbarDisplayType.BLUE)
        }
        userRepository.retrieveUser(false).subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            when (requestCode) {
                TASK_SELECTION_ACTIVITY -> {
                    if (resultCode == Activity.RESULT_OK) {
                        useSkill(selectedSkill, data.getStringExtra("task_id"))
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
        displayProgressDialog()
        val observable: Observable<SkillResponse> = if (taskId != null) {
            userRepository.useSkill(user, skill.key, skill.target, taskId)
        } else {
            userRepository.useSkill(user, skill.key, skill.target)
        }
        observable.subscribe({ skillResponse -> this.displaySkillResult(skill, skillResponse) }) { throwable -> removeProgressDialog() }
    }

    private fun displayProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = ProgressDialog.show(activity, context?.getString(R.string.skill_progress_title), null, true)
    }

    private fun removeProgressDialog() {
        progressDialog?.dismiss()
    }

    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.sidebar_skills)
    }
}
