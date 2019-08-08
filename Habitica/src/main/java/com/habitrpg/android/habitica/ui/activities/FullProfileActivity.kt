package com.habitrpg.android.habitica.ui.activities

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.UserStatComputer
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.Outfit
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel
import com.habitrpg.android.habitica.ui.adapter.social.AchievementProfileAdapter
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.helpers.loadImage
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import net.pherth.android.emoji_library.EmojiEditText
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class FullProfileActivity : BaseActivity() {
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var socialRepository: SocialRepository

    private val profileImage: SimpleDraweeView by bindView(R.id.profile_image)
    private val blurbTextView: TextView by bindView(R.id.profile_blurb)
    private val avatarView: AvatarView by bindView(R.id.avatarView)
    private val copyUsernameButton: Button by bindView(R.id.copy_username)
    private val usernameText: TextView by bindView(R.id.username)
    private val attributesCardView: androidx.cardview.widget.CardView by bindView(R.id.profile_attributes_card)
    private val attributesTableLayout: TableLayout by bindView(R.id.attributes_table)
    private val attributesCollapseIcon: AppCompatImageView by bindView(R.id.attributes_collapse_icon)
    private val equipmentTableLayout: TableLayout by bindView(R.id.equipment_table)
    private val costumeTableLayout: TableLayout by bindView(R.id.costume_table)
    private val costumeCard: androidx.cardview.widget.CardView by bindView(R.id.profile_costume_card)
    private val avatarWithStatsView: View by bindView(R.id.avatar_with_bars)
    private val scrollView: ScrollView by bindView(R.id.fullprofile_scrollview)
    private val petsFoundCount: TextView by bindView(R.id.profile_pets_found_count)
    private val mountsTamedCount: TextView by bindView(R.id.profile_mounts_tamed_count)
    private val currentPetDrawee: SimpleDraweeView by bindView(R.id.current_pet_drawee)
    private val currentMountDrawee: SimpleDraweeView by bindView(R.id.current_mount_drawee)
    private val achievementCard: androidx.cardview.widget.CardView by bindView(R.id.profile_achievements_card)
    private val achievementProgress: ProgressBar by bindView(R.id.avatar_achievements_progress)
    private val achievementGroupList: androidx.recyclerview.widget.RecyclerView by bindView(R.id.recyclerView)
    private val joinedView: TextView by bindView(R.id.joined_view)
    private val lastLoginView: TextView by bindView(R.id.last_login_view)
    private val totalCheckinsView: TextView by bindView(R.id.total_checkins_view)

    private var userID = ""
    private var userName: String? = null
    private var avatarWithBars: AvatarWithBarsViewModel? = null
    private var attributeStrSum = 0f
    private var attributeIntSum = 0f
    private var attributeConSum = 0f
    private var attributePerSum = 0f
    private var attributeDetailsHidden = true
    private val attributeRows = ArrayList<TableRow>()
    private val dateFormatter = SimpleDateFormat.getDateInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userID = intent?.extras?.getString("userID", "") ?: ""
        if (userID.isEmpty()) {
            userID = intent?.data?.path?.removePrefix("/profile/") ?: ""
        }

        setTitle(R.string.profile_loading_data)

        compositeSubscription.add(socialRepository.getMember(this.userID).subscribe(Consumer { this.updateView(it) }, RxErrorHandler.handleEmptyError()))

        avatarWithBars?.valueBarLabelsToBlack()

        avatarWithStatsView.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))

        attributeRows.clear()
        attributesCardView.setOnClickListener { toggleAttributeDetails() }

        avatarWithBars = AvatarWithBarsViewModel(this, avatarWithStatsView)
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        return when (id) {
            R.id.private_message -> {
                showSendMessageToUserDialog()
                true
            }
            android.R.id.home -> {
                // app icon in action bar clicked; goto parent activity.
                this.finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSendMessageToUserDialog() {
        val factory = LayoutInflater.from(this)
        val newMessageView = factory.inflate(R.layout.profile_new_message_dialog, null)

        val emojiEditText = newMessageView.findViewById<EmojiEditText>(R.id.edit_new_message_text)

        val newMessageTitle = newMessageView.findViewById<TextView>(R.id.new_message_title)
        newMessageTitle.text = String.format(getString(R.string.profile_send_message_to), userName)

        val addMessageDialog = HabiticaAlertDialog(this)
        addMessageDialog.addButton(android.R.string.ok, true) { _, _ ->
                    socialRepository.postPrivateMessage(userID, emojiEditText.text.toString())
                            .subscribe(Consumer {
                                HabiticaSnackbar.showSnackbar(this@FullProfileActivity.scrollView.getChildAt(0) as ViewGroup,
                                        String.format(getString(R.string.profile_message_sent_to), userName), SnackbarDisplayType.NORMAL)
                            }, RxErrorHandler.handleEmptyError())

                    dismissKeyboard()
                }
        addMessageDialog.addButton(android.R.string.cancel, false) { _, _ -> dismissKeyboard() }
        addMessageDialog.setAdditionalContentView(newMessageView)
        addMessageDialog.show()
    }

    private fun updateView(user: Member) {
        val profile = user.profile ?: return

        updatePetsMountsView(user)
        userName = profile.name

        title = profile.name

        val imageUrl = profile.imageUrl
        if (imageUrl == null || imageUrl.isEmpty()) {
            profileImage.visibility = View.GONE
        } else {
            profileImage.controller = Fresco.newDraweeControllerBuilder()
                    .setUri(imageUrl)
                    .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                        override fun onFailure(id: String?, throwable: Throwable?) {
                            profileImage.visibility = View.GONE
                        }
                    })
                    .build()
        }

        val blurbText = profile.blurb
        if (blurbText != null && !blurbText.isEmpty()) {
            blurbTextView.text = MarkdownParser.parseMarkdown(blurbText)
        }

        user.authentication?.timestamps?.createdAt?.let { joinedView.text = dateFormatter.format(it) }
        user.authentication?.timestamps?.lastLoggedIn?.let { lastLoginView.text = dateFormatter.format(it) }
        totalCheckinsView.text = user.loginIncentives.toString()

        usernameText.text = user.username
        copyUsernameButton.visibility = View.VISIBLE
        copyUsernameButton.setOnClickListener { view ->
            val clipboard = view.context
                    .getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(user.username, user.username)
            clipboard?.primaryClip = clip
        }

        avatarView.setAvatar(user)
        avatarWithBars?.updateData(user)

        compositeSubscription.add(loadItemDataByOutfit(user.equipped).subscribe(Consumer { gear -> this.gotGear(gear, user) }, RxErrorHandler.handleEmptyError()))

        if (user.preferences?.costume == true) {
            compositeSubscription.add(loadItemDataByOutfit(user.costume).subscribe(Consumer<RealmResults<Equipment>> { this.gotCostume(it) }, RxErrorHandler.handleEmptyError()))
        } else {
            costumeCard.visibility = View.GONE
        }


        // Load the members achievements now
        compositeSubscription.add(socialRepository.getMemberAchievements(this.userID).subscribe(Consumer { this.fillAchievements(it) }, RxErrorHandler.handleEmptyError()))
    }

    private fun updatePetsMountsView(user: Member) {
        petsFoundCount.text = user.petsFoundCount.toString()
        mountsTamedCount.text = user.mountsTamedCount.toString()

        currentPetDrawee.loadImage("Pet-" + user.currentPet)
        currentMountDrawee.loadImage("Mount_Icon_" + user.currentMount)
    }

    // endregion

    // region Attributes

    private fun fillAchievements(achievements: List<Achievement>?) {
        if (achievements == null) {
            return
        }
        val items = ArrayList<Any>()

        fillAchievements(R.string.basic_achievements, achievements.filter { it.category == "basic" }, items)
        fillAchievements(R.string.seasonal_achievements, achievements.filter { it.category == "seasonal" }, items)
        fillAchievements(R.string.special_achievements, achievements.filter { it.category == "special" }, items)

        val adapter = AchievementProfileAdapter()
        adapter.setItemList(items)

        val layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)
        layoutManager.spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == 0) {
                    layoutManager.spanCount
                } else {
                    1
                }
            }
        }
        achievementGroupList.layoutManager = layoutManager
        achievementGroupList.adapter = adapter

        stopAndHideProgress(achievementProgress)
    }

    private fun fillAchievements(labelID: Int, achievements: List<Achievement>, targetList: MutableList<Any>) {
        // Order by ID first
        val achievementList = ArrayList(achievements)
        achievementList.sortWith(Comparator { achievement, t1 -> java.lang.Double.compare(achievement.index.toDouble(), t1.index.toDouble()) })

        targetList.add(getString(labelID))
        targetList.addAll(achievementList)
    }

    private fun stopAndHideProgress(bar: ProgressBar) {
        bar.isIndeterminate = false
        bar.visibility = View.GONE
    }

    private fun getFloorValueString(`val`: Float, roundDown: Boolean): String {
        return if (roundDown) {
            Math.floor(`val`.toDouble()).toString()
        } else {
            if (`val`.toDouble() == 0.0) {
                "0"
            } else {
                `val`.toString()
            }
        }
    }

    private fun getFloorValue(value: Float, roundDown: Boolean): Float {
        return if (roundDown) {
            Math.floor(value.toDouble()).toFloat()
        } else {
            value
        }
    }

    private fun addEquipmentRow(table: TableLayout, gearKey: String?, text: String, stats: String) {
        val gearRow = layoutInflater.inflate(R.layout.profile_gear_tablerow, table, false) as? TableRow

        val draweeView = gearRow?.findViewById<SimpleDraweeView>(R.id.gear_drawee)

        draweeView?.controller = Fresco.newDraweeControllerBuilder()
                .setUri(AvatarView.IMAGE_URI_ROOT + "shop_" + gearKey + ".png")
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onFailure(id: String?, throwable: Throwable?) {
                        draweeView?.visibility = View.GONE
                    }
                })
                .build()

        val keyTextView = gearRow?.findViewById<TextView>(R.id.tableRowTextView1)
        keyTextView?.text = text

        val valueTextView = gearRow?.findViewById<TextView>(R.id.tableRowTextView2)

        if (!stats.isEmpty()) {
            valueTextView?.text = stats
        } else {
            valueTextView?.visibility = View.GONE
        }

        table.addView(gearRow)

    }

    private fun addLevelAttributes(user: Member) {
        val byLevelStat = Math.min((user.stats?.lvl ?: 0) / 2.0f, 50f)

        addAttributeRow(getString(R.string.profile_level), byLevelStat, byLevelStat, byLevelStat, byLevelStat, true, false)
    }

    private fun loadItemDataByOutfit(outfit: Outfit?): Flowable<RealmResults<Equipment>> {
        val outfitList = ArrayList<String>()
        if (outfit != null) {
            outfitList.add(outfit.armor)
            outfitList.add(outfit.back)
            outfitList.add(outfit.body)
            outfitList.add(outfit.eyeWear)
            outfitList.add(outfit.head)
            outfitList.add(outfit.headAccessory)
            outfitList.add(outfit.shield)
            outfitList.add(outfit.weapon)
        }
        return inventoryRepository.getEquipment(outfitList)
    }

    private fun gotGear(equipmentList: List<Equipment>, user: Member) {
        val userStatComputer = UserStatComputer()
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)

        equipmentTableLayout.removeAllViews()
        for (index in 1 until attributesTableLayout.childCount) {
            attributesTableLayout.removeViewAt(index)
        }

        addLevelAttributes(user)

        for (row in statsRows) {
            if (row is UserStatComputer.EquipmentRow) {
                addEquipmentRow(equipmentTableLayout, row.gearKey, row.text, row.stats)
            } else if (row is UserStatComputer.AttributeRow) {
                addAttributeRow(getString(row.labelId), row.strVal, row.intVal, row.conVal, row.perVal, row.roundDown, row.isSummary)
            }
        }

        user.stats?.let { addNormalAddBuffAttributes(it) }
    }

    private fun gotCostume(obj: List<Equipment>) {
        // fill costume table
        costumeTableLayout.removeAllViews()
        for (i in obj) {
            addEquipmentRow(costumeTableLayout, i.key, i.text, "")
        }
    }

    private fun addNormalAddBuffAttributes(stats: Stats) {
        val buffs = stats.buffs

        addAttributeRow(getString(R.string.profile_allocated), stats.strength?.toFloat() ?: 0f, stats.intelligence?.toFloat() ?: 0f, stats.constitution?.toFloat() ?: 0f, stats.per?.toFloat() ?: 0f, true, false)
        addAttributeRow(getString(R.string.buffs), buffs?.getStr() ?: 0f, buffs?.get_int() ?: 0f, buffs?.getCon() ?: 0f, buffs?.getPer() ?: 0f, true, false)

        // Summary row
        addAttributeRow("", attributeStrSum, attributeIntSum, attributeConSum, attributePerSum, false, true)
    }

    private fun addAttributeRow(label: String, strVal: Float, intVal: Float, conVal: Float, perVal: Float, roundDown: Boolean, isSummary: Boolean) {
        val tableRow = layoutInflater.inflate(R.layout.profile_attributetablerow, attributesTableLayout, false) as? TableRow ?: return
        val keyTextView = tableRow.findViewById<TextView>(R.id.tv_attribute_type)
        keyTextView?.text = label

        val strTextView = tableRow.findViewById<TextView>(R.id.tv_attribute_str)
        strTextView?.text = getFloorValueString(strVal, roundDown)

        val intTextView = tableRow.findViewById<TextView>(R.id.tv_attribute_int)
        intTextView?.text = getFloorValueString(intVal, roundDown)

        val conTextView = tableRow.findViewById<TextView>(R.id.tv_attribute_con)
        conTextView?.text = getFloorValueString(conVal, roundDown)

        val perTextView = tableRow.findViewById<TextView>(R.id.tv_attribute_per)
        perTextView?.text = getFloorValueString(perVal, roundDown)


        if (isSummary) {
            strTextView?.setTypeface(null, Typeface.BOLD)
            intTextView?.setTypeface(null, Typeface.BOLD)
            conTextView?.setTypeface(null, Typeface.BOLD)
            perTextView?.setTypeface(null, Typeface.BOLD)
        } else {
            attributeStrSum += getFloorValue(strVal, roundDown)
            attributeIntSum += getFloorValue(intVal, roundDown)
            attributeConSum += getFloorValue(conVal, roundDown)
            attributePerSum += getFloorValue(perVal, roundDown)
            attributeRows.add(tableRow)
            tableRow.visibility = if (attributeDetailsHidden) View.GONE else View.VISIBLE
        }

        attributesTableLayout.addView(tableRow)

    }

    private fun toggleAttributeDetails() {
        attributeDetailsHidden = !attributeDetailsHidden

        attributesCollapseIcon.setImageDrawable(ContextCompat.getDrawable(this, if (attributeDetailsHidden)
            R.drawable.ic_keyboard_arrow_right_black_24dp
        else
            R.drawable.ic_keyboard_arrow_down_black_24dp))

        for (row in attributeRows) {
            row.visibility = if (attributeDetailsHidden) View.GONE else View.VISIBLE
        }
    }


    // endregion

    // region Navigation

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        finish()
    }

    // endregion

    // region BaseActivity-Overrides

    override fun getLayoutResId(): Int {
        return R.layout.activity_full_profile
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_full_profile, menu)
        return true
    }

    companion object {

        fun open(userId: String) {
            if (userId == "system") {
                return
            }
            val bundle = Bundle()
            bundle.putString("userID", userId)
            MainNavigationController.navigate(R.id.fullProfileActivity, bundle)
        }
    }

    // endregion

}
