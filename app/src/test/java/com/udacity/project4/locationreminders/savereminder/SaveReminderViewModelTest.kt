package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
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

    private lateinit var repository: FakeDataSource

    private val reminder = ReminderDataItem("Title1", "Description1", "Shop1", 10.0, 10.0)


    @Before
    fun setup() {
        repository = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), repository)
    }

    @After
    fun clear() = runBlockingTest {
        repository.deleteAllReminders()
        stopKoin()
    }

    @Test
    fun saveReminder_showLoading_showToast_setNavigate() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()

        viewModel.saveReminder(reminder)

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
        assertThat(viewModel.navigate.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun saveReminder_checksIfSaved() = runBlockingTest {
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

    @Test
    fun onClear_returnsNull() {
        viewModel.onClear()

        assertThat(viewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.selectedPOI.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.navigate.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validateData_returnTrue() {
        val result = viewModel.validateEnteredData(reminder)

        assertThat(result, `is`(true))
    }

    @Test
    fun validateData_showSnackBarNoTitle() {
        reminder.title = ""

        viewModel.validateAndSaveReminder(reminder)
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateData_showSnackBarNoLocation() {
        reminder.location = ""

        viewModel.validateAndSaveReminder(reminder)
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

}