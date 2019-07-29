package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.commands.FeedCommand
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import org.greenrobot.eventbus.EventBus

class PetDetailRecyclerAdapter(data: OrderedRealmCollection<Pet>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<Pet, PetDetailRecyclerAdapter.PetViewHolder>(data, autoUpdate) {

    var itemType: String? = null
    var context: Context? = null
    private var existingMounts: RealmResults<Mount>? = null
    private var ownedPets: Map<String, OwnedPet>? = null
    private var ownedMounts: Map<String, OwnedMount>? = null
    private val equipEvents = PublishSubject.create<String>()

    fun getEquipFlowable(): Flowable<String> {
        return equipEvents.toFlowable(BackpressureStrategy.DROP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        return PetViewHolder(parent.inflate(R.layout.pet_detail_item))
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        data?.let {
            holder.bind(it[position], ownedPets?.get(it[position]?.key ?: ""))
        }
    }

    fun setExistingMounts(existingMounts: RealmResults<Mount>) {
        this.existingMounts = existingMounts
        notifyDataSetChanged()
    }

    fun setOwnedMounts(ownedMounts: Map<String, OwnedMount>) {
        this.ownedMounts = ownedMounts
        notifyDataSetChanged()
    }

    fun setOwnedPets(ownedPets: Map<String, OwnedPet>) {
        this.ownedPets = ownedPets
        notifyDataSetChanged()
    }
    inner class PetViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var animal: Pet? = null
        var ownedPet: OwnedPet? = null

        private val imageView: SimpleDraweeView by bindView(R.id.imageView)
        private val titleView: TextView by bindView(R.id.titleTextView)
        private val trainedProgressbar: ProgressBar by bindView(R.id.trainedProgressBar)

        private val isOwned: Boolean
            get() = this.ownedPet?.trained ?: 0 > 0

        private val canRaiseToMount: Boolean
            get() {
                for (mount in existingMounts ?: emptyList<Mount>()) {
                    if (mount.key == animal?.key) {
                        return !(ownedMounts?.get(mount.key)?.owned ?: false)
                    }
                }
                return false
            }

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(item: Pet, ownedPet: OwnedPet?) {
            this.animal = item
            this.ownedPet = ownedPet
            if (item.color == "Veggie") {
                this.titleView.text = context?.getString(R.string.garden)
            } else {
                this.titleView.text = item.color
            }
            this.imageView.alpha = 1.0f
            val imageName = "Pet-$itemType-${item.color}"
            if (this.ownedPet?.trained ?: 0 > 0) {
                if (this.canRaiseToMount) {
                    this.trainedProgressbar.visibility = View.VISIBLE
                    this.trainedProgressbar.progress = ownedPet?.trained ?: 0
                } else {
                    this.trainedProgressbar.visibility = View.GONE
                }
            } else {
                this.trainedProgressbar.visibility = View.GONE
                this.imageView.alpha = 0.1f
            }
            imageView.background = null
            val trained = ownedPet?.trained ?: 0
            DataBindingUtils.loadImage(imageName) {
                val drawable = BitmapDrawable(context?.resources, if (trained  == 0) it.extractAlpha() else it)
                Observable.just(drawable)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Consumer {
                            imageView.background = drawable
                        }, RxErrorHandler.handleEmptyError())
            }
        }

        override fun onClick(v: View) {
            if (!this.isOwned) {
                return
            }
            val context = context ?: return
            val menu = BottomSheetMenu(context)
            menu.addMenuItem(BottomSheetMenuItem(itemView.resources.getString(R.string.use_animal)))
            if (canRaiseToMount) {
                menu.addMenuItem(BottomSheetMenuItem(itemView.resources.getString(R.string.feed)))
            }
            menu.setSelectionRunnable { index ->
                if (index == 0) {
                    animal?.let {
                        equipEvents.onNext(it.key)
                    }
                } else if (index == 1) {
                    val event = FeedCommand()
                    event.usingPet = animal
                    EventBus.getDefault().post(event)
                }
            }
            menu.show()
        }
    }
}
