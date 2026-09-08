package com.nearwake.core.network

import okhttp3.logging.HttpLoggingInterceptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class NearWakeHttpClientTest {
    @Test
    fun `debug builds do not log URLs containing location and credentials`() {
        val client = NearWakeHttpClient.create(isDebug = true)

        val loggingInterceptor = client.interceptors.firstOrNull { interceptor ->
            interceptor is HttpLoggingInterceptor
        } as? HttpLoggingInterceptor

        assertNotNull(loggingInterceptor)
        assertEquals(HttpLoggingInterceptor.Level.NONE, loggingInterceptor?.level)
    }

    @Test
    fun `create leaves logging off when debug is disabled`() {
        val client = NearWakeHttpClient.create(isDebug = false)

        assertFalse(client.interceptors.any { interceptor -> interceptor is HttpLoggingInterceptor })
        assertEquals(20_000, client.connectTimeoutMillis)
        assertEquals(30_000, client.callTimeoutMillis)
    }
}
