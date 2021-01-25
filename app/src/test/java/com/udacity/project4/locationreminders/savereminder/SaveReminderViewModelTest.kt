package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SaveReminderViewModel
    //TODO: provide testing to the SaveReminderView and its live data objects
    private lateinit var repository: FakeDataSource


    @Before
    fun setup() {
        repository = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), repository)
    }

    @After
    fun clear() = runBlockingTest {
        repository.deleteAllReminders()
    }

    @Test
    fun saveReminder_showLoading_showToast_setNavigate() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()

        val reminder = ReminderDataItem("Title1", "Description1", "Shop1", 10.0, 10.0)
        viewModel.saveReminder(reminder)

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
        assertThat(viewModel.navigate.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun saveReminder_checksIfSaved() = runBlockingTest {
        val reminder = ReminderDataItem("Title1", "Description1", "Shop1", 10.0, 10.0)
        viewModel.saveReminder(reminder)

        val result = repository.getReminder(reminder.id)
        if (result is Result.Success<ReminderDTO>) {
            val reminderDTO = result.data

            assertThat(reminderDTO.title, `is`("Title1"))
            assertThat(reminderDTO.description, `is`("Description1"))
            assertThat(reminderDTO.location, `is`("Shop1"))
            assertThat(reminderDTO.latitude, `is`(10.0))
            assertThat(reminderDTO.longitude, `is`(10.0))
        }

    }

}