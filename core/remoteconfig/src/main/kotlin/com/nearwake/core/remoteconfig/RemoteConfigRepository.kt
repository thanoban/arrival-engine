package com.nearwake.core.remoteconfig

interface RemoteConfigRepository {
    fun currentThresholdConfig(): ThresholdConfig
}
