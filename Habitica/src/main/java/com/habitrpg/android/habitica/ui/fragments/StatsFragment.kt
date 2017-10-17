package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.UserStatComputer
import com.habitrpg.android.habitica.modules.AppModule
import kotlinx.android.synthetic.main.fragment_stats.*
import rx.functions.Action1
import java.util.ArrayList
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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater?.inflate(R.layout.fragment_stats, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        compositeSubscription.add(userRepository.getUser(userId).subscribe(Action1 {
            user = it
            updateStats()
        }, RxErrorHandler.handleEmptyError()))
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun updateStats() {
        val currentUser = user ?: return
        val levelStat = Math.min(currentUser.stats.getLvl()!! / 2.0f, 50f).toInt()

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

        inventoryRepository.getItems(outfitList).subscribe(Action1 {
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
            strengthStatsView.equipmentValue += strength
            intelligenceStatsView.equipmentValue += intelligence
            constitutionStatsView.equipmentValue += constitution
            perceptionStatsView.equipmentValue += perception

        }, RxErrorHandler.handleEmptyError())
    }

    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.sidebar_stats)
    }

}