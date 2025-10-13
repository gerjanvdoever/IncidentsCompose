package com.example.incidentscompose

import android.app.Application
import com.example.incidentscompose.di.dataModule
import com.example.incidentscompose.di.networkModule
import com.example.incidentscompose.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(networkModule, dataModule, viewModelModule)
        }
    }
}
