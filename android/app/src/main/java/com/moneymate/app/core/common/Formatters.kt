package com.moneymate.app.core.common

// =============================================================================
// File: Formatters.kt
// Purpose: Formatting helpers for money, dates, labels, and other user-facing values.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import java.text.NumberFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale


// -----------------------------------------------------------------------------
// Section: money
// Purpose: Formatting/helper logic for money.
// -----------------------------------------------------------------------------
fun money(amount: Double, currencyCode: String): String {
    return runCatching {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.currency = Currency.getInstance(currencyCode)
        format.format(amount)
    }.getOrElse { "$currencyCode ${"%,.2f".format(amount)}" }
}


// -----------------------------------------------------------------------------
// Section: money
// Purpose: Formatting/helper logic for money.
// -----------------------------------------------------------------------------
fun money(amount: String, currencyCode: String): String = money(amount.toDoubleOrNull() ?: 0.0, currencyCode)


// -----------------------------------------------------------------------------
// Section: displayDate
// Purpose: Encapsulates the display Date section of this file.
// -----------------------------------------------------------------------------
fun displayDate(value: String?): String {
    if (value.isNullOrBlank()) return "—"
    return runCatching {
        val date = OffsetDateTime.parse(value.replace(" ", "T"))
        date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }.getOrElse { value.take(10) }
}


// -----------------------------------------------------------------------------
// Section: isoNow
// Purpose: Encapsulates the iso Now section of this file.
// -----------------------------------------------------------------------------
fun isoNow(): String = java.time.Instant.now().toString()
