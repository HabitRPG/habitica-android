package com.habitrpg.android.habitica.ui.activities

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityClassSelectionBinding
import com.habitrpg.android.habitica.models.user.Gear
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.Outfit
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaProgressDialog
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClassSelectionActivity : BaseActivity() {

    @Inject
    lateinit var userViewModel: MainUserViewModel

    private lateinit var binding: ActivityClassSelectionBinding
    private var currentClass: String? = null
    private var newClass: String = "healer"
        set(value) {
            field = value
            when (value) {
                "healer" -> healerSelected()
                "wizard" -> mageSelected()
                "mage" -> mageSelected()
                "rogue" -> rogueSelected()
                "warrior" -> warriorSelected()
            }
        }
    private var className: String? = null
        set(value) {
            field = value
            binding.selectedTitleTextView.text = getString(R.string.x_class, className)
            binding.selectedButton.text = getString(R.string.become_x, className)
        }
    private var isInitialSelection: Boolean = false
    private var classWasUnset: Boolean? = false
    private var shouldFinish: Boolean? = false

    private var progressDialog: HabiticaProgressDialog? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_class_selection
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityClassSelectionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val args = navArgs<ClassSelectionActivityArgs>().value
        isInitialSelection = args.isInitialSelection
        currentClass = args.className

        newClass = currentClass ?: "healer"

        userViewModel.user.observe(this) {
            it?.preferences?.let { preferences ->
                val unmanagedPrefs = userRepository.getUnmanagedCopy(preferences)
                unmanagedPrefs.costume = false
                setAvatarViews(unmanagedPrefs)
            }
        }

        if (!isInitialSelection) {
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                userRepository.changeClass()
                classWasUnset
            }
        }

        binding.healerWrapper.setOnClickListener { newClass = "healer" }
        binding.mageWrapper.setOnClickListener { newClass = "wizard" }
        binding.rogueWrapper.setOnClickListener { newClass = "rogue" }
        binding.warriorWrapper.setOnClickListener { newClass = "warrior" }
        binding.selectedButton.setOnClickListener { displayConfirmationDialogForClass() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.class_selection, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.opt_out -> optOutSelected()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setAvatarViews(preferences: Preferences) {
        val healerOutfit = Outfit()
        healerOutfit.armor = "armor_healer_5"
        healerOutfit.head = "head_healer_5"
        healerOutfit.shield = "shield_healer_5"
        healerOutfit.weapon = "weapon_healer_6"
        val healer = this.makeUser(preferences, healerOutfit)
        binding.healerAvatarView.setAvatar(healer)
        val healerIcon = BitmapDrawable(resources, HabiticaIconsHelper.imageOfHealerLightBg())
        binding.healerButton.setCompoundDrawablesWithIntrinsicBounds(healerIcon, null, null, null)

        val mageOutfit = Outfit()
        mageOutfit.armor = "armor_wizard_5"
        mageOutfit.head = "head_wizard_5"
        mageOutfit.weapon = "weapon_wizard_6"
        val mage = this.makeUser(preferences, mageOutfit)
        binding.mageAvatarView.setAvatar(mage)
        val mageIcon = BitmapDrawable(resources, HabiticaIconsHelper.imageOfMageLightBg())
        binding.mageButton.setCompoundDrawablesWithIntrinsicBounds(mageIcon, null, null, null)

        val rogueOutfit = Outfit()
        rogueOutfit.armor = "armor_rogue_5"
        rogueOutfit.head = "head_rogue_5"
        rogueOutfit.shield = "shield_rogue_6"
        rogueOutfit.weapon = "weapon_rogue_6"
        val rogue = this.makeUser(preferences, rogueOutfit)
        binding.rogueAvatarView.setAvatar(rogue)
        val rogueIcon = BitmapDrawable(resources, HabiticaIconsHelper.imageOfRogueLightBg())
        binding.rogueButton.setCompoundDrawablesWithIntrinsicBounds(rogueIcon, null, null, null)

        val warriorOutfit = Outfit()
        warriorOutfit.armor = "armor_warrior_5"
        warriorOutfit.head = "head_warrior_5"
        warriorOutfit.shield = "shield_warrior_5"
        warriorOutfit.weapon = "weapon_warrior_6"
        val warrior = this.makeUser(preferences, warriorOutfit)
        binding.warriorAvatarView.setAvatar(warrior)
        val warriorIcon = BitmapDrawable(resources, HabiticaIconsHelper.imageOfWarriorLightBg())
        binding.warriorButton.setCompoundDrawablesWithIntrinsicBounds(warriorIcon, null, null, null)
    }

    private fun makeUser(preferences: Preferences, outfit: Outfit): User {
        val user = User()
        user.preferences = preferences
        user.items = Items()
        user.items?.gear = Gear()
        user.items?.gear?.equipped = outfit
        return user
    }

    private fun healerSelected() {
        className = getString(R.string.healer)
        binding.selectedDescriptionTextView.text = getString(R.string.healer_description)
        binding.selectedWrapperView.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow_100))
        binding.selectedTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.dark_brown))
        binding.selectedDescriptionTextView.setTextColor(ContextCompat.getColor(this, R.color.dark_brown))
        binding.selectedButton.setBackgroundResource(R.drawable.layout_rounded_bg_yellow_10)
        updateButtonBackgrounds(binding.healerButton, ContextCompat.getDrawable(this, R.drawable.layout_rounded_bg_window_yellow_border))
    }

    private fun mageSelected() {
        className = getString(R.string.mage)
        binding.selectedDescriptionTextView.text = getString(R.string.mage_description)
        binding.selectedWrapperView.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_10))
        binding.selectedTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.selectedDescriptionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.selectedButton.setBackgroundResource(R.drawable.layout_rounded_bg_gray_alpha)
        updateButtonBackgrounds(binding.mageButton, ContextCompat.getDrawable(this, R.drawable.layout_rounded_bg_window_blue_border))
    }

    private fun rogueSelected() {
        className = getString(R.string.rogue)
        binding.selectedDescriptionTextView.text = getString(R.string.rogue_description)
        binding.selectedWrapperView.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_200))
        binding.selectedTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.selectedDescriptionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.selectedButton.setBackgroundResource(R.drawable.layout_rounded_bg_gray_alpha)
        updateButtonBackgrounds(binding.rogueButton, ContextCompat.getDrawable(this, R.drawable.layout_rounded_bg_window_brand_border))
    }

    private fun warriorSelected() {
        className = getString(R.string.warrior)
        binding.selectedDescriptionTextView.text = getString(R.string.warrior_description)
        binding.selectedWrapperView.setBackgroundColor(ContextCompat.getColor(this, R.color.maroon_50))
        binding.selectedTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.selectedDescriptionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.selectedButton.setBackgroundResource(R.drawable.layout_rounded_bg_gray_alpha)
        updateButtonBackgrounds(binding.warriorButton, ContextCompat.getDrawable(this, R.drawable.layout_rounded_bg_window_red_border))
    }

    private fun updateButtonBackgrounds(selectedButton: TextView, background: Drawable?) {
        val deselectedBackground = ContextCompat.getDrawable(this, R.drawable.layout_rounded_bg_window)
        binding.healerButton.background = if (binding.healerButton == selectedButton) background else deselectedBackground
        binding.mageButton.background = if (binding.mageButton == selectedButton) background else deselectedBackground
        binding.rogueButton.background = if (binding.rogueButton == selectedButton) background else deselectedBackground
        binding.warriorButton.background = if (binding.warriorButton == selectedButton) background else deselectedBackground
    }

    private fun optOutSelected() {
        if (!this.isInitialSelection && this.classWasUnset == false) {
            return
        }
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(getString(R.string.opt_out_confirmation))
        alert.addButton(R.string.opt_out_class, true) { _, _ -> optOutOfClasses() }
        alert.addButton(R.string.dialog_go_back, false)
        alert.show()
    }

    private fun displayConfirmationDialogForClass() {
        if (!this.isInitialSelection && this.classWasUnset == false) {
            val alert = HabiticaAlertDialog(this)
            alert.setTitle(getString(R.string.change_class_selected_confirmation, newClass))
            alert.setMessage(getString(R.string.change_class_equipment_warning))
            alert.addButton(R.string.choose_class, true) { _, _ ->
                selectClass(newClass, true)
            }
            alert.addButton(R.string.dialog_go_back, false)
            alert.show()
        } else {
            val alert = HabiticaAlertDialog(this)
            alert.setTitle(getString(R.string.class_confirmation, className))
            alert.addButton(R.string.choose_class, true) { _, _ -> selectClass(newClass, false) }
            alert.addButton(R.string.dialog_go_back, false)
            alert.show()
        }
    }

    private fun displayClassChanged(selectedClass: String) {
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(getString(R.string.class_changed, className))
        alert.setMessage(getString(R.string.class_changed_description, selectedClass))
        alert.addButton(getString(R.string.complete_tutorial), true){ _, _ -> dismiss() }
        alert.setOnCancelListener {
            dismiss()
        }
        alert.show()
    }

    private fun optOutOfClasses() {
        shouldFinish = true
        this.displayProgressDialog(getString(R.string.opting_out_progress))
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.disableClasses()
            dismiss()
        }
    }

    private fun selectClass(selectedClass: String, isChanging: Boolean) {
        shouldFinish = true
        this.displayProgressDialog(getString(R.string.changing_class_progress))
        lifecycleScope.launch(Dispatchers.Main) {
            userRepository.changeClass(selectedClass)
            if (isChanging) displayClassChanged(selectedClass)
        }
    }

    private fun displayProgressDialog(progressText: String) {
        HabiticaProgressDialog.show(this, progressText, 300)
    }

    private fun dismiss() {
        if (shouldFinish == true) {
            progressDialog?.dismiss()
            finish()
        }
    }
}
