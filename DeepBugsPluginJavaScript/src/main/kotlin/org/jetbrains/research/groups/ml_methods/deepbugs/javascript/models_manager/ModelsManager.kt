package org.jetbrains.research.groups.ml_methods.deepbugs.javascript.models_manager

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.ProjectManager
import org.jetbrains.research.groups.ml_methods.deepbugs.javascript.utils.DeepBugsJSBundle
import org.jetbrains.research.groups.ml_methods.deepbugs.services.utils.Mapping
import org.jetbrains.research.groups.ml_methods.deepbugs.services.utils.ModelUtils
import org.tensorflow.Session

object ModelsManager {
    val pluginName = DeepBugsJSBundle.message("plugin.name")
    var nodeTypeMapping: Mapping? = null
        private set
    var tokenMapping: Mapping? = null
        private set
    var operatorMapping: Mapping? = null
        private set
    var typeMapping: Mapping? = null
        private set
    var binOperatorModel: Session? = null
        private set
    var binOperandModel: Session? = null
        private set
    var swappedArgsModel: Session? = null
        private set

    init {
        DownloadClient.checkRepos()
    }

    fun initModels() {
        ProgressManager.getInstance().run(object : Task.Backgroundable(ProjectManager.getInstance().defaultProject,
                DeepBugsJSBundle.message("initialize.task.title"), false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                nodeTypeMapping = ModelUtils.loadMapping(pluginName, "nodeTypeToVector.json", indicator)
                typeMapping = ModelUtils.loadMapping(pluginName, "typeToVector.json", indicator)
                operatorMapping = ModelUtils.loadMapping(pluginName, "operatorToVector.json", indicator)
                tokenMapping = ModelUtils.loadMapping(pluginName, "tokenToVector.json", indicator)
                binOperandModel = ModelUtils.loadModel(pluginName, "binOperandDetectionModel", indicator)
                binOperatorModel = ModelUtils.loadModel(pluginName, "binOperatorDetectionModel", indicator)
                swappedArgsModel = ModelUtils.loadModel(pluginName, "swappedArgsDetectionModel", indicator)
            }
        })
    }
}