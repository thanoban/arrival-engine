package com.nearwake.domain.trip.engine

import com.nearwake.domain.routing.model.RouteSnapshot

enum class TransferCheckpointType {
    TRANSFER,
    DESTINATION,
}

enum class TransferProgressStatus {
    COMPLETED,
    SOON,
    UPCOMING,
}

data class TransferCheckpoint(
    val type: TransferCheckpointType,
    val stopName: String,
    val lineName: String? = null,
    val remainingMinutes: Int,
    val status: TransferProgressStatus,
)

data class TransferProgressState(
    val checkpoints: List<TransferCheckpoint>,
) {
    val nextTransfer: TransferCheckpoint?
        get() = checkpoints.firstOrNull { checkpoint ->
            checkpoint.type == TransferCheckpointType.TRANSFER &&
                checkpoint.status != TransferProgressStatus.COMPLETED
        }
}

class TransferMonitor {
    fun evaluate(
        routeSnapshot: RouteSnapshot,
        etaMinutes: Int,
        destinationName: String,
    ): TransferProgressState {
        val elapsedMinutes = (routeSnapshot.totalDurationMinutes - etaMinutes).coerceAtLeast(0)
        val transfers = routeSnapshot.transfers
            .sortedBy { transfer -> transfer.arrivalMinutes }
            .map { transfer ->
                val remainingMinutes = transfer.arrivalMinutes - elapsedMinutes
                TransferCheckpoint(
                    type = TransferCheckpointType.TRANSFER,
                    stopName = transfer.stop.name,
                    lineName = transfer.lineName,
                    remainingMinutes = remainingMinutes.coerceAtLeast(0),
                    status = when {
                        remainingMinutes < 0 -> TransferProgressStatus.COMPLETED
                        remainingMinutes <= SOON_WINDOW_MINUTES -> TransferProgressStatus.SOON
                        else -> TransferProgressStatus.UPCOMING
                    },
                )
            }

        val destinationCheckpoint = TransferCheckpoint(
            type = TransferCheckpointType.DESTINATION,
            stopName = destinationName,
            remainingMinutes = etaMinutes.coerceAtLeast(0),
            status = if (etaMinutes <= SOON_WINDOW_MINUTES) {
                TransferProgressStatus.SOON
            } else {
                TransferProgressStatus.UPCOMING
            },
        )

        return TransferProgressState(
            checkpoints = transfers + destinationCheckpoint,
        )
    }

    companion object {
        const val SOON_WINDOW_MINUTES = 3
    }
}
