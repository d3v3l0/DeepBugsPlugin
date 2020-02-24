package org.jetbrains.research.deepbugs.python.ide.inspections.base

import com.intellij.codeInspection.*
import com.jetbrains.python.inspections.PyInspection
import com.jetbrains.python.inspections.PyInspectionVisitor
import com.jetbrains.python.psi.PyElement
import org.jetbrains.research.deepbugs.common.datatypes.DataType
import org.jetbrains.research.deepbugs.common.ide.fus.collectors.counter.InspectionReportCollector
import org.jetbrains.research.deepbugs.common.ide.problem.BugDescriptor
import org.jetbrains.research.deepbugs.python.PyDeepBugsConfig
import org.jetbrains.research.deepbugs.python.ide.quickfixes.PyIgnoreExpressionQuickFix
import org.jetbrains.research.keras.runner.nn.model.sequential.Perceptron

abstract class PyDeepBugsBaseInspection<T : PyElement, in V : DataType>(private val threshold: Float) : PyInspection() {
    protected abstract val model: Perceptron?

    protected open fun skip(node: T): Boolean = false

    protected open fun createProblemDescriptor(node: T, data: V): ProblemDescriptor =
        BugDescriptor(node, createTooltip(node), listOf(PyIgnoreExpressionQuickFix(data, node.text)))

    protected abstract fun createTooltip(node: T, vararg params: String): String

    abstract inner class PyDeepBugsVisitor(
        holder: ProblemsHolder,
        session: LocalInspectionToolSession
    ) : PyInspectionVisitor(holder, session) {
        protected fun visit(node: T, collect: T.() -> V?) {
            if (skip(node)) return
            val data = node.collect() ?: return
            val result = model?.predict(data.vectorize() ?: return) ?: return
            analyzeInspected(result, node, data)
        }

        private fun analyzeInspected(result: Float, node: T, data: V) {
            if (!PyDeepBugsConfig.isProblem(result, threshold, data)) return
            holder?.registerProblem(createProblemDescriptor(node, data))
            InspectionReportCollector.logReport(holder?.project ?: return, shortName, result)
        }
    }
}
