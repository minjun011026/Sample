package com.example.sample.di

import com.example.sample.FakeDeviceConnectionRepository
import com.example.sample.data.di.DataModule
import com.example.sample.data.repository.api.DeviceConnectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    replaces = [DataModule::class],
    components = [SingletonComponent::class]
)
interface FakeDataModule {

    @Binds
    fun bindDeviceConnectionRepository(impl: FakeDeviceConnectionRepository): DeviceConnectionRepository
}