package com.habitrpg.android.habitica.helpers

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import javax.inject.Inject

interface PerformanceMonitor {
    fun newTrace(name: String): PerformanceTrace
}

interface PerformanceTrace {
    fun start()

    fun stop()
}

class FirebasePerformanceMonitor
    @Inject
    constructor() : PerformanceMonitor {
        override fun newTrace(name: String): PerformanceTrace =
            try {
                FirebaseTrace(FirebasePerformance.getInstance().newTrace(name))
            } catch (ignored: IllegalStateException) {
                NoOpTrace
            }

        private class FirebaseTrace(
            private val trace: Trace,
        ) : PerformanceTrace {
            override fun start() = trace.start()

            override fun stop() = trace.stop()
        }

        private object NoOpTrace : PerformanceTrace {
            override fun start() = Unit

            override fun stop() = Unit
        }
    }
