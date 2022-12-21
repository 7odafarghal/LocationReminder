package com.udacity.project4.locationreminders.savereminder

import android.os.Build.VERSION_CODES.S
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [S])
class SaveReminderViewModelTest {

    private lateinit var viewModel: SaveReminderViewModel
    private val emptyReminderItem = ReminderDataItem("", "", "", null, null)
    private val filledReminderItem = ReminderDataItem("Some Place", "", "Here", 68.9, 68.9)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        // given a view model with a data source
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource())
    }

    @After
    fun tearDown() {
        GlobalContext.stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun check_loading() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        // when calling saveReminder
        viewModel.saveReminder(filledReminderItem)
        // then should show loading indicator
        assertEquals(true, viewModel.showLoading.getOrAwaitValue())
        // then should remove loading indicator
        advanceUntilIdle()
        assertEquals(false, viewModel.showLoading.getOrAwaitValue())
    }

    @Test
    fun validateAndSaveReminder_ShouldShowErrorSnackBarWhenSavingEmptyReminder() = runTest {
        // when calling saveReminder on an empty reminder item
        viewModel.validateAndSaveReminder(emptyReminderItem)
        // then should show error Snack bar
        viewModel.showSnackBarInt.getOrAwaitValue()
        //test passes without assertions and fails if errorStatus is never set
    }

    @Test
    fun saveReminder_shouldResetReminderItemLiveData() = runTest {
        // when saveReminder finishes excuting
        viewModel.saveReminder(filledReminderItem)
        // then reminder item live data objects should be null
        assertNull(viewModel.reminderTitle.value)
    }

}