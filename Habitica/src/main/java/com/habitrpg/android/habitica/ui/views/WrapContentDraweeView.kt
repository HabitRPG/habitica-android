package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.drawable.Animatable
import android.net.Uri
import android.util.AttributeSet
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo

//https://stackoverflow.com/questions/33955510/facebook-fresco-using-wrap-content
class WrapContentDraweeView : SimpleDraweeView {

    // we set a listener and update the view's aspect ratio depending on the loaded image
    private val listener = object : BaseControllerListener<ImageInfo>() {
        override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
        }

        override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)


    override fun setImageURI(uri: Uri, callerContext: Any?) {
        val controller = (controllerBuilder as PipelineDraweeControllerBuilder)
                .setControllerListener(listener)
                .setCallerContext(callerContext)
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .setOldController(controller)
                .build()
        setController(controller)
    }
}