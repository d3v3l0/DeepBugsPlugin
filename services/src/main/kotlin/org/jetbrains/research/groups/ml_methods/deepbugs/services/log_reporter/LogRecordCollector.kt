package org.jetbrains.research.groups.ml_methods.deepbugs.services.log_reporter

import kotlin.concurrent.thread

class LogRecordCollector {
    private val collectedReports: MutableList<String> = mutableListOf()
    private var size: Int = 0
    private var lastSendingThread: Thread? = null

    fun appendReport(report: String) {
        if (size + report.length + System.lineSeparator().length > MAX_SIZE_BYTE) {
            dump()
        }
        size += report.length + System.lineSeparator().length
        collectedReports.add(report)
    }

    fun dump() {
        val reportsToSent = collectedReports.toList()
        collectedReports.clear()
        size = 0
        lastSendingThread = thread {
            //TODO: change to DeepBugsLogReporter.send
            TestDeepBugsLogReporter.send(reportsToSent.joinToString(System.lineSeparator()))
        }
    }

    companion object {
        private const val MAX_SIZE_BYTE = 250 * 1024
    }
}