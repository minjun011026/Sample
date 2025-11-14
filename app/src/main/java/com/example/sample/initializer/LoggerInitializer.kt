package com.example.sample.initializer

import android.content.Context
import androidx.startup.Initializer
import com.example.sample.BuildConfig
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class LoggerInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
