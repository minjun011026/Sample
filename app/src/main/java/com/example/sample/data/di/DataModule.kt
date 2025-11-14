package com.example.sample.data.di

import com.example.sample.data.repository.api.DeviceConnectionRepository
import com.example.sample.data.repository.impl.DeviceConnectionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [InternalDataModule::class])
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindDeviceConnectionRepository(@Named("Internal") impl: DeviceConnectionRepository): DeviceConnectionRepository

}

@Module
@InstallIn(SingletonComponent::class)
internal interface InternalDataModule {

    @Binds
    @Named("Internal")
    @Singleton
    fun bindDeviceConnectionRepository(deviceConnectionRepositoryImpl: DeviceConnectionRepositoryImpl): DeviceConnectionRepository

}