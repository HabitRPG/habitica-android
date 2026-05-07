package com.habitrpg.android.habitica.ui.fragments.skills

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.SkillDialog
import com.habitrpg.common.habitica.extensions.asPainter
import com.habitrpg.common.habitica.theme.HabiticaTheme

class SkillDialogBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_SKILL_TITLE = "skill_title"
        private const val ARG_SKILL_DESCRIPTION = "skill_description"
        private const val ARG_SKILL_MP_COST = "skill_mp_cost"
        private const val ARG_SKILL_KEY = "skill_key"
        private const val ARG_SKILL_PATH = "skill_path"
        private const val ARG_IS_TRANSFORMATION_ITEM = "is_transformation_item"

        fun newInstance(
            skillTitle: String? = "",
            skillDescription: String? = "",
            skillMpCost: String? = "",
            skillPath: String? = "",
            skillKey: String? = "",
            resourceIcon: Drawable? = null,
            isTransformationItem: Boolean = false,
            onUseSkill: () -> Unit
        ): SkillDialogBottomSheetFragment {
            return SkillDialogBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SKILL_TITLE, skillTitle)
                    putString(ARG_SKILL_DESCRIPTION, skillDescription)
                    putString(ARG_SKILL_MP_COST, skillMpCost)
                    putString(ARG_SKILL_KEY, skillKey)
                    putString(ARG_SKILL_PATH, skillPath)
                    putBoolean(ARG_IS_TRANSFORMATION_ITEM, isTransformationItem)
                }
                this.onUseSkill = onUseSkill
            }
        }
    }

    var onUseSkill: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog: DialogInterface ->
            val d = dialog as BottomSheetDialog
            d.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            d.behavior.skipCollapsed = true
        }
        return bottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        val args = requireArguments()
        val skillKey = args.getString(ARG_SKILL_KEY) ?: ""
        val skillPath = args.getString(ARG_SKILL_PATH) ?: ""
        val isTransformationItem = args.getBoolean(ARG_IS_TRANSFORMATION_ITEM, false)
        val magicIcon: Drawable = BitmapDrawable(resources, HabiticaIconsHelper.imageOfMagic())

        setContent {
            HabiticaTheme {
                SkillDialog(
                    skillKey = skillKey,
                    skillPath = skillPath,
                    resourceIconPainter = magicIcon.asPainter() ?: painterResource(R.drawable.empty_slot),
                    title = args.getString(ARG_SKILL_TITLE) ?: "",
                    description = args.getString(ARG_SKILL_DESCRIPTION) ?: "",
                    mpCost = args.getString(ARG_SKILL_MP_COST) ?: "",
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

