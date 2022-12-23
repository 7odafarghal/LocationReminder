package com.udacity.project4.locationreminders.reminderslist

import android.os.Build.VERSION_CODES.S
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [S])
class RemindersListViewModelTest {

    private val reminder1 = ReminderDTO("title", "desc", "here", 68.9, 68.9)
    private val reminder2 = ReminderDTO("titl", "des", "her", 68.8, 68.8)
    private val reminder3 = ReminderDTO("tit", "de", "he", 68.7, 68.7)
    private val sourceRemindersList = mutableListOf(reminder1, reminder2, reminder3)

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun check_loading() = runTest {
        // given a view model with a data source
        val viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource())
        // when loading
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        viewModel.loadReminders()
        // then should show loading indicator
        assertEquals(true, viewModel.showLoading.getOrAwaitValue())
        // and remove loading indicator after loading is finished
        advanceUntilIdle()
        assertEquals(false, viewModel.showLoading.getOrAwaitValue())
    }

    @Test
    fun shouldReturnError() = runTest {
        // given a view model with a corrupt data source
        val viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(shouldReturnError = true))
        // when loading reminders
        viewModel.loadReminders()
        // then should show error message
        viewModel.showSnackBar.getOrAwaitValue()
        //test passes without assertions and fails if errorStatus is never set
    }

    @Test
    fun loadReminder_hidesNoDataMessage_getsAllRemindersFromDataSource() = runTest {
        // given a view model with a non-empty data source
        val viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(sourceRemindersList))
        // when loading reminders
        viewModel.loadReminders()
        // the screen shouldn't show a no data message
        val showNoDataValue = viewModel.showNoData.getOrAwaitValue()
        assertEquals(false, showNoDataValue)
        // and the view model's reminders list should be the same size as the source reminders list
        val vMRemindersList = viewModel.remindersList.getOrAwaitValue()
        assertEquals(sourceRemindersList.size, vMRemindersList.size)
    }

    @Test
    fun loadReminder_showsNoDataMessage_hasEmptyListOfReminders() = runTest {
        // given a view model with an empty data source
        val viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource())
        // when loading reminders
        viewModel.loadReminders()
        // the screen shouldn't show a no data message
        val showNoDataValue = viewModel.showNoData.getOrAwaitValue()
        assertEquals(true, showNoDataValue)
        // and the view model's reminders list should be empty
        val vMRemindersList = viewModel.remindersList.getOrAwaitValue()
        assertEquals(mutableListOf<ReminderDTO>(), vMRemindersList)
    }

}