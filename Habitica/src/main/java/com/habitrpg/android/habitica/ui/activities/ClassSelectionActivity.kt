package com.habitrpg.android.habitica.ui.activities

import android.app.ProgressDialog
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.navArgs
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.*
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ClassSelectionActivity : BaseActivity(), Consumer<User> {

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
        selectedTitleTextView.text = getString(R.string.x_class, className)
        selectedButton.text = getString(R.string.become_x, className)
    }
    private var isInitialSelection: Boolean = false
    private var classWasUnset: Boolean? = false
    private var shouldFinish: Boolean? = false

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val healerAvatarView: AvatarView by bindView(R.id.healerAvatarView)
    private val healerWrapper: View by bindView(R.id.healerWrapper)
    private val healerButton: TextView by bindView(R.id.healerButton)
    private val mageAvatarView: AvatarView by bindView(R.id.mageAvatarView)
    private val mageWrapper: View by bindView(R.id.mageWrapper)
    private val mageButton: TextView by bindView(R.id.mageButton)
    private val rogueAvatarView: AvatarView by bindView(R.id.rogueAvatarView)
    private val rogueWrapper: View by bindView(R.id.rogueWrapper)
    private val rogueButton: TextView by bindView(R.id.rogueButton)
    private val warriorAvatarView: AvatarView by bindView(R.id.warriorAvatarView)
    private val warriorWrapper: View by bindView(R.id.warriorWrapper)
    private val warriorButton: TextView by bindView(R.id.warriorButton)

    private val selectedWrapperView: ViewGroup by bindView(R.id.selected_wrapper)
    private val selectedTitleTextView: TextView by bindView(R.id.selected_title_textview)
    private val selectedDescriptionTextView: TextView by bindView(R.id.selected_description_textview)
    private val selectedButton: Button by bindView(R.id.selected_button)

    @Inject
    lateinit var userRepository: UserRepository

    @Suppress("DEPRECATION")
    private var progressDialog: ProgressDialog? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_class_selection
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val args = navArgs<ClassSelectionActivityArgs>().value
        isInitialSelection = args.isInitialSelection
        currentClass = args.className

        compositeSubscription.add(userRepository.getUser().firstElement().subscribe(Consumer {
            it.preferences?.let {preferences ->
                val unmanagedPrefs = userRepository.getUnmanagedCopy(preferences)
                unmanagedPrefs.costume = false
                setAvatarViews(unmanagedPrefs)
            }
        }, RxErrorHandler.handleEmptyError()))

        if (!isInitialSelection) {
            compositeSubscription.add(userRepository.changeClass()
                    .subscribe(Consumer { classWasUnset = true }, RxErrorHandler.handleEmptyError()))
        }

        healerWrapper.setOnClickListener { newClass = "healer" }
        mageWrapper.setOnClickListener { newClass = "wizard" }
        rogueWrapper.setOnClickListener { newClass = "rogue" }
        warriorWrapper.setOnClickListener { newClass = "warrior" }
        selectedButton.setOnClickListener { displayConfirmationDialogForClass() }
    }

    override fun onStart() {
        super.onStart()
        newClass = currentClass ?: "healer"
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.class_selection, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
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
        healerAvatarView.setAvatar(healer)
        val healerIcon = BitmapDrawable(resources, HabiticaIconsHelper.imageOfHealerLightBg())
        healerButton.setCompoundDrawablesWithIntrinsicBounds(healerIcon, null, null, null)

        val mageOutfit = Outfit()
        mageOutfit.armor = "armor_wizard_5"
        mageOutfit.head = "head_wizard_5"
        mageOutfit.weapon = "weapon_wizard_6"
        val mage = this.makeUser(preferences, mageOutfit)
        mageAvatarView.setAvatar(mage)
        val mageIcon = BitmapDrawable(resources, HabiticaIconsHelper.imageOfMageLightBg())
        mageButton.setCompoundDrawablesWithIntrinsicBounds(mageIcon, null, null, null)

        val rogueOutfit = Outfit()
        rogueOutfit.armor = "armor_rogue_5"
        rogueOutfit.head = "head_rogue_5"
        rogueOutfit.shield = "shield_rogue_6"
        rogueOutfit.weapon = "weapon_rogue_6"
        val rogue = this.makeUser(preferences, rogueOutfit)
        rogueAvatarView.setAvatar(rogue)
        val rogueIcon = BitmapDrawable(resources, HabiticaIconsHelper.imageOfRogueLightBg())
        rogueButton.setCompoundDrawablesWithIntrinsicBounds(rogueIcon, null, null, null)

        val warriorOutfit = Outfit()
        warriorOutfit.armor = "armor_warrior_5"
        warriorOutfit.head = "head_warrior_5"
        warriorOutfit.shield = "shield_warrior_5"
        warriorOutfit.weapon = "weapon_warrior_6"
        val warrior = this.makeUser(preferences, warriorOutfit)
        warriorAvatarView.setAvatar(warrior)
        val warriorIcon = BitmapDrawable(resources, HabiticaIconsHelper.imageOfWarriorLightBg())
        warriorButton.setCompoundDrawablesWithIntrinsicBounds(warriorIcon, null, null, null)
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
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
        selectedDescriptionTextView.text = getString(R.string.healer_description)
        selectedWrapperView.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow_100))
        selectedTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.dark_brown))
        selectedDescriptionTextView.setTextColor(ContextCompat.getColor(this, R.color.dark_brown))
        selectedButton.setBackgroundResource(R.drawable.layout_rounded_bg_yellow_10)
        updateButtonBackgrounds(healerButton, getDrawable(R.drawable.layout_rounded_bg_brand_700_yellow_border))
    }

    private fun mageSelected() {
        className = getString(R.string.mage)
        selectedDescriptionTextView.text = getString(R.string.mage_description)
        selectedWrapperView.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_10))
        selectedTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        selectedDescriptionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        selectedButton.setBackgroundResource(R.drawable.layout_rounded_bg_gray_alpha)
        updateButtonBackgrounds(mageButton, getDrawable(R.drawable.layout_rounded_bg_brand_700_blue_border))
    }

    private fun rogueSelected() {
        className = getString(R.string.rogue)
        selectedDescriptionTextView.text = getString(R.string.rogue_description)
        selectedWrapperView.setBackgroundColor(ContextCompat.getColor(this, R.color.brand_200))
        selectedTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        selectedDescriptionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        selectedButton.setBackgroundResource(R.drawable.layout_rounded_bg_gray_alpha)
        updateButtonBackgrounds(rogueButton, getDrawable(R.drawable.layout_rounded_bg_brand_700_brand_border))
    }

    private fun warriorSelected() {
        className = getString(R.string.warrior)
        selectedDescriptionTextView.text = getString(R.string.warrior_description)
        selectedWrapperView.setBackgroundColor(ContextCompat.getColor(this, R.color.maroon_50))
        selectedTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        selectedDescriptionTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
        selectedButton.setBackgroundResource(R.drawable.layout_rounded_bg_gray_alpha)
        updateButtonBackgrounds(warriorButton, getDrawable(R.drawable.layout_rounded_bg_brand_700_red_border))
    }

    private fun updateButtonBackgrounds(selectedButton: TextView, background: Drawable?) {
        val deselectedBackground = getDrawable(R.drawable.layout_rounded_bg_gray_700)
        healerButton.background = if (healerButton == selectedButton) background else deselectedBackground
        mageButton.background = if (mageButton == selectedButton) background else deselectedBackground
        rogueButton.background = if (rogueButton == selectedButton) background else deselectedBackground
        warriorButton.background = if (warriorButton == selectedButton) background else deselectedBackground
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
            alert.setTitle(getString(R.string.change_class_confirmation))
            alert.setMessage(getString(R.string.change_class_equipment_warning, currentClass))
            alert.addButton(R.string.choose_class, true) { _, _ ->
                        selectClass(newClass)
                        displayClassChanged()
                    }
            alert.addButton(R.string.dialog_go_back, false)
            alert.show()
        } else {
            val alert = HabiticaAlertDialog(this)
            alert.setTitle(getString(R.string.class_confirmation, className))
            alert.addButton(R.string.choose_class, true) { _, _ -> selectClass(newClass) }
            alert.addButton(R.string.dialog_go_back, false)
            alert.show()
        }
    }

    private fun displayClassChanged() {
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(getString(R.string.class_changed, className))
        alert.setMessage(getString(R.string.class_changed_description))
        alert.addButton(getString(R.string.complete_tutorial), true)
        alert.show()
    }

    private fun optOutOfClasses() {
        shouldFinish = true
        this.displayProgressDialog(getString(R.string.opting_out_progress))
        compositeSubscription.add(userRepository.disableClasses().subscribe(this, RxErrorHandler.handleEmptyError()))
    }

    private fun selectClass(selectedClass: String) {
        shouldFinish = true
        this.displayProgressDialog(getString(R.string.changing_class_progress))
        compositeSubscription.add(userRepository.changeClass(selectedClass).subscribe(this, RxErrorHandler.handleEmptyError()))
    }

    private fun displayProgressDialog(progressText: String) {
        @Suppress("DEPRECATION")
        progressDialog = ProgressDialog.show(this, progressText, null, true)
    }

    override fun accept(user: User) {
        if (shouldFinish == true) {
            progressDialog?.dismiss()
            setResult(MainActivity.SELECT_CLASS_RESULT)
            finish()
        }
    }
}
