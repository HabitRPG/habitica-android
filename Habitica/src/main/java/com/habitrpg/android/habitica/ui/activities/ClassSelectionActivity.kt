package com.habitrpg.android.habitica.ui.activities

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.View
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.*
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ClassSelectionActivity : BaseActivity(), Consumer<User> {

    private var currentClass: String? = null
    private var isInitialSelection: Boolean = false
    private var classWasUnset: Boolean? = false
    private var shouldFinish: Boolean? = false

    internal val healerAvatarView: AvatarView by bindView(R.id.healerAvatarView)
    private val healerWrapper: View by bindView(R.id.healerWrapper)
    internal val mageAvatarView: AvatarView by bindView(R.id.mageAvatarView)
    private val mageWrapper: View by bindView(R.id.mageWrapper)
    internal val rogueAvatarView: AvatarView by bindView(R.id.rogueAvatarView)
    private val rogueWrapper: View by bindView(R.id.rogueWrapper)
    internal val warriorAvatarView: AvatarView by bindView(R.id.warriorAvatarView)
    private val warriorWrapper: View by bindView(R.id.warriorWrapper)
    private val optOutWrapper: View by bindView(R.id.optOutWrapper)

    @Inject
    lateinit var userRepository: UserRepository

    @Suppress("DEPRECATION")
    private var progressDialog: ProgressDialog? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_class_selection
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val bundle = intent.extras
        isInitialSelection = bundle?.getBoolean("isInitialSelection") ?: false

        val preferences = Preferences()
        preferences.setHair(Hair())
        preferences.costume = false
        bundle.notNull { thisBundle ->
            currentClass = thisBundle.getString("currentClass")
            preferences.setSize(thisBundle.getString("size") ?: "slim")
            preferences.setSkin(thisBundle.getString("skin") ?: "")
            preferences.setShirt(thisBundle.getString("shirt") ?: "")
            preferences.hair?.bangs = thisBundle.getInt("hairBangs")
            preferences.hair?.base = thisBundle.getInt("hairBase")
            preferences.hair?.color = thisBundle.getString("hairColor")
            preferences.hair?.mustache = thisBundle.getInt("hairMustache")
            preferences.hair?.beard = thisBundle.getInt("hairBeard")
        }


        val healerOutfit = Outfit()
        healerOutfit.armor = "armor_healer_5"
        healerOutfit.head = "head_healer_5"
        healerOutfit.shield = "shield_healer_5"
        healerOutfit.weapon = "weapon_healer_6"
        val healer = this.makeUser(preferences, healerOutfit)
        healerAvatarView.setAvatar(healer)

        val mageOutfit = Outfit()
        mageOutfit.armor = "armor_wizard_5"
        mageOutfit.head = "head_wizard_5"
        mageOutfit.weapon = "weapon_wizard_6"
        val mage = this.makeUser(preferences, mageOutfit)
        mageAvatarView.setAvatar(mage)

        val rogueOutfit = Outfit()
        rogueOutfit.armor = "armor_rogue_5"
        rogueOutfit.head = "head_rogue_5"
        rogueOutfit.shield = "shield_rogue_6"
        rogueOutfit.weapon = "weapon_rogue_6"
        val rogue = this.makeUser(preferences, rogueOutfit)
        rogueAvatarView.setAvatar(rogue)

        val warriorOutfit = Outfit()
        warriorOutfit.armor = "armor_warrior_5"
        warriorOutfit.head = "head_warrior_5"
        warriorOutfit.shield = "shield_warrior_5"
        warriorOutfit.weapon = "weapon_warrior_6"
        val warrior = this.makeUser(preferences, warriorOutfit)
        warriorAvatarView.setAvatar(warrior)

        if (!isInitialSelection) {
            compositeSubscription.add(userRepository.changeClass()
                    .subscribe(Consumer { classWasUnset = true }, RxErrorHandler.handleEmptyError()))
        }

        healerWrapper.setOnClickListener { healerSelected() }
        mageWrapper.setOnClickListener { mageSelected() }
        rogueWrapper.setOnClickListener { rogueSelected() }
        warriorWrapper.setOnClickListener { warriorSelected() }
        optOutWrapper.setOnClickListener { optOutSelected() }
    }

    override fun injectActivity(component: AppComponent?) {
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
        displayConfirmationDialogForClass(getString(R.string.healer), Stats.HEALER)
    }

    private fun mageSelected() {
        displayConfirmationDialogForClass(getString(R.string.mage), Stats.MAGE)
    }

    private fun rogueSelected() {
        displayConfirmationDialogForClass(getString(R.string.rogue), Stats.ROGUE)
    }

    private fun warriorSelected() {
        displayConfirmationDialogForClass(getString(R.string.warrior), Stats.WARRIOR)
    }

    private fun optOutSelected() {
        if (!this.isInitialSelection && this.classWasUnset == false) {
            return
        }
        val alert = AlertDialog.Builder(this)
                .setTitle(getString(R.string.opt_out_confirmation))
                .setNegativeButton(getString(R.string.dialog_go_back)) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(getString(R.string.opt_out_class)) { _, _ -> optOutOfClasses() }.create()
        alert.show()
    }

    private fun displayConfirmationDialogForClass(className: String, classIdentifier: String) {

        if (!this.isInitialSelection && this.classWasUnset == false) {
            val builder = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.change_class_confirmation))
                    .setMessage(getString(R.string.change_class_equipment_warning, currentClass))
                    .setNegativeButton(getString(R.string.dialog_go_back)) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(getString(R.string.choose_class)) { _, _ ->
                        selectClass(classIdentifier)
                        displayClassChanged(className)
                    }
            val alert = builder.create()
            alert.show()
        } else {
            val builder = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.class_confirmation, className))
                    .setNegativeButton(getString(R.string.dialog_go_back)) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(getString(R.string.choose_class)) { _, _ -> selectClass(classIdentifier) }
            val alert = builder.create()
            alert.show()
        }
    }

    private fun displayClassChanged(newClassName: String) {
        val changeConfirmedBuilder = AlertDialog.Builder(this)
                .setTitle(getString(R.string.class_changed, newClassName))
                .setMessage(getString(R.string.class_changed_description))
                .setPositiveButton(getString(R.string.complete_tutorial)) { dialog, _ -> dialog.dismiss() }
        val changeDoneAlert = changeConfirmedBuilder.create()
        changeDoneAlert.show()
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
