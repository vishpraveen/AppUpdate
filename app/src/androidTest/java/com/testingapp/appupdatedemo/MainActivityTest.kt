package com.testingapp.appupdatedemo

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var fakeAppUpdateManager: FakeAppUpdateManager

    @Before
    fun setUp() {
        fakeAppUpdateManager = FakeAppUpdateManager(
            ApplicationProvider.getApplicationContext<MainActivity>()
        )
    }

    @Test
    fun testFlexibleUpdateComplete() {
        //setup Flexible update
        fakeAppUpdateManager.partiallyAllowedUpdateType = AppUpdateType.FLEXIBLE
        fakeAppUpdateManager.setUpdateAvailable(2)

        ActivityScenario.launch(MainActivity::class.java)

        // Simulate user's and download behavior.
        fakeAppUpdateManager.userAcceptsUpdate()

        fakeAppUpdateManager.downloadStarts()

        fakeAppUpdateManager.downloadCompletes()

        fakeAppUpdateManager.installCompletes()
    }

    @Test
    fun testImmediateUpdateComplete() {
        //setup Flexible update
        fakeAppUpdateManager.partiallyAllowedUpdateType = AppUpdateType.IMMEDIATE
        fakeAppUpdateManager.setUpdateAvailable(2)

        ActivityScenario.launch(MainActivity::class.java)

        // Simulate user's and download behavior.
        fakeAppUpdateManager.userAcceptsUpdate()

        fakeAppUpdateManager.downloadStarts()

        fakeAppUpdateManager.downloadCompletes()

        fakeAppUpdateManager.installCompletes()
    }

    @Test
    fun testFlexibleUpdateUserDeny() {
        //setup Flexible update
        fakeAppUpdateManager.partiallyAllowedUpdateType = AppUpdateType.FLEXIBLE
        fakeAppUpdateManager.setUpdateAvailable(2)

        ActivityScenario.launch(MainActivity::class.java)

        // Simulate user's and download behavior.
        fakeAppUpdateManager.userRejectsUpdate()

        fakeAppUpdateManager.downloadFails()
    }

    @Test
    fun testFlexibleUpdateUserCancelDownload() {
        //setup Flexible update
        fakeAppUpdateManager.partiallyAllowedUpdateType = AppUpdateType.FLEXIBLE
        fakeAppUpdateManager.setUpdateAvailable(2)

        ActivityScenario.launch(MainActivity::class.java)

        // Simulate user's and download behavior.
        fakeAppUpdateManager.userAcceptsUpdate()

        fakeAppUpdateManager.downloadStarts()

        fakeAppUpdateManager.userCancelsDownload()

        fakeAppUpdateManager.downloadFails()

    }

}