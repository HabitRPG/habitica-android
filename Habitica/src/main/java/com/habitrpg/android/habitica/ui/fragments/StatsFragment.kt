package com.habitrpg.android.habitica.ui.fragments

import android.app.AlertDialog
import android.os.Build
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
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.extensions.setOkButton
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.UserStatComputer
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.stats.BulkAllocateStatsDialog
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_stats.*
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
        context.notNull {
            val color = ContextCompat.getColor(it, R.color.brand_200)
            distributeEvenlyHelpButton.setImageBitmap(HabiticaIconsHelper.imageOfInfoIcon(color))
            distributeClassHelpButton.setImageBitmap(HabiticaIconsHelper.imageOfInfoIcon(color))
            distributeTaskHelpButton.setImageBitmap(HabiticaIconsHelper.imageOfInfoIcon(color))
        }

        compositeSubscription.add(userRepository.getUser(userId).subscribe(Consumer {
            user = it
            unlock_at_level.visibility = if (user?.stats?.lvl ?: 0 < 10) View.VISIBLE else View.GONE
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
            userRepository.updateUser(user, "preferences.automaticAllocation", isChecked).subscribe(Consumer {}, RxErrorHandler.handleEmptyError())
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
                val lvl = user?.stats?.lvl
                if (lvl != null && lvl >= 10) {
                    showBulkAllocateDialog()
                }
            }
        }
    }

    private fun showBulkAllocateDialog() {
        context.notNull { context ->
            val dialog = BulkAllocateStatsDialog(context, HabiticaBaseApplication.component)
            dialog.show()
        }
    }

    private fun changeAutoAllocationMode(@Stats.AutoAllocationTypes allocationMode: String) {
        userRepository.updateUser(user, "preferences.allocationMode", allocationMode).subscribe(Consumer {}, RxErrorHandler.handleEmptyError())
        distributeEvenlyButton.isChecked = allocationMode == Stats.AUTO_ALLOCATE_FLAT
        distributeClassButton.isChecked = allocationMode == Stats.AUTO_ALLOCATE_CLASSBASED
        distributeTaskButton.isChecked = allocationMode == Stats.AUTO_ALLOCATE_TASKBASED

    }

    private fun showHelpAlert(resourceId: Int) {
        val builder = AlertDialog.Builder(context).setMessage(resourceId).setOkButton()
        builder.show()
    }

    private fun allocatePoint(@Stats.StatsTypes stat: String) {
        userRepository.allocatePoint(user, stat).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
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
        context.notNull { context ->
            if (canDistributePoints) {
                val points = user?.stats?.points ?: 0
                numberOfPointsTextView.text = getString(R.string.points_to_allocate, points)
                numberOfPointsTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    numberOfPointsTextView.background = ContextCompat.getDrawable(context, R.drawable.button_gray_100)
                }
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
        val levelStat = Math.min((currentUser.stats?.lvl ?: 0) / 2.0f, 50f).toInt()

        totalStrength = levelStat
        totalIntelligence = levelStat
        totalConstitution = levelStat
        totalPerception = levelStat

        strengthStatsView.levelValue = levelStat
        intelligenceStatsView.levelValue = levelStat
        constitutionStatsView.levelValue = levelStat
        perceptionStatsView.levelValue = levelStat

        totalStrength += currentUser.stats?.buffs?.getStr()?.toInt() ?: 0
        totalIntelligence += currentUser.stats?.buffs?.get_int()?.toInt() ?: 0
        totalConstitution += currentUser.stats?.buffs?.getCon()?.toInt() ?: 0
        totalPerception += currentUser.stats?.buffs?.getPer()?.toInt() ?: 0
        strengthStatsView.buffValue = currentUser.stats?.buffs?.getStr()?.toInt() ?: 0
        intelligenceStatsView.buffValue = currentUser.stats?.buffs?.get_int()?.toInt() ?: 0
        constitutionStatsView.buffValue = currentUser.stats?.buffs?.getCon()?.toInt() ?: 0
        perceptionStatsView.buffValue = currentUser.stats?.buffs?.getPer()?.toInt() ?: 0

        totalStrength += currentUser.stats?.str ?: 0
        totalIntelligence += currentUser.stats?._int ?: 0
        totalConstitution += currentUser.stats?.con ?: 0
        totalPerception += currentUser.stats?.per ?: 0
        strengthStatsView.allocatedValue = currentUser.stats?.str ?: 0
        intelligenceStatsView.allocatedValue = currentUser.stats?._int ?: 0
        constitutionStatsView.allocatedValue = currentUser.stats?.con ?: 0
        perceptionStatsView.allocatedValue = currentUser.stats?.per ?: 0

        val outfit = currentUser.items?.gear?.equipped
        val outfitList = ArrayList<String>()
        outfit.notNull { thisOutfit ->
            outfitList.add(thisOutfit.armor)
            outfitList.add(thisOutfit.back)
            outfitList.add(thisOutfit.body)
            outfitList.add(thisOutfit.eyeWear)
            outfitList.add(thisOutfit.head)
            outfitList.add(thisOutfit.headAccessory)
            outfitList.add(thisOutfit.shield)
            outfitList.add(thisOutfit.weapon)
        }

        inventoryRepository.getItems(outfitList).firstElement()
                .retry(1)
                .subscribe(Consumer {
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
