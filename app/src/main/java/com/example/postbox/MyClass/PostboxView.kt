package com.example.postbox.MyClass

import android.content.Context
import android.view.View

class PostboxView(context: Context): View(context) {
    var postboxData: PostboxData?
        get() {
            return postboxData
        }
        set(value) {
            this.postboxData = value
        }


    data class PostboxData(
        val stateID: Int,
        val name: String
    )
}