package com.habitrpg.android.habitica.ui.fragments.social.party

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentNoPartyBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.extensions.DataBindingUtils
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class NoPartyFragmentFragment : BaseMainFragment<FragmentNoPartyBinding>() {
    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var configManager: AppConfigManager

    @Inject
    lateinit var userViewModel: MainUserViewModel

    override var binding: FragmentNoPartyBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNoPartyBinding {
        return FragmentNoPartyBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding?.refreshLayout?.setOnRefreshListener { this.refresh() }
        refresh()

        binding?.invitationsView?.acceptCall = {
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                socialRepository.joinGroup(it)
                userRepository.retrieveUser(false, true)
                parentFragmentManager.popBackStack()
                MainNavigationController.navigate(
                    R.id.partyFragment,
                    bundleOf(Pair("partyID", userViewModel.partyID))
                )
            }
        }

        binding?.invitationsView?.rejectCall = {
            lifecycleScope.launchCatching {
                socialRepository.rejectGroupInvite(it)
            }
            binding?.invitationWrapper?.visibility = View.GONE
        }

        binding?.invitationsView?.getLeader = { leaderID ->
            socialRepository.retrieveMember(leaderID)
        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            val partyInvitations = user?.invitations?.parties ?: emptyList()
            if (partyInvitations.isNotEmpty()) {
                binding?.invitationWrapper?.visibility = View.VISIBLE
                binding?.invitationsView?.setInvitations(partyInvitations)
            } else {
                binding?.invitationWrapper?.visibility = View.GONE
            }

            val isSeeking = user?.party?.seeking != null
            binding?.seekPartyButton?.isVisible = !isSeeking
            binding?.seekingPartyWrapper?.isVisible = isSeeking
        }

        binding?.seekPartyButton?.setOnClickListener {
            lifecycleScope.launchCatching {
                userRepository.updateUser("party.seeking", Date())
            }
        }

        binding?.leaveSeekingButton?.setOnClickListener {
            lifecycleScope.launchCatching {
                userRepository.updateUser("party.seeking", null)
            }
        }

        binding?.createPartyButton?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("groupType", "party")
            bundle.putString("leader", userViewModel.userID)
            val intent = Intent(mainActivity, GroupFormActivity::class.java)
            intent.putExtras(bundle)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            groupFormResult.launch(intent)
        }

        context?.let { context ->
            DataBindingUtils.loadImage(context, "timeTravelersShop_background_fall") { drawable ->
                val bitmap = drawable.toBitmap()
                val aspectRatio = bitmap.width / bitmap.height.toFloat()
                val height = context.resources.getDimension(R.dimen.shop_height).toInt()
                val width = (height * aspectRatio).roundToInt()
                val bitmapDrawable =
                    BitmapDrawable(
                        context.resources,
                        Bitmap.createScaledBitmap(bitmap, width, height, false)
                    )
                bitmapDrawable.tileModeX = Shader.TileMode.REPEAT
                if (binding?.noPartyBackground != null) {
                    binding?.noPartyBackground?.background = bitmapDrawable
                }
            }
        }
    }

    private val groupFormResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val bundle = it.data?.extras
                if (bundle?.getString("groupType") == "party") {
                    lifecycleScope.launch(ExceptionHandler.coroutine()) {
                        val group =
                            socialRepository.createGroup(
                                bundle.getString("name"),
                                bundle.getString("description"),
                                bundle.getString("leader"),
                                "party",
                                bundle.getString("privacy"),
                                bundle.getBoolean("leaderCreateChallenge")
                            )
                        userRepository.retrieveUser(false, true)
                        if (isAdded) {
                            parentFragmentManager.popBackStack()
                        }
                        MainNavigationController.navigate(
                            R.id.partyFragment,
                            bundleOf(Pair("partyID", group?.id))
                        )
                    }
                }
            }
        }

    private fun refresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            val user = userRepository.retrieveUser(false, true)
            binding?.refreshLayout?.isRefreshing = false
            if (user?.hasParty == true) {
                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                    val group = socialRepository.retrieveGroup("party")
                    socialRepository.retrievePartyMembers(group?.id ?: "", true)
                }
            }
        }
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }
}
