package com.gosyria.app.di

import com.gosyria.app.data.mock.MockAuthRepository
import com.gosyria.app.data.mock.MockRideRepository
import com.gosyria.app.data.repository.AuthRepository
import com.gosyria.app.data.repository.RideRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: MockAuthRepository): AuthRepository

    @Binds @Singleton
    abstract fun bindRideRepository(impl: MockRideRepository): RideRepository
}
