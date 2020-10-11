package com.rahul.rickandmorty.utils

import android.view.View


fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun isNumeric(string: String): Boolean {
    return string.matches("-?\\d+(\\.\\d+)?".toRegex())
}