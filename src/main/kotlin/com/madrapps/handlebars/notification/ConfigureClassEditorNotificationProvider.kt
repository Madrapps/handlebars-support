package com.madrapps.handlebars.notification

import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import com.madrapps.handlebars.extension.HandlebarsMappingProvider.Companion.MAPPING_PROVIDER_EP_NAME
import com.madrapps.handlebars.persistence.PersistenceService

private val KEY = Key.create<EditorNotificationPanel>("hbs.associate.class")

class ConfigureClassEditorNotificationProvider(private val project: Project, private val notifications: EditorNotifications) : EditorNotifications.Provider<EditorNotificationPanel>(), DumbAware {

    override fun getKey(): Key<EditorNotificationPanel> = KEY

    override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel? {
        if (file.extension == "hbs" && isNotConfigured(file.path)) {
            return createPanel(file)
        }
        return null
    }

    private fun isNotConfigured(path: String): Boolean {
        return !PersistenceService.getInstance(project).psiMap.keys.contains(path)
    }

    private fun createPanel(file: VirtualFile): EditorNotificationPanel {
        val service = PersistenceService.getInstance(project)
        val panel = EditorNotificationPanel()
        panel.setText("No class configured.")
        panel.createActionLabel("Configure") {
            val psiClass = chooseClassDialog("Choose class", project)
            if (psiClass != null) {
                println("Class configured : " + psiClass.name)
                psiClass.qualifiedName?.let {
                    service.psiMap[file.path] = it
                    notifications.updateAllNotifications()
                }
            }
        }
        panel.createActionLabel("Ignore") {
            service.psiMap[file.path] = null
            notifications.updateAllNotifications()
        }
        return panel
    }

    private fun chooseClassDialog(title: String, project: Project): PsiClass? {
        val dialog = TreeClassChooserFactory.getInstance(project).createAllProjectScopeChooser(title)
        dialog.showDialog()
        return dialog.selected
    }
}