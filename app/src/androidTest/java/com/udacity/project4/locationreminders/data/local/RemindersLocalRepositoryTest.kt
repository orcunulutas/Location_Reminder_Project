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
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    private val reminder = ReminderDTO("Title1", "Description1", "Shop1", 10.0, 10.0)
    private val reminder2 = ReminderDTO("Title2", "Description2", "Shop2", 10.0, 10.0)


    @Before
    fun initDb() = runBlocking() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remindersLocalRepository =
            RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
        remindersLocalRepository.saveReminder(reminder)
        remindersLocalRepository.saveReminder(reminder2)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun saveReminder_retrieveReminder() = runBlocking {
        remindersLocalRepository.saveReminder(reminder)

        val result = remindersLocalRepository.getReminder(reminder.id)

        if (result is Result.Success<ReminderDTO>) {
            val retrievedReminder = result.data

            assertThat(retrievedReminder, `is`(notNullValue()))
            assertThat(retrievedReminder.title, `is`(reminder.title))
            assertThat(retrievedReminder.description, `is`(reminder.description))
            assertThat(retrievedReminder.location, `is`(reminder.location))
            assertThat(retrievedReminder.latitude, `is`(reminder.latitude))
            assertThat(retrievedReminder.longitude, `is`(reminder.longitude))
        }
    }

    @Test
    fun deleteReminders_returnsEmpty()= runBlocking {
        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminders()

        result as Result.Success

        assertThat(result.data.isEmpty(), `is`(true))
    }

    @Test
    fun getReminders_returns0() = runBlocking {
        remindersLocalRepository.deleteAllReminders()

        val result = remindersLocalRepository.getReminders()

        result as Result.Success
        assertThat(result.data.size, `is`(0))
    }

    @Test
    fun getReminders_returns2() = runBlocking {
        val result = remindersLocalRepository.getReminders()

        result as Result.Success
        assertThat(result.data.size, `is`(2))
    }

    @Test
    fun noReminder_returnsError() = runBlocking {
        val result = remindersLocalRepository.getReminder("32")

        result as Result.Error

        assertThat(result.message, `is`("Reminder not found!"))
    }
}