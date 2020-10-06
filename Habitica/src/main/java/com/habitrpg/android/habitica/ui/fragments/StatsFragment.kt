package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentStatsBinding

import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.setScaledPadding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.UserStatComputer
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.stats.BulkAllocateStatsDialog
import io.reactivex.rxjava3.functions.Consumer
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.min


class StatsFragment: BaseMainFragment<FragmentStatsBinding>() {

    override var binding: FragmentStatsBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentStatsBinding {
        return FragmentStatsBinding.inflate(inflater, container, false)
    }

    private var canAllocatePoints: Boolean = false
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    private var totalStrength = 0
        set(value) {
            field = value
            binding?.strengthStatsView?.totalValue = value
        }
    private var totalIntelligence = 0
        set(value) {
            field = value
            binding?.intelligenceStatsView?.totalValue = value
        }
    private var totalConstitution = 0
        set(value) {
            field = value
            binding?.constitutionStatsView?.totalValue = value
        }
    private var totalPerception = 0
        set(value) {
            field = value
            binding?.perceptionStatsView?.totalValue = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        tutorialStepIdentifier = "stats"
        tutorialText = getString(R.string.tutorial_stats)
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.leftSparklesView?.setImageBitmap(HabiticaIconsHelper.imageOfAttributeSparklesLeft())
        binding?.rightSparklesView?.setImageBitmap(HabiticaIconsHelper.imageOfAttributeSparklesRight())
        context?.let {
            val color = it.getThemeColor(R.attr.colorPrimaryOffset)
            binding?.distributeEvenlyHelpButton?.setImageBitmap(HabiticaIconsHelper.imageOfInfoIcon(color))
            binding?. distributeClassHelpButton?.setImageBitmap(HabiticaIconsHelper.imageOfInfoIcon(color))
            binding?.distributeTaskHelpButton?.setImageBitmap(HabiticaIconsHelper.imageOfInfoIcon(color))
        }

        compositeSubscription.add(userRepository.getUser(userId).subscribe({
            canAllocatePoints = user?.stats?.lvl ?: 0 >= 10 && user?.stats?.points ?: 0 > 0
            binding?.unlockAtLevel?.visibility = if (it?.stats?.lvl ?: 0 < 10) View.VISIBLE else View.GONE
            updateStats(it)
            updateAttributePoints(it)
        }, RxErrorHandler.handleEmptyError()))

        binding?.distributeEvenlyButton?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                changeAutoAllocationMode(Stats.AUTO_ALLOCATE_FLAT)
            }
        }
        binding?.distributeClassButton?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                changeAutoAllocationMode(Stats.AUTO_ALLOCATE_CLASSBASED)
            }
        }
        binding?.distributeTaskButton?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                changeAutoAllocationMode(Stats.AUTO_ALLOCATE_TASKBASED)
            }
        }

        binding?.automaticAllocationSwitch?.setOnCheckedChangeListener{ _, isChecked ->
            userRepository.updateUser(user, "preferences.automaticAllocation", isChecked).subscribe({}, RxErrorHandler.handleEmptyError())
        }

        binding?.strengthStatsView?.allocateAction = { allocatePoint(Stats.STRENGTH) }
        binding?.intelligenceStatsView?.allocateAction = { allocatePoint(Stats.INTELLIGENCE) }
        binding?.constitutionStatsView?.allocateAction = { allocatePoint(Stats.CONSTITUTION) }
        binding?.perceptionStatsView?.allocateAction = { allocatePoint(Stats.PERCEPTION) }

        binding?.distributeEvenlyHelpButton?.setOnClickListener { showHelpAlert(R.string.distribute_evenly_help) }
        binding?.distributeClassHelpButton?.setOnClickListener { showHelpAlert(R.string.distribute_class_help) }
        binding?.distributeTaskHelpButton?.setOnClickListener { showHelpAlert(R.string.distribute_task_help) }

        binding?.statsAllocationButton?.setOnClickListener {
            if (canAllocatePoints) {
                showBulkAllocateDialog()
            }
        }
    }

    private fun showBulkAllocateDialog() {
        context?.let { context ->
            val dialog = BulkAllocateStatsDialog(context, HabiticaBaseApplication.userComponent)
            dialog.show()
        }
    }

    private fun changeAutoAllocationMode(allocationMode: String) {
        compositeSubscription.add(userRepository.updateUser(user, "preferences.allocationMode", allocationMode).subscribe({}, RxErrorHandler.handleEmptyError()))
        binding?.distributeEvenlyButton?.isChecked = allocationMode == Stats.AUTO_ALLOCATE_FLAT
        binding?.distributeClassButton?.isChecked = allocationMode == Stats.AUTO_ALLOCATE_CLASSBASED
        binding?.distributeTaskButton?.isChecked = allocationMode == Stats.AUTO_ALLOCATE_TASKBASED

    }

    private fun showHelpAlert(resourceId: Int) {
        val alert = context?.let { HabiticaAlertDialog(it) }
        alert?.setMessage(resourceId)
        alert?.addOkButton()
        alert?.show()
    }

    private fun allocatePoint(stat: String) {
        compositeSubscription.add(userRepository.allocatePoint(user, stat).subscribe({ }, RxErrorHandler.handleEmptyError()))
    }

    private fun updateAttributePoints(user: User) {
        val automaticAllocation = user.preferences?.automaticAllocation ?: false
        binding?.automaticAllocationSwitch?.isChecked = automaticAllocation
        binding?.autoAllocationModeWrapper?.visibility = if (automaticAllocation) View.VISIBLE else View.GONE

        val allocationMode = user.preferences?.allocationMode ?: ""
        binding?.distributeEvenlyButton?.isChecked = allocationMode == Stats.AUTO_ALLOCATE_FLAT
        binding?.distributeClassButton?.isChecked = allocationMode == Stats.AUTO_ALLOCATE_CLASSBASED
        binding?.distributeTaskButton?.isChecked = allocationMode == Stats.AUTO_ALLOCATE_TASKBASED

        val canDistributePoints = 0 < (user.stats?.points ?: 0) && 10 <= (user.stats?.lvl ?: 0)
        if (10 <= (user.stats?.lvl ?: 0)) {
            binding?.automaticAllocationSwitch?.visibility = View.VISIBLE
            binding?.automaticAllocationSwitch?.visibility = View.VISIBLE
        } else {
            binding?.automaticAllocationSwitch?.visibility = View.GONE
            binding?.automaticAllocationSwitch?.visibility = View.GONE
        }
        binding?.strengthStatsView?.canDistributePoints = canDistributePoints
        binding?.intelligenceStatsView?.canDistributePoints = canDistributePoints
        binding?.constitutionStatsView?.canDistributePoints = canDistributePoints
        binding?.perceptionStatsView?.canDistributePoints = canDistributePoints
        context?.let { context ->
            if (canDistributePoints) {
                val points = user.stats?.points ?: 0
                binding?.numberOfPointsTextView?.text = getString(R.string.points_to_allocate, points)
                binding?.numberOfPointsTextView?.setTextColor(ContextCompat.getColor(context, R.color.white))
                binding?.numberOfPointsTextView?.background = ContextCompat.getDrawable(context, R.drawable.button_gray_100)
                binding?.leftSparklesView?.visibility = View.VISIBLE
                binding?.rightSparklesView?.visibility = View.VISIBLE
            } else {
                binding?.numberOfPointsTextView?.text = getString(R.string.no_points_to_allocate)
                binding?.numberOfPointsTextView?.setTextColor(ContextCompat.getColor(context, R.color.text_quad))
                binding?.numberOfPointsTextView?.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
                binding?.leftSparklesView?.visibility = View.GONE
                binding?.rightSparklesView?.visibility = View.GONE
            }
        }
        binding?.numberOfPointsTextView?.setScaledPadding(context, 18, 4, 18, 4)
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun updateStats(currentUser: User) {
        val levelStat = min((currentUser.stats?.lvl ?: 0) / 2.0f, 50f).toInt()

        totalStrength = levelStat
        totalIntelligence = levelStat
        totalConstitution = levelStat
        totalPerception = levelStat

        binding?.strengthStatsView?.levelValue = levelStat
        binding?.intelligenceStatsView?.levelValue = levelStat
        binding?.constitutionStatsView?.levelValue = levelStat
        binding?.perceptionStatsView?.levelValue = levelStat

        totalStrength += currentUser.stats?.buffs?.str?.toInt() ?: 0
        totalIntelligence += currentUser.stats?.buffs?._int?.toInt() ?: 0
        totalConstitution += currentUser.stats?.buffs?.con?.toInt() ?: 0
        totalPerception += currentUser.stats?.buffs?.per?.toInt() ?: 0
        binding?.strengthStatsView?.buffValue = currentUser.stats?.buffs?.str?.toInt() ?: 0
        binding?.intelligenceStatsView?.buffValue = currentUser.stats?.buffs?._int?.toInt() ?: 0
        binding?.constitutionStatsView?.buffValue = currentUser.stats?.buffs?.con?.toInt() ?: 0
        binding?.perceptionStatsView?.buffValue = currentUser.stats?.buffs?.per?.toInt() ?: 0

        totalStrength += currentUser.stats?.strength ?: 0
        totalIntelligence += currentUser.stats?.intelligence ?: 0
        totalConstitution += currentUser.stats?.constitution ?: 0
        totalPerception += currentUser.stats?.per ?: 0
        binding?.strengthStatsView?.allocatedValue = currentUser.stats?.strength ?: 0
        binding?.intelligenceStatsView?.allocatedValue = currentUser.stats?.intelligence ?: 0
        binding?.constitutionStatsView?.allocatedValue = currentUser.stats?.constitution ?: 0
        binding?.perceptionStatsView?.allocatedValue = currentUser.stats?.per ?: 0

        val outfit = currentUser.items?.gear?.equipped
        val outfitList = ArrayList<String>()
        outfit?.let { thisOutfit ->
            outfitList.add(thisOutfit.armor)
            outfitList.add(thisOutfit.back)
            outfitList.add(thisOutfit.body)
            outfitList.add(thisOutfit.eyeWear)
            outfitList.add(thisOutfit.head)
            outfitList.add(thisOutfit.headAccessory)
            outfitList.add(thisOutfit.shield)
            outfitList.add(thisOutfit.weapon)
        }

        compositeSubscription.add(inventoryRepository.getEquipment(outfitList).firstElement()
                .retry(1)
                .subscribe(Consumer {
            val userStatComputer = UserStatComputer()
            val statsRows = user?.let { it1 -> userStatComputer.computeClassBonus(it, it1) } ?: return@Consumer

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
                    binding?.strengthStatsView?.equipmentValue = strength
                    binding?.intelligenceStatsView?.equipmentValue = intelligence
                    binding?.constitutionStatsView?.equipmentValue = constitution
                    binding?.perceptionStatsView?.equipmentValue = perception

        }, RxErrorHandler.handleEmptyError()))
    }

}
