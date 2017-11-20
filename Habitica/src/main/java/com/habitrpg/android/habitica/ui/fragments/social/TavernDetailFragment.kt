package com.habitrpg.android.habitica.ui.fragments.social

import android.graphics.Bitmap
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.BaseFragment

import javax.inject.Inject
import javax.inject.Named

import butterknife.OnClick
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.habitrpg.android.habitica.helpers.RemoteConfigManager
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import kotlinx.android.synthetic.main.shop_header.*
import kotlinx.android.synthetic.main.fragment_tavern_detail.*
import kotlinx.android.synthetic.main.stats_view.view.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1

class TavernDetailFragment : BaseFragment() {

    @Inject
    lateinit var userRepository: UserRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String
    @Inject
    lateinit var configManager: RemoteConfigManager


    private var user: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_tavern_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        compositeSubscription.add(userRepository.getUser(userId).subscribe(Action1 {
            this.user = it
            this.updatePausedState()
        }, RxErrorHandler.handleEmptyError()))

        descriptionView.setText(R.string.tavern_description)
        namePlate.setText(R.string.tavern_owner)

        DataBindingUtils.loadImage(sceneView, "tavern_scene" + configManager.shopSpriteSuffix())

        backgroundView.scaleType = ImageView.ScaleType.FIT_START

        val imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse("https://habitica-assets.s3.amazonaws.com/mobileApp/images/tavern_background" + configManager.shopSpriteSuffix() + ".png"))
                .build()

        val imagePipeline = Fresco.getImagePipeline()
        val dataSource = imagePipeline.fetchDecodedImage(imageRequest, this)

        dataSource.subscribe(object : BaseBitmapDataSubscriber() {
            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                dataSource?.close()
            }

            public override fun onNewResultImpl(bitmap: Bitmap?) {
                if (dataSource.isFinished && bitmap != null) {
                    val aspectRatio = bitmap.width / bitmap.height.toFloat()
                    val height = context?.resources?.getDimension(R.dimen.shop_height)?.toInt() ?: 0
                    val width = Math.round(height * aspectRatio)
                    val drawable = BitmapDrawable(context?.resources, Bitmap.createScaledBitmap(bitmap, width, height, false))
                    drawable.tileModeX = Shader.TileMode.REPEAT
                    Observable.just(drawable)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Action1 {
                                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                    backgroundView?.setBackgroundDrawable(it)
                                } else {
                                    backgroundView?.background = it
                                }}, RxErrorHandler.handleEmptyError())
                    dataSource.close()
                }
            }
        }, CallerThreadExecutor.getInstance())
    }

    private fun updatePausedState() {
        if (dailiesButton == null) {
            return
        }
        if (user?.preferences?.sleep == true) {
            dailiesButton .setText(R.string.tavern_inn_checkOut)
        } else {
            dailiesButton.setText(R.string.tavern_inn_rest)
        }
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    @OnClick(R.id.dailiesButton)
    fun dailiesButtonClicked() {
        user?.let { userRepository.sleep(it).subscribe(Action1 { }, RxErrorHandler.handleEmptyError()) }
    }
}
