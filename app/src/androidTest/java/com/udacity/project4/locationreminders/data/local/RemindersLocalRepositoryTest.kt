package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createRepo() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        remindersLocalRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Unconfined)
        runBlocking {
            remindersLocalRepository.deleteAllReminders()
        }
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun insertReminderAndGetById() = runTest(UnconfinedTestDispatcher()) {
        // given inserting a reminder
        val reminder = ReminderDTO("title", "desc.", "here", 68.9, 68.9)
        remindersLocalRepository.saveReminder(reminder)
        // when getting the reminder by id
        val loadedReminder = (remindersLocalRepository.getReminder(reminder.id) as Result.Success).data
        // then the loaded reminder is the same as the inserted reminder
        assertEquals(true, loadedReminder == reminder)
    }

    @Test
    fun getReminderReturnError() = runTest(UnconfinedTestDispatcher()) {
        // given delete all reminders
        remindersLocalRepository.deleteAllReminders()
        // when get reminder by id
        val result = remindersLocalRepository.getReminder("foo")
        // then should return error
        assertEquals(Result.Error("Reminder not found!"), result)
    }



}