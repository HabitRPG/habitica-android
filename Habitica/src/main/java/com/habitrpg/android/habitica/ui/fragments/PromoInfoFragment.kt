package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentPromoInfoBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import javax.inject.Inject

class PromoInfoFragment : BaseMainFragment() {

    internal lateinit var binding: FragmentPromoInfoBinding

    @Inject
    lateinit var configManager: AppConfigManager

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.hidesToolbar = true
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentPromoInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val promo = configManager.activePromo()
        promo?.configureInfoFragment(this)
    }
}