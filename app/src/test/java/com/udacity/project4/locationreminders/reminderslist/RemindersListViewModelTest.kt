package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val scenario = InstantTaskExecutorRule()

    private lateinit var reminderListViewModel: RemindersListViewModel
    private lateinit var reminderDataSource: FakeDataSource


    @Before
    fun setupReminderListViewModel() {
        reminderDataSource = FakeDataSource()
        val reminder1 = ReminderDTO("Title1", "Description1", "Shop1", 10.0, 10.0)
        val reminder2 = ReminderDTO("Title2", "Description2", "Shop2", 20.0, 20.0)
        val reminder3 = ReminderDTO("Title3", "Description3", "Shop3", 30.0, 30.0)
        reminderDataSource.addReminders(reminder1, reminder2, reminder3)

        reminderListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    @After
    fun cleanData() = runBlocking {
        reminderDataSource.deleteAllReminders()
        stopKoin()
    }

    @Test
    fun loadReminders_showLoading() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()

        assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()

        assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun getReminders_AllReminders()  {
        reminderListViewModel.loadReminders()

        val result = reminderListViewModel.remindersList.getOrAwaitValue()

        assertThat(result.size, `is`(3))
    }

    @Test
    fun getReminders_noReminders() = runBlockingTest {
       reminderDataSource.deleteAllReminders()

        reminderListViewModel.loadReminders()

        val result = reminderListViewModel.remindersList.getOrAwaitValue()

        assertThat(result.size, `is`(0))
    }

    @Test
    fun testInvalidateShowNoData() = runBlockingTest {
        reminderDataSource.deleteAllReminders()

        reminderListViewModel.loadReminders()

        assertThat(reminderListViewModel.showNoData.value == true, `is`(true))
    }

    @Test
    fun getReminders_returnError() {
        reminderDataSource.setReturnError(true)

        reminderListViewModel.loadReminders()

        val error = reminderListViewModel.showSnackBar.getOrAwaitValue()

        assertThat(error, `is`("Error"))


    }
}