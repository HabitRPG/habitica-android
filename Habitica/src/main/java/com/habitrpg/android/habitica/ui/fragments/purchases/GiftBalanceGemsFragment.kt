package com.habitrpg.android.habitica.ui.fragments.purchases

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentGiftGemBalanceBinding
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

class GiftBalanceGemsFragment : BaseFragment<FragmentGiftGemBalanceBinding>() {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository

    override var binding: FragmentGiftGemBalanceBinding? = null

    private var isGifting = false

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGiftGemBalanceBinding {
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
            val amount = binding?.giftEditText?.text.toString().toInt()
            giftedMember?.id?.let {
                compositeSubscription.add(
                    socialRepository.transferGems(it, amount)
                        .doOnError {
                            isGifting = false
                        }
                        .subscribe(
                            {
                                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                                    userRepository.retrieveUser(false)
                                }
                            activity?.finish()
                            }, ExceptionHandler.rx()
                        )
                )
            }
        } catch (ignored: NumberFormatException) {}
    }
}
