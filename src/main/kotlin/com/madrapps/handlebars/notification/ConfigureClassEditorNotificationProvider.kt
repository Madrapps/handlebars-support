package com.madrapps.handlebars.notification

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
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
            println("Configuring class")
        }
        panel.createActionLabel("Ignore") {
            isIgnored = true
            notifications.updateAllNotifications()
        }
        return panel
    }
}