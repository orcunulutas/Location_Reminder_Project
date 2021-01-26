package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    private lateinit var reminder: ReminderDTO
    private lateinit var reminder2: ReminderDTO

    @Before
    fun initDb() = runBlockingTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        reminder = ReminderDTO("Title1", "Description1", "Shop1", 10.0, 10.0)
        reminder2 = ReminderDTO("Title2", "Description2", "Shop2", 20.0, 20.0)

        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun saveReminders_getReminders() = runBlockingTest {
        val list = database.reminderDao().getReminders()

        assertThat(list[0], `is`(reminder))
        assertThat(list[1], `is`(reminder2))
    }

    @Test
    fun saveReminder_getReminderById() = runBlockingTest {
        val result = database.reminderDao().getReminderById(reminder.id)

        assertThat(result?.title, `is`(reminder.title))
        assertThat(result?.description, `is`(reminder.description))
        assertThat(result?.location, `is`(reminder.location))
        assertThat(result?.latitude, `is`(reminder.latitude))
        assertThat(result?.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveReminders_deleteReminders() = runBlockingTest {
        database.reminderDao().deleteAllReminders()

        val result = database.reminderDao().getReminders()

        assertThat(result.isEmpty(), `is`(true))
    }


//    TODO: Add testing implementation to the RemindersDao.kt

}