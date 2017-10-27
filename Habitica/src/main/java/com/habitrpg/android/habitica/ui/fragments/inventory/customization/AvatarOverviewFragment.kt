package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import kotlinx.android.synthetic.main.fragment_avatar_overview.*
import rx.functions.Action1

class AvatarOverviewFragment : BaseMainFragment(), AdapterView.OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (apiClient != null) {
            apiClient.content
                    .subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_avatar_overview, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (this.user == null) {
            return
        }

        this.setSize(this.user?.preferences?.size)
        avatarSizeSpinner.onItemSelectedListener = this

        avatarShirtView.setOnClickListener { displayCustomizationFragment("shirt", null) }

        avatarSkinView.setOnClickListener { displayCustomizationFragment("skin", null) }

        avatarHairColorView.setOnClickListener { displayCustomizationFragment("hair", "color") }
        avatarHairBangsView.setOnClickListener { displayCustomizationFragment("hair", "bangs") }
        avatarHairBaseView.setOnClickListener { displayCustomizationFragment("hair", "base") }
        avatarHairFlowerView.setOnClickListener { displayCustomizationFragment("hair", "flower") }
        avatarHairBeardView.setOnClickListener { displayCustomizationFragment("hair", "beard") }
        avatarHairMustacheView.setOnClickListener { displayCustomizationFragment("hair", "mustache") }
        avatarBackgroundView.setOnClickListener { displayCustomizationFragment("background", null) }
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun displayCustomizationFragment(type: String, category: String?) {
        val fragment = AvatarCustomizationFragment()
        fragment.type = type
        fragment.category = category
        activity?.displayFragment(fragment)
    }

    override fun updateUserData(user: User?) {
        super.updateUserData(user)
        this.setSize(user?.preferences?.size)
    }

    private fun setSize(size: String?) {
        if (avatarSizeSpinner == null || size == null) {
            return
        }
        if (size == "slim") {
            avatarSizeSpinner.setSelection(0, false)
        } else {
            avatarSizeSpinner.setSelection(1, false)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val newSize: String = if (position == 0) "slim" else "broad"

        if (this.user != null && this.user!!.preferences.size != newSize) {
            userRepository.updateUser(user, "preferences.size", newSize)
                    .subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}


    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.sidebar_avatar)
    }
}
