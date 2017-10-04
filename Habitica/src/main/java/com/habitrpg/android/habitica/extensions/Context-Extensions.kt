package com.habitrpg.android.habitica.extensions

import android.app.Service
import android.content.Context
import android.view.LayoutInflater

val Context.layoutInflater: LayoutInflater
    get() = this.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater
