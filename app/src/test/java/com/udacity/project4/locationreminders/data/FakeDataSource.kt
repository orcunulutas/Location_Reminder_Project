package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    var reminderServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if(shouldReturnError) {
          return Result.Error("Error", 0)
        }
        return Result.Success(reminderServiceData.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderServiceData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (!shouldReturnError) {
            reminderServiceData[id]?.let {
                return Result.Success(it)
            }
        }
        return Result.Error("Could not find reminder", 0)
    }

    override suspend fun deleteAllReminders() {
        reminderServiceData.clear()
    }

    fun addReminders(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            reminderServiceData[reminder.id] = reminder
        }
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


}