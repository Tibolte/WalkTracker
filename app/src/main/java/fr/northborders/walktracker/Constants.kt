package fr.northborders.walktracker

object Constants {
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    const val NOTIFICATION_CHANNEL_ID = "trackmywalk_channel"
    const val NOTIFICATION_CHANNEL_NAME = "trackmywalk"
    const val NOTIFICATION_ID = 1

    const val TIMER_UPDATE_INTERVAL = 50L

    const val PREFERENCES_SERVICE = "preferences_service"
    const val SERVICE_STATE = "service_state"

    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    const val SMALLEST_DISPLACEMENT_100_METERS = 100F
    const val INTERVAL_TIME = 60
    const val FASTEST_INTERVAL_TIME = 30
}