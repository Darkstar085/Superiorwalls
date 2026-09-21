package com.sipun.superiorwalls.features.details

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {
    @Test
    fun `formats bytes as bytes below one kilobyte`() {
        assertEquals("512 B", formatBytes(512))
    }

    @Test
    fun `formats kilobytes without unnecessary decimals`() {
        assertEquals("512 KB", formatBytes(512L * 1024L))
    }

    @Test
    fun `formats megabytes with one decimal`() {
        assertEquals("1.5 MB", formatBytes(1536L * 1024L))
    }
}
