package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import butterknife.ButterKnife
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand
import com.habitrpg.android.habitica.extensions.backgroundCompat
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.ShopItemViewHolder
import kotlinx.android.synthetic.main.shop_header.*
import org.greenrobot.eventbus.EventBus
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1


class ShopRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: MutableList<Any> = ArrayList()
    private var shopIdentifier: String? = null
    private var ownedItems: Map<String, Item> = HashMap()


    var shopSpriteSuffix: String? = null
    set(value) {
        field = value
        notifyItemChanged(0)
    }
    var context: Context? = null
    var user: User? = null
    set(value) {
        field = value
        this.notifyDataSetChanged()
    }
    private var pinnedItemKeys: List<String> = ArrayList()

    var gearCategories: MutableList<ShopCategory> = ArrayList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    internal var selectedGearCategory: String = ""
    set(value) {
        field = value
        if (field != "") {
            notifyDataSetChanged()
        }
    }

    private val emptyViewResource: Int
        get() = when (this.shopIdentifier) {
            Shop.SEASONAL_SHOP -> R.layout.empty_view_seasonal_shop
            Shop.TIME_TRAVELERS_SHOP -> R.layout.empty_view_timetravelers
            else -> R.layout.simple_textview
        }

    fun setShop(shop: Shop?) {
        if (shop == null) {
            return
        }
        shopIdentifier = shop.identifier
        items.clear()
        items.add(shop)
        for (category in shop.categories) {
            if (category.items.size > 0) {
                items.add(category)
                for (item in category.items) {
                    item.categoryIdentifier = category.identifier
                    items.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                0 -> {
                    val view = parent.inflate(R.layout.shop_header)
                    ShopHeaderViewHolder(view)
                }
                1 -> {
                    val view = parent.inflate(R.layout.shop_section_header)
                    SectionViewHolder(view)
                }
                2 -> {
                    val view = parent.inflate(emptyViewResource)
                    EmptyStateViewHolder(view)
                }
                else -> {
                    val view = parent.inflate(R.layout.row_shopitem)
                    val viewHolder = ShopItemViewHolder(view)
                    viewHolder.shopIdentifier = shopIdentifier
                    viewHolder
                }
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val obj = getItem(position)
        if (obj != null) {
            when (obj.javaClass) {
                Shop::class.java -> (holder as ShopHeaderViewHolder).bind(obj as Shop, shopSpriteSuffix)
                ShopCategory::class.java -> {
                    val category = obj as ShopCategory
                    (holder as SectionViewHolder).bind((category).text)
                    if (gearCategories.contains(category)) {
                        val adapter = HabiticaClassArrayAdapter(context, R.layout.class_spinner_dropdown_item, gearCategories.map { it.identifier })
                        holder.spinnerAdapter = adapter
                        holder.selectedItem = gearCategories.indexOf(category)
                        holder.spinnerSelectionChanged = {
                            if (selectedGearCategory != gearCategories[holder.selectedItem].identifier) {
                                selectedGearCategory = gearCategories[holder.selectedItem].identifier
                            }
                        }
                        if (user?.stats?.habitClass != category.identifier) {
                            holder.notesView?.text = context?.getString(R.string.class_gear_disclaimer)
                            holder.notesView?.visibility = View.VISIBLE
                        } else {
                            holder.notesView?.visibility = View.GONE
                        }
                    } else {
                        holder.spinnerAdapter = null
                        holder.notesView?.visibility = View.GONE
                    }
                }
                ShopItem::class.java -> {
                    val item = obj as ShopItem
                    (holder as ShopItemViewHolder).bind(item, item.canAfford(user))
                    if (ownedItems.containsKey(item.key+"-"+item.pinType)) {
                        holder.itemCount = ownedItems[item.key+"-"+item.pinType]?.owned ?: 0
                    }
                    holder.isPinned = pinnedItemKeys.contains(item.key)
                }
                String::class.java -> (holder as EmptyStateViewHolder).text = obj as String
            }
        }
    }

    private fun getItem(position: Int): Any? {
        if (items.size == 0) {
            return null
        }
        if (position == 0) {
            return items[0]
        }
        if (position <= getGearItemCount()) {
            return when {
                position == 1 -> {
                    val category = getSelectedShopCategory()
                    category?.text = context?.getString(R.string.class_equipment) ?: ""
                    category
                }
                getSelectedShopCategory()?.items?.size ?: 0 <= position-2 -> return context?.getString(R.string.equipment_empty)
                else -> getSelectedShopCategory()?.items?.get(position-2)
            }
        } else {
            val itemPosition = position - getGearItemCount()
            if (itemPosition > items.size-1) {
                return null
            }
            return items[itemPosition]
        }
    }

    override fun getItemViewType(position: Int): Int = when(getItem(position)?.javaClass) {
        Shop::class.java -> 0
        ShopCategory::class.java -> 1
        ShopItem::class.java -> 3
        else -> 2
    }

    override fun getItemCount(): Int {
        val size = items.size + getGearItemCount()
        return if (size == 1) {
            2
        } else size
    }

    private fun getGearItemCount(): Int {
        return if (selectedGearCategory == "") {
            0
        } else {
            val selectedCategory: ShopCategory? = getSelectedShopCategory()
            return if (selectedCategory != null) {
                return if (selectedCategory.items.size == 0) {
                    2
                } else {
                    selectedCategory.items.size+1
                }
            } else {
                0
            }
        }
    }

    private fun getSelectedShopCategory() =
            gearCategories.firstOrNull { selectedGearCategory == it.identifier }

    fun setOwnedItems(ownedItems: Map<String, Item>) {
        this.ownedItems = ownedItems
        this.notifyDataSetChanged()
    }

    fun setPinnedItemKeys(pinnedItemKeys: List<String>) {
        this.pinnedItemKeys = pinnedItemKeys
        this.notifyDataSetChanged()
    }

    internal class ShopHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val context: Context
        private val descriptionView: TextView by bindView(itemView, R.id.descriptionView)
        private val sceneView: SimpleDraweeView by bindView(itemView, R.id.sceneView)
        private val backgroundView: ImageView by bindView(itemView, R.id.backgroundView)
        private val namePlate: TextView by bindView(itemView, R.id.namePlate)

        init {
            ButterKnife.bind(this, itemView)
            context = itemView.context
            descriptionView.movementMethod = LinkMovementMethod.getInstance()
        }

        fun bind(shop: Shop, shopSpriteSuffix: String?) {
            DataBindingUtils.loadImage(sceneView, shop.identifier + "_scene" + shopSpriteSuffix)

            backgroundView.scaleType = ImageView.ScaleType.FIT_START

            DataBindingUtils.loadImage(shop.identifier + "_background" + shopSpriteSuffix, {
                val aspectRatio = it.width / it.height.toFloat()
                val height = context.resources.getDimension(R.dimen.shop_height).toInt()
                val width = Math.round(height * aspectRatio)
                val drawable = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(it, width, height, false))
                drawable.tileModeX = Shader.TileMode.REPEAT
                Observable.just(drawable)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Action1 {
                            backgroundView.backgroundCompat = it
                        }, RxErrorHandler.handleEmptyError())
            })

            descriptionView.text = Html.fromHtml(shop.notes)
            namePlate.setText(shop.npcNameResource)
        }

    }

    class EmptyStateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val subscribeButton: Button? by bindView(itemView, R.id.subscribeButton)
        private val textView: TextView? by bindView(itemView, R.id.textView)
        init {
            ButterKnife.bind(this, view)
            subscribeButton?.setOnClickListener { EventBus.getDefault().post(OpenGemPurchaseFragmentCommand()) }
        }

        var text: String? = null
        set(value) {
            field = value
            textView?.text = field
        }
    }
}
