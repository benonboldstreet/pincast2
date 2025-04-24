package com.example.pincast.data.models

import java.time.LocalDateTime

/**
 * Represents the status of an IPFS gateway
 */
enum class GatewayStatus {
    ONLINE,
    OFFLINE,
    SLOW,
    UNKNOWN
}

/**
 * Information about an IPFS gateway including its status and performance
 */
data class GatewayInfo(
    val url: String,
    val name: String,
    val status: GatewayStatus = GatewayStatus.UNKNOWN,
    val responseTimeMs: Long = 0,
    val lastTestedAt: LocalDateTime? = null
) 