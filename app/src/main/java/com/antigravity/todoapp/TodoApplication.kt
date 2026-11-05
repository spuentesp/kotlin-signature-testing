package com.antigravity.todoapp

import android.app.Application
import com.antigravity.todoapp.di.AppContainer
import com.antigravity.todoapp.di.DefaultAppContainer

/**
 * Base Application class holding the application-level dependency container.
 */
class TodoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
