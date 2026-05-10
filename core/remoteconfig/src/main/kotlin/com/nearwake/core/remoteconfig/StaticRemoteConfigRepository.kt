package com.nearwake.core.remoteconfig

class StaticRemoteConfigRepository(
    private val thresholdConfig: ThresholdConfig = ThresholdConfig(),
) : RemoteConfigRepository {
    override fun currentThresholdConfig(): ThresholdConfig = thresholdConfig
}
