package com.habitrpg.android.habitica.extensions

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import androidx.annotation.IdRes
import android.view.View
import com.habitrpg.android.habitica.R

public fun AlertDialog.Builder.setOkButton(listener: ((DialogInterface, Int) -> Unit)? = null): AlertDialog.Builder {
    this.setPositiveButton(R.string.ok, { dialog, which ->
        listener?.invoke(dialog, which)
    })
    return this
}

public fun AlertDialog.Builder.setCloseButton(listener: ((DialogInterface, Int) -> Unit)? = null): AlertDialog.Builder {
    this.setPositiveButton(R.string.close, { dialog, which ->
        listener?.invoke(dialog, which)
    })
    return this
}

public fun AlertDialog.setOkButton(whichButton: Int = AlertDialog.BUTTON_POSITIVE, listener: ((DialogInterface, Int) -> Unit)? = null) {
    this.setButton(whichButton, context.getString(R.string.ok), { dialog, which ->
        listener?.invoke(dialog, which)
    })
}

public fun AlertDialog.setCloseButton(whichButton: Int = AlertDialog.BUTTON_POSITIVE, listener: ((DialogInterface, Int) -> Unit)? = null) {
    this.setButton(whichButton, context.getString(R.string.close), { dialog, which ->
        listener?.invoke(dialog, which)
    })
}