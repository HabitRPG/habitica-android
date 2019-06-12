package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.user.OwnedMount
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

class MountDetailRecyclerAdapter(data: OrderedRealmCollection<Mount>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<Mount, MountDetailRecyclerAdapter.MountViewHolder>(data, autoUpdate) {

    var itemType: String? = null
    var context: Context? = null
    private var ownedMounts: Map<String, OwnedMount>? = null

    private val equipEvents = PublishSubject.create<String>()


    fun getEquipFlowable(): Flowable<String> {
        return equipEvents.toFlowable(BackpressureStrategy.DROP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MountViewHolder {
        return MountViewHolder(parent.inflate(R.layout.animal_overview_item))
    }

    override fun onBindViewHolder(holder: MountViewHolder, position: Int) {
        data?.let { holder.bind(it[position], ownedMounts?.get(it[position].key)) }
    }

    fun setOwnedMounts(ownedMounts: Map<String, OwnedMount>) {
        this.ownedMounts = ownedMounts
        notifyDataSetChanged()
    }

    inner class MountViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var animal: Mount? = null
        private var ownedMount: OwnedMount? = null

        private val imageView: SimpleDraweeView by bindView(R.id.imageView)
        private val titleView: TextView by bindView(R.id.titleTextView)
        private val ownedTextView: TextView by bindView(R.id.ownedTextView)

        var resources: Resources = itemView.resources

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(item: Mount, ownedMount: OwnedMount?) {
            animal = item
            this.ownedMount = ownedMount
            titleView.text = item.color
            ownedTextView.visibility = View.GONE
            val imageName = "Mount_Icon_" + itemType + "-" + item.color
            this.imageView.alpha = 1.0f
            if (ownedMount?.owned != true) {
                this.imageView.alpha = 0.1f
            }
            imageView.background = null
            val owned = ownedMount?.owned ?: false
            DataBindingUtils.loadImage(imageName) {
                val drawable = BitmapDrawable(context?.resources, if (owned) it else it.extractAlpha())

                Observable.just(drawable)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Consumer {
                            imageView.background = drawable
                        }, RxErrorHandler.handleEmptyError())
            }
        }

        override fun onClick(v: View) {
            if (ownedMount?.owned == false) {
                return
            }
            val menu = BottomSheetMenu(itemView.context)
            menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.use_animal)))
            menu.setSelectionRunnable {
                animal?.let { equipEvents.onNext(it.key) }
            }
            menu.show()
        }
    }
}
