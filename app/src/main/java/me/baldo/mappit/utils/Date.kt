package me.baldo.mappit.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale

fun Instant.getPrettyFormat(): String {
    return this.toLocalDateTime(TimeZone.currentSystemDefault())
        .let { dt ->
            val time = "%02d:%02d".format(dt.hour, dt.minute)
            val day = "%02d".format(dt.dayOfMonth)
            val month = dt.month.getDisplayName(
                TextStyle.SHORT,
                Locale.getDefault()
            )
            val year = "%02d".format(dt.year % 100)
            "$time · $day $month $year"
        }
}

fun Instant.getPrettyFormatDay(): String {
    return this.toLocalDateTime(TimeZone.currentSystemDefault())
        .let { dt ->
            val day = "%02d".format(dt.dayOfMonth)
            val month = dt.month.getDisplayName(
                TextStyle.SHORT,
                Locale.getDefault()
            )
            val year = "%02d".format(dt.year % 100)
            "$day $month $year"
        }
}
