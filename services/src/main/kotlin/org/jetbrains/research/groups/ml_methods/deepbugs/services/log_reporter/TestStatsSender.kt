package org.jetbrains.research.groups.ml_methods.deepbugs.services.log_reporter

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.HttpRequests

//simple test localhost logger
object TestStatsSender {
    private const val reportUrl = "http://localhost:3000/test/reported"
    private val LOG = Logger.getInstance(TestStatsSender::class.java)

    fun send(text: String): Boolean {
        try {
            executeRequest(text)
            return true
        } catch (e: Exception) {
            LOG.debug(e)
        }
        return false
    }

    private fun executeRequest(text: String) {
        HttpRequests.post(reportUrl, "text/html").write(text)
    }
}