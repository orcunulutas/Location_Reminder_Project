package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()


    @Before
    fun setupKoin() {
        appContext = getApplicationContext()

        stopKoin()
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            viewModel {
                AuthenticationViewModel()
            }

            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(getApplicationContext()) }
        }

        startKoin {
            modules(listOf(myModule))
        }

        repository = get()

        //start repository from scratch
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Test
    fun clickSaveReminder_navigateToSaveReminderFragment() {
        //GIVEN
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)


        val navController = mock(NavController::class.java)
        scenario.onFragment {
            it.view?.let { view -> Navigation.setViewNavController(view, navController) }
        }

        //WHEN
        onView(withId(R.id.addReminderFAB)).perform(click())

        //THEN - check if it navigates to next SaveReminderFragment
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

   @Test
    fun loadReminders_checkIfDisplayed() {

       val reminder1 = ReminderDTO("Title1", "Description1", "Shop1", 10.0, 10.0)

       runBlocking {
           repository.saveReminder(reminder1)
       }

       val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
       dataBindingIdlingResource.monitorFragment(scenario)


       onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

       onView(withText(reminder1.title)).check(matches(isDisplayed()))
       onView(withText(reminder1.title)).check(matches(isDisplayed()))
       onView(withText(reminder1.title)).check(matches(isDisplayed()))
    }

    @Test
    fun loadReminders_noDataShown() {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }
}