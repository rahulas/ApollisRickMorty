package com.rahul.rickandmorty.app

import android.app.Application
import android.content.Context
import com.rahul.rickandmorty.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MainApplication : Application() {
    companion object {

        var mContext: MainApplication? = null

        fun getContext(): Context? {
            return mContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }


        mContext = this
    }

}