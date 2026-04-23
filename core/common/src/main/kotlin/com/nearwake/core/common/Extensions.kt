package com.nearwake.core.common

fun String?.nullIfBlank(): String? =
    this?.takeIf { it.isNotBlank() }

fun Throwable.userMessage(fallback: String = "Something went wrong"): String =
    message.nullIfBlank() ?: fallback

fun <T> T?.orElse(fallback: () -> T): T =
    this ?: fallback()
