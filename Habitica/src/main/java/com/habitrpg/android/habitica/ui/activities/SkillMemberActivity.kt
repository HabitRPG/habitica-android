package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.adapter.social.PartyMemberRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.functions.Consumer
import javax.inject.Inject

class SkillMemberActivity : BaseActivity() {
    private val recyclerView: RecyclerView by bindView(R.id.recyclerView)

    private var viewAdapter: PartyMemberRecyclerViewAdapter? = null

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository

    override fun getLayoutResId(): Int {
        return R.layout.activity_skill_members
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMemberList()
    }

    private fun loadMemberList() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        viewAdapter = PartyMemberRecyclerViewAdapter(null, true)
        viewAdapter?.getUserClickedEvents()?.subscribe(Consumer { userId ->
            val resultIntent = Intent()
            resultIntent.putExtra("member_id", userId)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }, RxErrorHandler.handleEmptyError())
        recyclerView.adapter = viewAdapter

        userRepository.getUser()
                .firstElement()
                .flatMap { user -> socialRepository.getGroupMembers(user.party?.id ?: "").firstElement() }
                .subscribe(Consumer { viewAdapter?.updateData(it) }, RxErrorHandler.handleEmptyError())
    }
}
