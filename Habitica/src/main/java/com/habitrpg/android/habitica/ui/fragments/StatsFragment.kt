package com.habitrpg.android.habitica.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.setOkButton
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.UserStatComputer
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.views.stats.BulkAllocateStatsDialog
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import kotlinx.android.synthetic.main.fragment_stats.*
import rx.functions.Action1
import java.util.*
import javax.inject.Inject
import javax.inject.Named


class StatsFragment: BaseMainFragment() {

    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    private var totalStrength = 0
        set(value) {
            field = value
            strengthStatsView.totalValue = value
        }
    private var totalIntelligence = 0
        set(value) {
            field = value
            intelligenceStatsView.totalValue = value
        }
    private var totalConstitution = 0
        set(value) {
            field = value
            constitutionStatsView.totalValue = value
        }
    private var totalPerception = 0
        set(value) {
            field = value
            perceptionStatsView.totalValue = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tutorialStepIdentifier = "stats"
        tutorialText = getString(R.string.tutorial_stats)

        super.onCreateView(inflater, container, savedInstanceState)
        hideToolbar()

        return container?.inflate(R.layout.fragment_stats)
    }

    override fun onDestroyView() {
        showToolbar()
        super.onDestroyView()
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leftSparklesView.setImageBitmap(HabiticaIconsHelper.imageOfAttributeSparklesLeft())
        rightSparklesView.setImageBitmap(HabiticaIconsHelper.imageOfAttributeSparklesRight())
        distributeEvenlyHelpButton.setImageBitmap(HabiticaIconsHelper.imageOfInfoIcon())
        distributeClassHelpButton.setImageBitmap(HabiticaIconsHelper.imageOfInfoIcon())
        distributeTaskHelpButton.setImageBitmap(HabiticaIconsHelper.imageOfInfoIcon())

        compositeSubscription.add(userRepository.getUser(userId).subscribe(Action1 {
            user = it
            updateStats()
            updateAttributePoints()
        }, RxErrorHandler.handleEmptyError()))

        distributeEvenlyButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                changeAutoAllocationMode(Stats.AUTO_ALLOCATE_FLAT)
            }
        }
        distributeClassButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                changeAutoAllocationMode(Stats.AUTO_ALLOCATE_CLASSBASED)
            }
        }
        distributeTaskButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                changeAutoAllocationMode(Stats.AUTO_ALLOCATE_TASKBASED)
            }
        }

        automaticAllocationSwitch.setOnCheckedChangeListener{ _, isChecked ->
            userRepository.updateUser(user, "preferences.automaticAllocation", isChecked).subscribe(Action1 {}, RxErrorHandler.handleEmptyError())
        }

        strengthStatsView.allocateAction = { allocatePoint(Stats.STRENGTH) }
        intelligenceStatsView.allocateAction = { allocatePoint(Stats.INTELLIGENCE) }
        constitutionStatsView.allocateAction = { allocatePoint(Stats.CONSTITUTION) }
        perceptionStatsView.allocateAction = { allocatePoint(Stats.PERCEPTION) }

        distributeEvenlyHelpButton.setOnClickListener { showHelpAlert(R.string.distribute_evenly_help) }
        distributeClassHelpButton.setOnClickListener { showHelpAlert(R.string.distribute_class_help) }
        distributeTaskHelpButton.setOnClickListener { showHelpAlert(R.string.distribute_task_help) }

        statsAllocationButton.setOnClickListener {
            if (user?.stats?.points ?: 0 > 0) {
                showBulkAllocateDialog()
            }
        }
    }

    private fun showBulkAllocateDialog() {
        val context = context
        if (context != null) {
            val dialog = BulkAllocateStatsDialog(context, HabiticaBaseApplication.getComponent())
            dialog.show()
        }
    }

    private fun changeAutoAllocationMode(@Stats.AutoAllocationTypes allocationMode: String) {
        userRepository.updateUser(user, "preferences.allocationMode", allocationMode).subscribe(Action1 {}, RxErrorHandler.handleEmptyError())
        distributeEvenlyButton.isChecked = allocationMode == Stats.AUTO_ALLOCATE_FLAT
        distributeClassButton.isChecked = allocationMode == Stats.AUTO_ALLOCATE_CLASSBASED
        distributeTaskButton.isChecked = allocationMode == Stats.AUTO_ALLOCATE_TASKBASED

    }

    private fun showHelpAlert(resourceId: Int) {
        val builder = AlertDialog.Builder(context).setMessage(resourceId).setOkButton()
        builder.show()
    }

    private fun allocatePoint(@Stats.StatsTypes stat: String) {
        userRepository.allocatePoint(user, stat).subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
    }

    private fun updateAttributePoints() {
        val automaticAllocation = user?.preferences?.automaticAllocation ?: false
        automaticAllocationSwitch.isChecked = automaticAllocation
        autoAllocationModeWrapper.visibility = if (automaticAllocation) View.VISIBLE else View.GONE

        val allocationMode = user?.preferences?.allocationMode ?: ""
        distributeEvenlyButton.isChecked = allocationMode == Stats.AUTO_ALLOCATE_FLAT
        distributeClassButton.isChecked = allocationMode == Stats.AUTO_ALLOCATE_CLASSBASED
        distributeTaskButton.isChecked = allocationMode == Stats.AUTO_ALLOCATE_TASKBASED

        val canDistributePoints = 0 < (user?.stats?.points ?: 0) && 10 <= (user?.stats?.lvl ?: 0)
        if (10 <= (user?.stats?.lvl ?: 0)) {
            automaticAllocationSwitch.visibility = View.VISIBLE
            automaticAllocationSwitch.visibility = View.VISIBLE
        } else {
            automaticAllocationSwitch.visibility = View.GONE
            automaticAllocationSwitch.visibility = View.GONE
        }
        strengthStatsView.canDistributePoints = canDistributePoints
        intelligenceStatsView.canDistributePoints = canDistributePoints
        constitutionStatsView.canDistributePoints = canDistributePoints
        perceptionStatsView.canDistributePoints = canDistributePoints
        val context = context
        if (context != null) {
            if (canDistributePoints) {
                val points = user?.stats?.points ?: 0
                numberOfPointsTextView.text = getString(R.string.points_to_allocate, points)
                numberOfPointsTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                numberOfPointsTextView.background = ContextCompat.getDrawable(context, R.drawable.button_gray_100)
                leftSparklesView.visibility = View.VISIBLE
                rightSparklesView.visibility = View.VISIBLE
            } else {
                numberOfPointsTextView.text = getString(R.string.no_points_to_allocate)
                numberOfPointsTextView.setTextColor(ContextCompat.getColor(context, R.color.gray_300))
                numberOfPointsTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                leftSparklesView.visibility = View.GONE
                rightSparklesView.visibility = View.GONE
            }
        }
        numberOfPointsTextView.setScaledPadding(context, 18, 4, 18, 4)
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun updateStats() {
        val currentUser = user ?: return
        val levelStat = Math.min(currentUser.stats.getLvl() / 2.0f, 50f).toInt()

        totalStrength = levelStat
        totalIntelligence = levelStat
        totalConstitution = levelStat
        totalPerception = levelStat

        strengthStatsView.levelValue = levelStat
        intelligenceStatsView.levelValue = levelStat
        constitutionStatsView.levelValue = levelStat
        perceptionStatsView.levelValue = levelStat

        totalStrength += currentUser.stats?.buffs?.str?.toInt() ?: 0
        totalIntelligence += currentUser.stats?.buffs?._int?.toInt() ?: 0
        totalConstitution += currentUser.stats?.buffs?.con?.toInt() ?: 0
        totalPerception += currentUser.stats?.buffs?.per?.toInt() ?: 0
        strengthStatsView.buffValue = currentUser.stats?.buffs?.str?.toInt() ?: 0
        intelligenceStatsView.buffValue = currentUser.stats?.buffs?._int?.toInt() ?: 0
        constitutionStatsView.buffValue = currentUser.stats?.buffs?.con?.toInt() ?: 0
        perceptionStatsView.buffValue = currentUser.stats?.buffs?.per?.toInt() ?: 0

        totalStrength += currentUser.stats?.str?.toInt() ?: 0
        totalIntelligence += currentUser.stats?._int?.toInt() ?: 0
        totalConstitution += currentUser.stats?.con?.toInt() ?: 0
        totalPerception += currentUser.stats?.per?.toInt() ?: 0
        strengthStatsView.allocatedValue = currentUser.stats?.str?.toInt() ?: 0
        intelligenceStatsView.allocatedValue = currentUser.stats?._int?.toInt() ?: 0
        constitutionStatsView.allocatedValue = currentUser.stats?.con?.toInt() ?: 0
        perceptionStatsView.allocatedValue = currentUser.stats?.per?.toInt() ?: 0

        val outfit = currentUser.items.gear.equipped
        val outfitList = ArrayList<String>()
        outfitList.add(outfit.armor)
        outfitList.add(outfit.back)
        outfitList.add(outfit.body)
        outfitList.add(outfit.eyeWear)
        outfitList.add(outfit.head)
        outfitList.add(outfit.headAccessory)
        outfitList.add(outfit.shield)
        outfitList.add(outfit.weapon)

        inventoryRepository.getItems(outfitList).first().subscribe(Action1 {
            val userStatComputer = UserStatComputer()
            val statsRows = userStatComputer.computeClassBonus(it, user)

            var strength = 0
            var intelligence = 0
            var constitution = 0
            var perception = 0

            for (row in statsRows) {
                if (row.javaClass == UserStatComputer.AttributeRow::class.java) {
                    val attributeRow = row as UserStatComputer.AttributeRow
                    strength += attributeRow.strVal.toInt()
                    intelligence += attributeRow.intVal.toInt()
                    constitution += attributeRow.conVal.toInt()
                    perception += attributeRow.perVal.toInt()
                }
            }

            totalStrength += strength
            totalIntelligence += intelligence
            totalConstitution += constitution
            totalPerception += perception
            strengthStatsView.equipmentValue = strength
            intelligenceStatsView.equipmentValue = intelligence
            constitutionStatsView.equipmentValue = constitution
            perceptionStatsView.equipmentValue = perception

        }, RxErrorHandler.handleEmptyError())
    }

    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.sidebar_stats)
    }

}
