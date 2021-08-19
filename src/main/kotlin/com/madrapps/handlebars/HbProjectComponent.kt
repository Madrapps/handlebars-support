package com.madrapps.handlebars

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.progress.util.ReadTask
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupManager
import com.madrapps.handlebars.extension.HandlebarsMappingProvider.Companion.MAPPING_PROVIDER_EP_NAME
import com.madrapps.handlebars.persistence.PersistenceService

class HbProjectComponent(val project: Project?) : ProjectComponent {

    override fun projectOpened() {
        if (project != null) {
            StartupManager.getInstance(project).registerPostStartupActivity {
                println("Project opened - " + project.name)
                runBackgroundProcess(project)
            }
        }
    }

    class ReadMapTask(private val project: Project) : ReadTask() {

        override fun runBackgroundProcess(indicator: ProgressIndicator): Continuation {
            if (project.isDisposed) return super.runBackgroundProcess(indicator)
            return DumbService.getInstance(project)
                .runReadActionInSmartMode<ReadTask.Continuation> { performInReadAction(indicator) }
        }

        override fun performInReadAction(indicator: ProgressIndicator): Continuation? {
            project.let {
                println("Init map")
                val storageMap = PersistenceService.getInstance(it).psiMap
                for (extension in MAPPING_PROVIDER_EP_NAME.extensions) {
                    val map = extension.addHbsMapping(project)
                    map.forEach { key, value ->
                        storageMap.putIfAbsent(key, value)
                    }
                    println(map)
                }
            }
            return Continuation {}
        }

        override fun onCanceled(indicator: ProgressIndicator) {
            if (project.isDisposed) return
            runBackgroundProcess(project)
        }
    }
}

private fun runBackgroundProcess(project: Project) {
    val progressIndicator = BackgroundableProcessIndicator(
        project,
        "Adding hbs mappings",
        PerformInBackgroundOption.ALWAYS_BACKGROUND,
        null,
        null,
        false
    )
    progressIndicator.isIndeterminate = true
    ProgressIndicatorUtils.scheduleWithWriteActionPriority(progressIndicator, HbProjectComponent.ReadMapTask(project))
}