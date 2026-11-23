package com.habitrpg.android.habitica.extensions

import androidx.fragment.app.Fragment

fun Fragment.openBrowserLink(url: String) = requireActivity().openBrowserLink(url)
