package com.habitrpg.android.habitica.ui.fragments.purchases

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentGiftGemBalanceBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.helpers.launchCatching
import javax.inject.Inject

class GiftBalanceGemsFragment : BaseFragment<FragmentGiftGemBalanceBinding>() {

    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var userRepository: UserRepository

    override var binding: FragmentGiftGemBalanceBinding? = null

    private var isGifting = false
        set(value) {
            field = value
            binding?.giftButton?.isVisible = !isGifting
            binding?.progressBar?.isVisible = isGifting
        }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGiftGemBalanceBinding {
        return FragmentGiftGemBalanceBinding.inflate(inflater, container, false)
    }

    var giftedMember: Member? = null
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            field?.let {
                updateMemberViews()
            }
        }

    private fun updateMemberViews() {
        val it = giftedMember ?: return
        binding?.avatarView?.setAvatar(it)
        binding?.displayNameTextview?.username = it.profile?.name
        binding?.displayNameTextview?.tier = it.contributor?.level ?: 0
        binding?.usernameTextview?.text = it.formattedUsername
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.giftButton?.setOnClickListener { sendGift() }
        updateMemberViews()
    }

    private fun sendGift() {
        if (isGifting) return
        isGifting = true
        try {
            val amount = binding?.giftEditText?.text.toString().strip().toInt()
            giftedMember?.id?.let {
                activity?.lifecycleScope?.launchCatching({
                    isGifting = false
                }) {
                    socialRepository.transferGems(it, amount)
                    userRepository.retrieveUser(false)
                    val dialog = context?.let { it1 -> HabiticaAlertDialog(it1) }
                    dialog?.setTitle(R.string.gift_confirmation_title)
                    dialog?.setMessage(
                        getString(
                            R.string.gift_confirmation_text_gems_new,
                            giftedMember?.username,
                            amount.toString()
                        )
                    )
                    dialog?.addCloseButton { _, _ ->
                        activity?.finish()
                    }
                    dialog?.show()
                }
            }
        } catch (ignored: NumberFormatException) {
        }
    }
}
