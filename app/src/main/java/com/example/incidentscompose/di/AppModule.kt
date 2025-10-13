package com.example.incidentscompose.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.incidentscompose.data.api.AuthApi
import com.example.incidentscompose.data.api.IncidentApi
import com.example.incidentscompose.data.api.UserApi
import com.example.incidentscompose.data.repository.AuthRepository
import com.example.incidentscompose.data.repository.IncidentRepository
import com.example.incidentscompose.data.repository.UserRepository
import com.example.incidentscompose.data.store.IncidentDataStore
import com.example.incidentscompose.data.store.TokenPreferences
import com.example.incidentscompose.viewmodel.LoginViewModel
import com.example.incidentscompose.viewmodel.MyIncidentViewModel
import com.example.incidentscompose.viewmodel.RegisterViewModel
import com.example.incidentscompose.viewmodel.ReportIncidentViewModel
import com.example.incidentscompose.viewmodel.UserViewModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore("user_prefs")

val appModule = module {

    single { TokenPreferences(androidContext()) }
    single { IncidentDataStore(androidContext())}

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    single { AuthApi(get()) }
    single { UserApi(get(), get()) }
    single { IncidentApi(get(), get())}

    single { AuthRepository(get(), get()) }
    single { UserRepository(get())}
    single { IncidentRepository(get())}

    viewModel { LoginViewModel(get()) }
    viewModel { MyIncidentViewModel(authRepository = get(), incidentRepository = get(), incidentDataStore = get(), userRepository = get())}
    viewModel { RegisterViewModel(get())}
    viewModel { ReportIncidentViewModel(get())}
    viewModel { UserViewModel(get())}

}
