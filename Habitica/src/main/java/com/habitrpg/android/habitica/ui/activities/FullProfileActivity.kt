package com.habitrpg.android.habitica.ui.activities

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityFullProfileBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.UserStatComputer
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.Outfit
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel
import com.habitrpg.android.habitica.ui.adapter.social.AchievementProfileAdapter
import com.habitrpg.common.habitica.helpers.RecyclerViewState
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.helpers.setMarkdown
import com.habitrpg.common.habitica.views.PixelArtView
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.min

class FullProfileActivity : BaseActivity() {
    private var blocks: List<String> = listOf()

    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var apiClient: ApiClient
    @Inject
    lateinit var socialRepository: SocialRepository

    private var userID = ""
    private var username: String? = null
    private var userDisplayName: String? = null
    private var attributeStrSum = 0f
    private var attributeIntSum = 0f
    private var attributeConSum = 0f
    private var attributePerSum = 0f
    private var attributeDetailsHidden = true
    private val attributeRows = ArrayList<TableRow>()
    private val dateFormatter = SimpleDateFormat.getDateInstance()
    private var avatarWithBars: AvatarWithBarsViewModel? = null
    private lateinit var binding: ActivityFullProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar(binding.toolbar)

        userID = intent?.extras?.getString("userID", "") ?: ""
        if (userID.isEmpty()) {
            userID = intent?.data?.path?.removePrefix("/profile/") ?: ""
        }

        setTitle(R.string.profile_loading_data)

        compositeSubscription.add(socialRepository.getMember(this.userID).subscribe({ this.updateView(it) }, RxErrorHandler.handleEmptyError()))
        avatarWithBars = AvatarWithBarsViewModel(this, binding.avatarWithBars)

        binding.avatarWithBars.root.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        binding.avatarWithBars.hpBar.barBackgroundColor = getThemeColor(R.color.window_background)
        binding.avatarWithBars.xpBar.barBackgroundColor = getThemeColor(R.color.window_background)
        binding.avatarWithBars.mpBar.barBackgroundColor = getThemeColor(R.color.window_background)

        attributeRows.clear()
        binding.attributesCardView.setOnClickListener { toggleAttributeDetails() }

        binding.sendMessageButton.setOnClickListener { showSendMessageToUserDialog() }
        binding.giftGemsButton.setOnClickListener { MainNavigationController.navigate(R.id.giftGemsActivity, bundleOf(Pair("userID", userID), Pair("username", null))) }
        binding.giftSubscriptionButton.setOnClickListener { MainNavigationController.navigate(R.id.giftSubscriptionActivity, bundleOf(Pair("userID", userID), Pair("username", null))) }
        compositeSubscription.add(
            userRepository.getUserFlowable().subscribe(
                {
                    blocks = it.inbox?.blocks ?: listOf()
                    binding.blockedDisclaimerView.visibility = if (isUserBlocked()) View.VISIBLE else View.GONE
                },
                RxErrorHandler.handleEmptyError()
            )
        )
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_full_profile, menu)
        val item = menu.findItem(R.id.block_user)
        if (isUserBlocked()) {
            item?.title = getString(R.string.unblock_user)
        } else {
            item?.title = getString(R.string.block)
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun isUserBlocked(): Boolean {
        return blocks.contains(userID)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.copy_username -> {
                val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText(username, username)
                clipboard?.setPrimaryClip(clip)
                HabiticaSnackbar.showSnackbar(
                    this@FullProfileActivity.binding.scrollView.getChildAt(0) as ViewGroup,
                    String.format(getString(R.string.username_copied), userDisplayName), SnackbarDisplayType.NORMAL
                )
                true
            }
            R.id.copy_userid -> {
                val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText(userID, userID)
                clipboard?.setPrimaryClip(clip)
                HabiticaSnackbar.showSnackbar(
                    this@FullProfileActivity.binding.scrollView.getChildAt(0) as ViewGroup,
                    String.format(getString(R.string.id_copied), userDisplayName), SnackbarDisplayType.NORMAL
                )
                true
            }
            R.id.block_user -> {
                if (blocks.contains(userID)) {
                    useBlock()
                } else {
                    showBlockDialog()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun useBlock() {
        compositeSubscription.add(
            socialRepository.blockMember(userID).flatMap {
                userRepository.retrieveUser()
            }.subscribe(
                {
                    invalidateOptionsMenu()
                },
                RxErrorHandler.handleEmptyError()
            )
        )
    }

    private fun showBlockDialog() {
        val dialog = HabiticaAlertDialog(this)
        dialog.setTitle(getString(R.string.block_user_title, userDisplayName))
        dialog.setMessage(R.string.block_user_description)
        dialog.addButton(R.string.block, true, true) { _, _ ->
            useBlock()
        }
        dialog.addCancelButton()
        dialog.show()
    }

    private fun showSendMessageToUserDialog() {
        finish()
        lifecycleScope.launch(context = Dispatchers.Main) {
            delay(1000L)
            MainNavigationController.navigate(R.id.inboxMessageListFragment, bundleOf(Pair("username", username), Pair("userID", userID)))
        }
    }

    private fun updateView(user: Member) {
        val profile = user.profile ?: return

        updatePetsMountsView(user)
        userDisplayName = profile.name
        username = user.username

        title = profile.name

        val imageUrl = profile.imageUrl
        if (imageUrl == null || imageUrl.isEmpty()) {
            binding.profileImage.visibility = View.GONE
        } else {
            //binding.profileImage.load(imageUrl)
        }

        val blurbText = profile.blurb
        if (blurbText != null && blurbText.isNotEmpty()) {
            binding.blurbTextView.setMarkdown(blurbText)
            binding.blurbTextView.movementMethod = LinkMovementMethod.getInstance()
        }

        user.authentication?.timestamps?.createdAt?.let { binding.joinedView.text = dateFormatter.format(it) }
        user.authentication?.timestamps?.lastLoggedIn?.let { binding.lastLoginView.text = dateFormatter.format(it) }
        binding.totalCheckinsView.text = user.loginIncentives.toString()

        avatarWithBars?.updateData(user)

        compositeSubscription.add(loadItemDataByOutfit(user.equipped).subscribe({ gear -> this.gotGear(gear, user) }, RxErrorHandler.handleEmptyError()))

        if (user.preferences?.costume == true) {
            compositeSubscription.add(loadItemDataByOutfit(user.costume).subscribe({ this.gotCostume(it) }, RxErrorHandler.handleEmptyError()))
        } else {
            binding.costumeCard.visibility = View.GONE
        }

        // Load the members achievements now
        compositeSubscription.add(socialRepository.getMemberAchievements(this.userID).subscribe({ this.fillAchievements(it) }, RxErrorHandler.handleEmptyError()))
    }

    private fun updatePetsMountsView(user: Member) {
        binding.petsFoundCount.text = user.petsFoundCount.toString()
        binding.mountsTamedCount.text = user.mountsTamedCount.toString()

        if (user.currentPet?.isNotBlank() == true) binding.currentPetDrawee.loadImage("Pet-" + user.currentPet)
        if (user.currentMount?.isNotBlank() == true) binding.currentMountDrawee.loadImage("Mount_Icon_" + user.currentMount)
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
        binding.achievementGroupList.layoutManager = layoutManager
        binding.achievementGroupList.adapter = adapter

        binding.achievementGroupList.state = RecyclerViewState.DISPLAYING_DATA
    }

    private fun fillAchievements(
        labelID: Int,
        achievements: List<Achievement>,
        targetList: MutableList<Any>
    ) {
        // Order by ID first
        val achievementList = ArrayList(achievements)
        achievementList.sortWith { achievement, t1 -> achievement.index.toDouble().compareTo(t1.index.toDouble()) }

        targetList.add(getString(labelID))
        targetList.addAll(achievementList)
    }

    private fun getFloorValueString(`val`: Float, roundDown: Boolean): String {
        return if (roundDown) {
            floor(`val`.toDouble()).toString()
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
            floor(value.toDouble()).toFloat()
        } else {
            value
        }
    }

    private fun addEquipmentRow(table: TableLayout, gearKey: String?, text: String?, stats: String?) {
        val gearRow = layoutInflater.inflate(R.layout.profile_gear_tablerow, table, false) as? TableRow

        val draweeView = gearRow?.findViewById<PixelArtView>(R.id.gear_drawee)

        draweeView?.loadImage("shop_$gearKey")

        val keyTextView = gearRow?.findViewById<TextView>(R.id.tableRowTextView1)
        keyTextView?.text = text

        val valueTextView = gearRow?.findViewById<TextView>(R.id.tableRowTextView2)

        if (stats?.isNotEmpty() == true) {
            valueTextView?.text = stats
        } else {
            valueTextView?.visibility = View.GONE
        }

        table.addView(gearRow)
    }

    private fun addLevelAttributes(user: Member) {
        val byLevelStat = min((user.stats?.lvl ?: 0) / 2.0f, 50f)
        addAttributeRow(
            getString(R.string.profile_level), byLevelStat, byLevelStat, byLevelStat, byLevelStat,
            roundDown = true,
            isSummary = false
        )
    }

    private fun loadItemDataByOutfit(outfit: Outfit?): Flowable<out List<Equipment>> {
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

        binding.equipmentTableLayout.removeAllViews()
        for (index in 1 until binding.attributesTableLayout.childCount) {
            if (binding.attributesTableLayout.getChildAt(index).isAttachedToWindow) {
                binding.attributesTableLayout.removeViewAt(index)
            }
        }

        addLevelAttributes(user)

        for (row in statsRows) {
            if (row is UserStatComputer.EquipmentRow) {
                addEquipmentRow(binding.equipmentTableLayout, row.gearKey, row.text, row.stats)
            } else if (row is UserStatComputer.AttributeRow) {
                addAttributeRow(getString(row.labelId), row.strVal, row.intVal, row.conVal, row.perVal, row.roundDown, row.summary)
            }
        }

        user.stats?.let { addNormalAddBuffAttributes(it) }
    }

    private fun gotCostume(obj: List<Equipment>) {
        // fill costume table
        binding.costumeTableLayout.removeAllViews()
        for (i in obj) {
            addEquipmentRow(binding.costumeTableLayout, i.key, i.text, "")
        }
    }

    private fun addNormalAddBuffAttributes(stats: Stats) {
        val buffs = stats.buffs

        addAttributeRow(
            getString(R.string.profile_allocated), stats.strength?.toFloat() ?: 0f, stats.intelligence?.toFloat() ?: 0f, stats.constitution?.toFloat() ?: 0f, stats.per?.toFloat() ?: 0f,
            roundDown = true,
            isSummary = false
        )
        addAttributeRow(
            getString(R.string.buffs),
            buffs?.str
                ?: 0f,
            buffs?._int ?: 0f, buffs?.con ?: 0f, buffs?.per ?: 0f, roundDown = true, isSummary = false
        )

        // Summary row
        addAttributeRow("", attributeStrSum, attributeIntSum, attributeConSum, attributePerSum, roundDown = false, isSummary = true)
    }

    private fun addAttributeRow(
        label: String,
        strVal: Float,
        intVal: Float,
        conVal: Float,
        perVal: Float,
        roundDown: Boolean,
        isSummary: Boolean
    ) {
        val tableRow = layoutInflater.inflate(R.layout.profile_attributetablerow, binding.attributesTableLayout, false) as? TableRow ?: return
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

        binding.attributesTableLayout.addView(tableRow)
    }

    private fun toggleAttributeDetails() {
        attributeDetailsHidden = !attributeDetailsHidden

        binding.attributesCollapseIcon.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                if (attributeDetailsHidden)
                    R.drawable.ic_keyboard_arrow_right_black_24dp
                else
                    R.drawable.ic_keyboard_arrow_down_black_24dp
            )
        )

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

    override fun getContentView(): View {
        binding = ActivityFullProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
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
