package com.mygoal_healthtracker.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DateUtilsTest {
    @Test
    fun dailyRangeUsesSelectedDateOnly() {
        val range = dateRangeFor("2026-05-03", ProgressRange.Daily)

        assertEquals("2026-05-03", range.start)
        assertEquals("2026-05-03", range.end)
    }

    @Test
    fun monthlyRangeCoversWholeSelectedMonth() {
        val range = dateRangeFor("2026-05-03", ProgressRange.Monthly)

        assertEquals("2026-05-01", range.start)
        assertEquals("2026-05-31", range.end)
    }
}
