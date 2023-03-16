package com.habitrpg.android.habitica.ui.fragments.social.party

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentNoPartyBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.common.habitica.extensions.DataBindingUtils
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.helpers.setMarkdown
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoPartyFragmentFragment : BaseMainFragment<FragmentNoPartyBinding>() {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var configManager: AppConfigManager
    @Inject
    lateinit var userViewModel: MainUserViewModel

    override var binding: FragmentNoPartyBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNoPartyBinding {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.refreshLayout?.setOnRefreshListener { this.refresh() }

        binding?.invitationsView?.acceptCall = {
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                socialRepository.joinGroup(it)
                userRepository.retrieveUser(false)
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
        }

        binding?.usernameTextview?.setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(context?.getString(R.string.username), userViewModel.username)
            clipboard?.setPrimaryClip(clip)
            val activity = mainActivity
            if (activity != null && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                HabiticaSnackbar.showSnackbar(activity.snackbarContainer, getString(R.string.username_copied), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
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
                val bitmapDrawable = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(bitmap, width, height, false))
                bitmapDrawable.tileModeX = Shader.TileMode.REPEAT
                if (binding?.noPartyBackground != null) {
                    binding?.noPartyBackground?.background = bitmapDrawable
                }
            }
        }

        if (configManager.noPartyLinkPartyGuild()) {
            binding?.joinPartyDescriptionTextview?.setMarkdown(getString(R.string.join_party_description_guild, "[Party Wanted Guild](https://habitica.com/groups/guild/f2db2a7f-13c5-454d-b3ee-ea1f5089e601)"))
            binding?.joinPartyDescriptionTextview?.setOnClickListener {
                context?.let { FirebaseAnalytics.getInstance(it).logEvent("clicked_party_wanted", null) }
                MainNavigationController.navigate(R.id.guildFragment, bundleOf("groupID" to "f2db2a7f-13c5-454d-b3ee-ea1f5089e601"))
            }
        }

        binding?.usernameTextview?.text = userViewModel.formattedUsername
    }

    private val groupFormResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val bundle = it.data?.extras
            if (bundle?.getString("groupType") == "party") {
                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                    val group = socialRepository.createGroup(
                        bundle.getString("name"),
                        bundle.getString("description"),
                        bundle.getString("leader"),
                        "party",
                        bundle.getString("privacy"),
                        bundle.getBoolean("leaderCreateChallenge")
                    )
                    userRepository.retrieveUser(false)
                    if (isAdded) {
                        parentFragmentManager.popBackStack()
                    }
                    MainNavigationController.navigate(
                        R.id.partyFragment,
                        bundleOf(Pair("partyID", userViewModel.partyID))
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
        userRepository.close()
        socialRepository.close()
        super.onDestroy()
    }


    companion object {

        fun newInstance(): NoPartyFragmentFragment {
            val args = Bundle()

            val fragment = NoPartyFragmentFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
