package com.amp.sample.app

import android.app.Application
import android.util.Log

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        dummyMethod()
    }

    private fun dummyMethod() {
        Log.d("SampleLog", "This is a dummy method call")
    }
}