package com.habitrpg.android.habitica.ui.fragments.purchases

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentGiftGemBalanceBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
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
                binding?.avatarView?.setAvatar(it)
                binding?.displayNameTextview?.username = it.profile?.name
                binding?.displayNameTextview?.tier = it.contributor?.level ?: 0
                binding?.usernameTextview?.text = "@${it.username}"
            }
        }

    var onCompleted: (() -> Unit)? = null

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.giftButton?.setOnClickListener { sendGift() }
    }

    private fun sendGift() {
        if (isGifting) return
        isGifting = true
        try {
            val amount = binding?.giftEditText?.text.toString().toInt()
            giftedMember?.id?.let {
                compositeSubscription.add(
                    socialRepository.transferGems(it, amount)
                        .flatMap { userRepository.retrieveUser(false, true) }
                        .doOnError {
                            isGifting = false
                        }
                        .subscribe(
                            {
                                onCompleted?.invoke()
                            },
                            RxErrorHandler.handleEmptyError()
                        )
                )
            }
        } catch (ignored: NumberFormatException) {}
    }
}
