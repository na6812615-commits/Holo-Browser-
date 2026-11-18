package com.example

import com.example.model.SearchEngine
import com.example.model.UrlUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun urlDetection_worksCorrectly() {
        assertTrue(UrlUtils.isProbablyUrl("google.com"))
        assertTrue(UrlUtils.isProbablyUrl("https://vnexpress.net/tin-tuc"))
        assertTrue(UrlUtils.isProbablyUrl("localhost:8080"))
        assertFalse(UrlUtils.isProbablyUrl("thời tiết hôm nay"))
    }

    @Test
    fun normalizeInput_upgradesHttpWhenHttpsOnly() {
        val result = UrlUtils.normalizeInputToUrl(
            raw = "http://example.com",
            engine = SearchEngine.GOOGLE,
            httpsOnly = true
        )
        assertEquals("https://example.com", result)
    }

    @Test
    fun extractDomain_stripsWww() {
        assertEquals("google.com", UrlUtils.extractDomain("https://www.google.com/search?q=holo"))
    }
}
