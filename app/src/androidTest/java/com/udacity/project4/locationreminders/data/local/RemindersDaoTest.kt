package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runTest(UnconfinedTestDispatcher()) {
        // given inserting a reminder
        val reminder = ReminderDTO("title", "desc.", "here", 68.9, 68.9)
        database.reminderDao().saveReminder(reminder)
        // when getting the reminder by id from the database
        val loadedReminder = database.reminderDao().getReminderById(reminder.id)
        // then the loaded reminder is the same as the inserted reminder
        assertEquals(true, loadedReminder == reminder)
    }

    @Test
    fun getReminderReturnErrorNull() = runTest(UnconfinedTestDispatcher()) {
        // given delete all reminders
        database.reminderDao().deleteAllReminders()
        // when get reminder by id
        val reminder = database.reminderDao().getReminderById("foo")
        // then should return null
        assertEquals(null, reminder)
    }
}