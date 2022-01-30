package fr.northborders.walktracker.presentation

import android.content.SharedPreferences
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import fr.northborders.walktracker.R
import fr.northborders.walktracker.util.launchFragmentInHiltContainer
import io.mockk.every
import io.mockk.mockkClass
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*

@HiltAndroidTest
class PhotosFragmentTests {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun test_menu_item_service_started() {
        val mockNavController = mockkClass(NavController::class)
        val mockSharedPreferences = mockkClass(SharedPreferences::class)

        every { mockSharedPreferences.getString(any(), any()) } returns "Start"

        launchFragmentInHiltContainer<PhotosFragment>(
        ) {
            sharedPreferences = mockSharedPreferences
            Navigation.setViewNavController(requireView(), mockNavController)
        }

        onView(withId(R.id.action_start_stop)).check(matches(isDisplayed()))
        onView(withId(R.id.action_start_stop)).check(matches(withText("Stop")))
    }

    @Test
    fun test_menu_item_service_stopped() {
        val mockNavController = mockkClass(NavController::class)
        val mockSharedPreferences = mockkClass(SharedPreferences::class)

        every { mockSharedPreferences.getString(any(), any()) } returns "Stop"

        launchFragmentInHiltContainer<PhotosFragment>(
        ) {
            sharedPreferences = mockSharedPreferences
            Navigation.setViewNavController(requireView(), mockNavController)
        }

        onView(withId(R.id.action_start_stop)).check(matches(isDisplayed()))
        onView(withId(R.id.action_start_stop)).check(matches(withText("Start")))
    }
}