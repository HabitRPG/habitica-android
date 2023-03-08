package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivitySkillMembersBinding
import com.habitrpg.android.habitica.ui.adapter.social.PartyMemberRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
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

    override fun getContentView(layoutResId: Int?): View {
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
        viewAdapter?.onUserClicked = {
            lifecycleScope.launchCatching {
                val resultIntent = Intent()
                resultIntent.putExtra("member_id", it)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
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
