package com.udacity.project4

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.MainKoinRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private val initialReminder = ReminderDTO("Some Place", "Desc", "Here", 68.9, 68.9)
    private val toBeAddedReminder = ReminderDTO("In the middle", "Of", "Nowhere", 0.0, 0.0)

    @get:Rule
    var mainKoinRule = MainKoinRule()

    @Before
    fun registerIdlingResource() {
        repository = get()
        runBlocking { repository.deleteAllReminders() }
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun checkErrorSnackBar() = runTest {
        // start main activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // click add reminder and add reminder title and description without location
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText(toBeAddedReminder.title))
        onView(withId(R.id.reminderDescription)).perform(replaceText(toBeAddedReminder.description))
        // click save should show error snack bar
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.select_location)))
        activityScenario.close()
    }

    @Test
    fun workflow() = runTest {
        // given initial state of the app
        repository.saveReminder(initialReminder)
        // start main activity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // check initial reminder exists
        onView(withId(R.id.title)).check(matches(withText(initialReminder.title)))
        onView(withId(R.id.description)).check(matches(withText(initialReminder.description)))
        onView(withId(R.id.location)).check(matches(withText(initialReminder.location)))
        // click on add reminder then click on add location
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())
        // clicking on save without selecting location should keep us in the fragment
        onView(withId(R.id.save_location_button)).perform(click())
        onView(withId(R.id.save_location_button)).check(matches(isDisplayed()))
        // press back and add reminder details without location
        pressBack()
        onView(withId(R.id.reminderTitle)).perform(replaceText(toBeAddedReminder.title))
        onView(withId(R.id.reminderDescription)).perform(replaceText(toBeAddedReminder.description))
        // click save should show error snack bar
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.select_location)))
        activityScenario.close()
    }

}
