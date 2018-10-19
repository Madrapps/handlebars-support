package com.madrapps.handlebars.notification

import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications

private val KEY = Key.create<EditorNotificationPanel>("hbs.associate.class")

class ConfigureClassEditorNotificationProvider(private val project: Project, private val notifications: EditorNotifications) : EditorNotifications.Provider<EditorNotificationPanel>() {

    private var isIgnored = false

    override fun getKey(): Key<EditorNotificationPanel> = KEY

    override fun createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel? {
        if (file.extension == "hbs" && !isIgnored) {
            return createPanel()
        }
        return null
    }

    private fun createPanel(): EditorNotificationPanel {
        val panel = EditorNotificationPanel()
        panel.setText("No class configured.")
        panel.createActionLabel("Configure") {
            val psiClass = chooseClassDialog("Choose class", project)
            println("Class configured : " + psiClass.name)
        }
        panel.createActionLabel("Ignore") {
            isIgnored = true
            notifications.updateAllNotifications()
        }
        return panel
    }

    private fun chooseClassDialog(title: String, project: Project): PsiClass {
        val dialog = TreeClassChooserFactory.getInstance(project).createAllProjectScopeChooser(title)
        dialog.showDialog()
        return dialog.selected
    }
}