package com.example.sample

import com.example.sample.data.repository.api.DeviceConnectionRepository
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class DeviceConnectionTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: DeviceConnectionRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `의존성_주입시_실제_구현체_대신_Fake_객체가_주입되는지_확인`() = runTest {
        assertThat(repository).isInstanceOf(FakeDeviceConnectionRepository::class.java)
    }

}