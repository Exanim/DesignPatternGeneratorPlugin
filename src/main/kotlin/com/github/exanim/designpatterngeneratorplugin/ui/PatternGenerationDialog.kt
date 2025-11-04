package com.github.exanim.designpatterngeneratorplugin.ui

import com.github.exanim.designpatterngeneratorplugin.MyBundle
import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Component
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class PatternGenerationDialog(private val project: Project, private val generators: List<PatternGenerator>) : DialogWrapper(project) {
    private val patternCombo = ComboBox<PatternGenerator>().apply {
        generators.forEach { addItem(it) }
        renderer = object : ListCellRenderer<PatternGenerator> {
            private val delegate = DefaultListCellRenderer()
            override fun getListCellRendererComponent(list: JList<out PatternGenerator>?, value: PatternGenerator?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                return delegate.getListCellRendererComponent(list, value?.let { MyBundle.message(it.nameKey) } ?: "", index, isSelected, cellHasFocus)
            }
        }
    }

    private val paramPanel = JPanel(GridBagLayout())
    private val fieldComponents = LinkedHashMap<String, JComponent>()

    init {
        title = MyBundle.message("dialog.patternGenerator.title")
        init()
        rebuildParameters()
        patternCombo.addActionListener { rebuildParameters() }
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout())
        val top = JPanel(BorderLayout()).apply {
            add(JLabel(MyBundle.message("label.selectPattern")), BorderLayout.WEST)
            add(patternCombo, BorderLayout.CENTER)
        }
        root.add(top, BorderLayout.NORTH)
        root.add(JScrollPane(paramPanel), BorderLayout.CENTER)
        return root
    }

    private fun rebuildParameters() {
        paramPanel.removeAll()
        fieldComponents.clear()
        val generator = getSelectedGenerator() ?: return
        val gbc = GridBagConstraints().apply {
            gridx = 0; gridy = 0; weightx = 0.0; insets = Insets(4,4,4,4); anchor = GridBagConstraints.WEST
        }
        for (p in generator.parameters) {
            gbc.gridx = 0
            val label = JLabel(MyBundle.message(p.labelKey) + if (p.required) " *" else "")
            paramPanel.add(label, gbc)
            gbc.gridx = 1
            val component: JComponent = when (p.type) {
                ParameterType.STRING -> JBTextField(p.defaultValue as? String ?: "")
                ParameterType.BOOLEAN -> JBCheckBox().apply { isSelected = (p.defaultValue as? Boolean) ?: false }
                ParameterType.INTEGER -> JBTextField((p.defaultValue as? Int)?.toString() ?: "")
            }
            fieldComponents[p.id] = component
            paramPanel.add(component, gbc)
            gbc.gridy++
        }
        paramPanel.revalidate(); paramPanel.repaint()
    }

    fun getSelectedGenerator(): PatternGenerator? = patternCombo.selectedItem as? PatternGenerator

    fun getValues(): Map<String, Any> {
        val g = getSelectedGenerator() ?: return emptyMap()
        val result = HashMap<String, Any>()
        for (p in g.parameters) {
            val comp = fieldComponents[p.id]
            val value: Any? = when (p.type) {
                ParameterType.STRING -> (comp as JBTextField).text.trim()
                ParameterType.BOOLEAN -> (comp as JBCheckBox).isSelected
                ParameterType.INTEGER -> (comp as JBTextField).text.trim().toIntOrNull() ?: 0
            }
            if (value != null) result[p.id] = value
        }
        return result
    }
}
