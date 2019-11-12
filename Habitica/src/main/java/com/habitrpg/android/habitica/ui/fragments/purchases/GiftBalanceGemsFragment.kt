package com.habitrpg.android.habitica.ui.fragments.purchases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import io.reactivex.functions.Consumer
import javax.inject.Inject

class GiftBalanceGemsFragment : BaseFragment() {

    @Inject
    lateinit var socialRepository: SocialRepository

    private val avatarView: AvatarView by bindView(R.id.avatar_view)
    private val displayNameTextView: UsernameLabel by bindView(R.id.display_name_textview)
    private val usernameTextView: TextView by bindView(R.id.username_textview)
    private val giftEditText: EditText by bindView(R.id.gift_edit_text)
    private val giftButton: Button by bindView(R.id.gift_button)

    var giftedMember: Member? = null
        set(value) {
            field = value
            field?.let {
                avatarView.setAvatar(it)
                displayNameTextView.username = it.profile?.name
                displayNameTextView.tier = it.contributor?.level ?: 0
                usernameTextView.text = "@${it.username}"
            }
        }

    var onCompleted: (() -> Unit)? = null

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_gift_gem_balance)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        giftButton.setOnClickListener { sendGift() }
    }

    private fun sendGift() {
        try {
            val amount = giftEditText.text.toString().toInt()
            giftedMember?.id?.let {
                compositeSubscription.add(socialRepository.transferGems(it, amount).subscribe(Consumer {
                    onCompleted?.invoke()
                }, RxErrorHandler.handleEmptyError()))
            }
        } catch (ignored: NumberFormatException) {}
    }
}