package com.habitrpg.android.habitica.ui.fragments.skills

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.SkillDialog
import com.habitrpg.common.habitica.extensions.asPainter
import com.habitrpg.common.habitica.theme.HabiticaTheme

class SkillDialogBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_SKILL_TITLE = "skill_title"
        private const val ARG_SKILL_DESCRIPTION = "skill_description"
        private const val ARG_SKILL_MP_COST = "skill_mp_cost"

        fun newInstance(
            skillTitle: String,
            skillDescription: String,
            skillMpCost: String,
            skillPath: String,
            skillKey: String,
            resourceIcon: Drawable,
            isTransformationItem: Boolean = false,
            onUseSkill: () -> Unit
        ): SkillDialogBottomSheetFragment {
            return SkillDialogBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SKILL_TITLE, skillTitle)
                    putString(ARG_SKILL_DESCRIPTION, skillDescription)
                    putString(ARG_SKILL_MP_COST, skillMpCost)
                }
                this.resourceIcon = resourceIcon
                this.skillKey = skillKey
                this.skillPath = skillPath
                this.onUseSkill = onUseSkill
                this.isTransformationItem = isTransformationItem
            }
        }
    }

    var onUseSkill: (() -> Unit)? = null
    private var resourceIcon: Drawable? = null
    var skillKey = ""
    var skillPath = ""
    var isTransformationItem: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            HabiticaTheme {
                SkillDialog(
                    skillKey = skillKey,
                    skillPath = skillPath,
                    resourceIconPainter = resourceIcon?.asPainter() ?: painterResource(R.drawable.empty_slot),
                    title = requireArguments().getString(ARG_SKILL_TITLE) ?: "",
                    description = requireArguments().getString(ARG_SKILL_DESCRIPTION) ?: "",
                    mpCost = requireArguments().getString(ARG_SKILL_MP_COST) ?: "",
                    isTransformationItem = isTransformationItem,
                    onUseSkill = {
                        onUseSkill?.invoke()
                        dismiss()
                    }
                )
            }
        }
    }
}

