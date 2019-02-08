package org.jetbrains.research.groups.ml_methods.deepbugs.javascript.datatypes

import com.intellij.lang.javascript.JSRecursiveNodeVisitor
import com.intellij.lang.javascript.buildTools.JSPsiUtil
import com.intellij.lang.javascript.flex.JSResolveHelper
import com.intellij.lang.javascript.hierarchy.call.JSCalleeMethodsTreeStructure
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSFunctionDeclaration
import com.intellij.openapi.paths.PathReference
import org.jetbrains.research.groups.ml_methods.deepbugs.javascript.extraction.Extractor
import org.jetbrains.research.groups.ml_methods.deepbugs.services.models_manager.ModelsManager
import org.jetbrains.research.groups.ml_methods.deepbugs.services.datatypes.Call
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils
import com.intellij.lang.javascript.psi.resolve.processors.JSResolveProcessor
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.JSTreeUtil
import com.intellij.lang.javascript.psi.util.JSUtils
import com.intellij.util.ObjectUtils


class JSCall(callee: String,
             arguments: List<String>,
             argumentTypes: List<String>,
             base: String,
             parameters: List<String>,
             src: String) : Call(callee, arguments, argumentTypes, base, parameters, src) {

    companion object {

        /**
         * Extract information from [JSCallExpression] and build [JSCall].
         * @param node [JSCallExpression] that should be processed.
         * @return [JSCall] with collected information.
         */
        fun collectFromJSNode(node: JSCallExpression, src: String = ""): JSCall? {

            if (node.arguments.size != 2)
                return null
            val callee = ObjectUtils.tryCast(node.methodExpression, JSReferenceExpression::class.java) ?: return null
            val name = Extractor.extractJSNodeName(callee) ?: return null
            val args = mutableListOf<String>()
            val argTypes = mutableListOf<String>()
            node.arguments.forEach { arg ->
                Extractor.extractJSNodeName(arg)?.let { argName -> args.add(argName) } ?: return null
                Extractor.extractJSNodeType(arg).let { argType -> argTypes.add(argType) }
            }
            val base = Extractor.extractPyNodeBase(node)
            val resolved = callee.multiResolve(false).filterIsInstance<JSFunction>().firstOrNull() //ObjectUtils.tryCast(callee.resolve(), JSFunction::class.java) ?: return null
            val params = resolved?.parameters?.toList()
            val paramNames = MutableList(args.size) { "" }
            paramNames.forEachIndexed { idx, _ ->
                paramNames[idx] = Extractor.extractJSNodeName(params?.getOrNull(idx)) ?: ""
            }
            return JSCall(name, args, argTypes, base, paramNames, src)
        }
    }

    override fun vectorize() = vectorize(ModelsManager.tokenMapping, ModelsManager.typeMapping)
}