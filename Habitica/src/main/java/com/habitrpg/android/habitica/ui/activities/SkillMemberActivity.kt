package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivitySkillMembersBinding
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.ui.adapter.social.PartyMemberRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class SkillMemberActivity : BaseActivity() {
    private lateinit var binding: ActivitySkillMembersBinding
    private var viewAdapter: PartyMemberRecyclerViewAdapter? = null

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userViewModel: MainUserViewModel

    override fun getLayoutResId(): Int {
        return R.layout.activity_skill_members
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun getContentView(): View {
        binding = ActivitySkillMembersBinding.inflate(layoutInflater)
        return binding.root
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar(findViewById(R.id.toolbar))
        loadMemberList()
    }

    private fun loadMemberList() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        viewAdapter = PartyMemberRecyclerViewAdapter()
        viewAdapter?.getUserClickedEvents()?.subscribe(
            { userId ->
                val resultIntent = Intent()
                resultIntent.putExtra("member_id", userId)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            },
            ExceptionHandler.rx()
        )?.let { compositeSubscription.add(it) }
        binding.recyclerView.adapter = viewAdapter

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.getUser()
                .map { it?.party?.id }
                .filterNotNull()
                .flatMapLatest { socialRepository.getPartyMembers(it) }
                .collect { viewAdapter?.data = it }
        }
    }
}
