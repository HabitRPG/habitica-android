package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.databinding.FragmentPromoInfoBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PromoInfoFragment : BaseMainFragment<FragmentPromoInfoBinding>() {
    override var binding: FragmentPromoInfoBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPromoInfoBinding {
        return FragmentPromoInfoBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var configManager: AppConfigManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val promo = configManager.activePromo()
        promo?.configureInfoFragment(this)
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.title = ""
    }
}
