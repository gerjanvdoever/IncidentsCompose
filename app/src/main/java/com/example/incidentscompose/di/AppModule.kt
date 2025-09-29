package com.example.incidentscompose.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.incidentscompose.data.api.AuthApi
import com.example.incidentscompose.data.repository.AuthRepository
import com.example.incidentscompose.data.store.TokenPreferences
import com.example.incidentscompose.viewmodel.LoginViewModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// DataStore extension property
private val Context.dataStore by preferencesDataStore("user_prefs")

val appModule = module {

    // DataStore wrapper
    single { TokenPreferences(androidContext()) }

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    single { AuthApi(get()) }  // inject HttpClient
    single { AuthRepository(get(), get()) } // TokenPreferences + AuthApi
    viewModel { LoginViewModel(get()) }

}
