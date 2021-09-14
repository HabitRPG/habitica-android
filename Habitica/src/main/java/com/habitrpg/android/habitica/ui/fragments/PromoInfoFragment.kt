package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentPromoInfoBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import javax.inject.Inject

class PromoInfoFragment : BaseMainFragment<FragmentPromoInfoBinding>() {

    override var binding: FragmentPromoInfoBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPromoInfoBinding {
        return FragmentPromoInfoBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var configManager: AppConfigManager

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val promo = configManager.activePromo()
        promo?.configureInfoFragment(this)
    }

    override fun onResume() {
        super.onResume()
        activity?.title = ""
    }
}
