package com.habitrpg.android.habitica.ui.fragments.social

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentNoPartyBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.setMarkdown
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject
import kotlin.math.roundToInt


class NoPartyFragmentFragment : BaseMainFragment<FragmentNoPartyBinding>() {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var configManager: AppConfigManager

    override var binding: FragmentNoPartyBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNoPartyBinding {
        return FragmentNoPartyBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.refreshLayout?.setOnRefreshListener { this.refresh() }

        binding?.invitationsView?.acceptCall = {
            socialRepository.joinGroup(it)
                    .flatMap { userRepository.retrieveUser(false) }
                    .subscribe({
                        parentFragmentManager.popBackStack()
                        MainNavigationController.navigate(R.id.partyFragment,
                                bundleOf(Pair("partyID", user?.party?.id)))
                    }, RxErrorHandler.handleEmptyError())
        }

        binding?.invitationsView?.rejectCall = {
            socialRepository.rejectGroupInvite(it).subscribe({ }, RxErrorHandler.handleEmptyError())
            binding?.invitationWrapper?.visibility = View.GONE
        }

        binding?.invitationsView?.setLeader = { leader ->
            compositeSubscription.add(
                    socialRepository.getMember(leader)
                            .subscribe({
                                binding?.root?.findViewById<AvatarView>(R.id.groupleader_avatar_view)?.setAvatar(it)
                                binding?.root?.findViewById<TextView>(R.id.groupleader_avatar_view)?.text = getString(R.string.invitation_title,it.displayName, binding?.invitationsView?.groupName)
                            }, RxErrorHandler.handleEmptyError())
            )
        }

        binding?.usernameTextview?.setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(context?.getString(R.string.username), user?.username)
            clipboard?.setPrimaryClip(clip)
            val activity = activity
            if (activity != null) {
                HabiticaSnackbar.showSnackbar(activity.snackbarContainer, getString(R.string.username_copied), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
            }
        }

        binding?.createPartyButton?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("groupType", "party")
            bundle.putString("leader", user?.id)
            val intent = Intent(activity, GroupFormActivity::class.java)
            intent.putExtras(bundle)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityForResult(intent, GroupFormActivity.GROUP_FORM_ACTIVITY)
        }

        context?.let { context ->
            DataBindingUtils.loadImage("timeTravelersShop_background_fall") {bitmap ->
                val aspectRatio = bitmap.width / bitmap.height.toFloat()
                val height = context.resources.getDimension(R.dimen.shop_height).toInt()
                val width = (height * aspectRatio).roundToInt()
                val drawable = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(bitmap, width, height, false))
                drawable.tileModeX = Shader.TileMode.REPEAT
                Observable.just(drawable)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if (binding?.noPartyBackground != null) {
                                binding?.noPartyBackground?.background = it
                            }
                        }, RxErrorHandler.handleEmptyError())
            }
        }

        if (configManager.noPartyLinkPartyGuild()) {
            binding?.joinPartyDescriptionTextview?.setMarkdown(getString(R.string.join_party_description_guild, "[Party Wanted Guild](https://habitica.com/groups/guild/f2db2a7f-13c5-454d-b3ee-ea1f5089e601)"))
            binding?.joinPartyDescriptionTextview?.setOnClickListener {
                context?.let { FirebaseAnalytics.getInstance(it).logEvent("clicked_party_wanted", null) }
                MainNavigationController.navigate(R.id.guildFragment, bundleOf("groupID" to "f2db2a7f-13c5-454d-b3ee-ea1f5089e601"))
            }
        }

        if ((user?.invitations?.parties?.count() ?: 0) > 0) {
            binding?.invitationWrapper?.visibility = View.VISIBLE
            user?.invitations?.parties?.let { binding?.invitationsView?.setInvitations(it) }
        } else {
            binding?.invitationWrapper?.visibility = View.GONE
        }

        binding?.usernameTextview?.text = user?.formattedUsername
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GroupFormActivity.GROUP_FORM_ACTIVITY -> {
                if (resultCode == Activity.RESULT_OK) {
                    val bundle = data?.extras
                    if (bundle?.getString("groupType") == "party") {
                        socialRepository.createGroup(bundle.getString("name"),
                                bundle.getString("description"),
                                bundle.getString("leader"),
                                "party",
                                bundle.getString("privacy"),
                                bundle.getBoolean("leaderCreateChallenge"))
                                .flatMap {
                                    userRepository.retrieveUser(false)
                                }
                                .subscribe({
                                    fragmentManager?.popBackStack()
                                    MainNavigationController.navigate(R.id.partyFragment,
                                            bundleOf(Pair("partyID", user?.party?.id)))
                                }, RxErrorHandler.handleEmptyError())
                    }
                }
            }
        }
    }

    private fun refresh() {
        compositeSubscription.add(userRepository.retrieveUser(false, forced = true)
                .filter { it.hasParty() }
                .flatMap { socialRepository.retrieveGroup("party") }
                .flatMap { group1 -> socialRepository.retrieveGroupMembers(group1.id, true) }
                .doOnComplete { binding?.refreshLayout?.isRefreshing = false }
                .subscribe({  }, RxErrorHandler.handleEmptyError()))
    }

    override fun onDestroy() {
        userRepository.close()
        socialRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    companion object {

        fun newInstance(user: User?): NoPartyFragmentFragment {
            val args = Bundle()

            val fragment = NoPartyFragmentFragment()
            fragment.arguments = args
            fragment.user = user
            return fragment
        }
    }

}
