package com.gosyria.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gosyria.app.data.repository.AuthRepository
import com.gosyria.app.data.repository.HttpAuthRepository
import com.gosyria.app.data.repository.HttpRideRepository
import com.gosyria.app.data.repository.RideRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: HttpAuthRepository): AuthRepository

    @Binds @Singleton
    abstract fun bindRideRepository(impl: HttpRideRepository): RideRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    }
}
