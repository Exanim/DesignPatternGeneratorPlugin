package com.github.exanim.designpatterngeneratorplugin.ui

import com.github.exanim.designpatterngeneratorplugin.MyBundle
import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.EditorTextField
import com.intellij.util.ui.JBUI
import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBTabbedPane
import java.awt.*
import java.awt.event.ItemEvent
import javax.swing.*
import javax.swing.Timer

enum class PatternCategory { CREATIONAL, STRUCTURAL, BEHAVIORAL, COMPOSITE }

private sealed interface PatternComboItem
private data class CategoryHeader(val category: PatternCategory) : PatternComboItem
private data class GeneratorEntry(val generator: PatternGenerator) : PatternComboItem

class PatternGenerationDialog(private val project: Project, generators: List<PatternGenerator>) : DialogWrapper(project) {
    // Phase flags
    private var patternChosen = false
    private var previewShown = false
    private var centerRootPanel: JPanel? = null
    private var previewContainer: JScrollPane? = null
    private val previewToggle: JBCheckBox = JBCheckBox(MyBundle.message("button.showPreview"), false)

    // Debounce timer for preview updates
    private val previewUpdateTimer = Timer(160) { performPreviewUpdate() }.apply { this.isRepeats = false }
    private val previewDoc = com.intellij.openapi.editor.EditorFactory.getInstance().createDocument("// Code preview will appear when all mandatory fields are filled\n")
    private val previewArea: EditorTextField = EditorTextField(previewDoc, project, JavaFileType.INSTANCE, true, false).apply {
        border = JBUI.Borders.empty(6)
    }

    private val patternItems: List<PatternComboItem> = buildList {
        val groups = generators.groupBy { categorize(it) }
        PatternCategory.entries.forEach { cat ->
            val gs = groups[cat] ?: emptyList()
            if (gs.isNotEmpty()) {
                add(CategoryHeader(cat))
                gs.forEach { add(GeneratorEntry(it)) }
            }
        }
    }

    private val patternCombo = ComboBox<PatternComboItem>().apply {
        patternItems.forEach { addItem(it) }
        renderer = object : ListCellRenderer<PatternComboItem> {
            private val defaultRenderer = DefaultListCellRenderer()
            override fun getListCellRendererComponent(list: JList<out PatternComboItem>?, value: PatternComboItem?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                return when (value) {
                    null -> defaultRenderer.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus)
                    is CategoryHeader -> categoryComponent(value, isSelected)
                    is GeneratorEntry -> generatorComponent(value.generator, list, isSelected)
                }
            }
            private fun categoryComponent(header: CategoryHeader, isSelected: Boolean): Component {
                val panel = JPanel(BorderLayout())
                panel.border = JBUI.Borders.empty(4,6)
                panel.background = if (isSelected) JBColor.namedColor("ComboBox.background", JBColor.LIGHT_GRAY) else JBColor(0xE0E0E0, 0x3E3E3E)
                val label = JLabel(header.category.name.lowercase().replaceFirstChar { it.titlecase() })
                label.font = label.font.deriveFont(Font.BOLD)
                panel.add(label, BorderLayout.WEST)
                return panel
            }
            private fun generatorComponent(gen: PatternGenerator, list: JList<out PatternComboItem>?, isSelected: Boolean): Component {
                val panel = JPanel(BorderLayout())
                panel.border = JBUI.Borders.empty(2,6)
                panel.background = if (isSelected) list?.selectionBackground ?: JBColor.LIGHT_GRAY else list?.background ?: JBColor.background()
                val icon = patternIcon(gen)
                val label = JLabel(MyBundle.message(gen.nameKey), icon, JLabel.LEFT)
                panel.add(label, BorderLayout.WEST)
                return panel
            }
        }
        addItemListener { ev ->
            if (ev.stateChange == ItemEvent.SELECTED && ev.item is GeneratorEntry) {
                patternChosen = true
                buildParameterPhase()
            }
        }
    }

    private val paramsPanel = JPanel(GridBagLayout())
    private val paramsScroll = JBScrollPane(paramsPanel).apply { border = JBUI.Borders.empty() }
    private val fieldComponents = LinkedHashMap<String, JComponent>()

    private val contentHolder = JPanel(BorderLayout())

    // Last preview segments for graphing
    private var lastPreviewSegments: List<Pair<String,String>> = emptyList() // fileName to body
    private var graphButton: JButton? = null
    private var graphDialog: PatternGraphDialog? = null
    private var paramsHeaderPanel: JPanel? = null

    init {
        title = MyBundle.message("dialog.patternGenerator.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout())
        centerRootPanel = root
        root.border = JBUI.Borders.empty(8)
        val top = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.emptyBottom(8)
            add(JLabel(MyBundle.message("label.selectPattern")), BorderLayout.WEST)
            add(patternCombo, BorderLayout.CENTER)
        }
        root.add(top, BorderLayout.NORTH)
        root.add(contentHolder, BorderLayout.CENTER)
        root.preferredSize = Dimension(520, 80)
        return root
    }

    override fun dispose() {
        previewUpdateTimer.stop()
        super.dispose()
    }

    private fun buildParameterPhase() {
        contentHolder.removeAll(); fieldComponents.clear(); previewShown = false
        graphDialog = null
        val generator = getSelectedGenerator() ?: return
        buildParametersUI(generator)
        // Header with preview toggle (west) and graph button (east)
        paramsHeaderPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(4)
            add(previewToggle.apply { addItemListener { evaluatePreviewVisibility() } }, BorderLayout.WEST)
            add(buildGraphButtonHolder(), BorderLayout.EAST)
        }
        val wrapper = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty()
            add(paramsHeaderPanel, BorderLayout.NORTH)
            add(paramsScroll, BorderLayout.CENTER)
        }
        contentHolder.add(wrapper, BorderLayout.CENTER)
        resizeForParameters(generator.parameters.size, includePreview = false)
        contentHolder.revalidate(); contentHolder.repaint(); window?.pack()
    }

    private fun buildParametersUI(generator: PatternGenerator) {
        paramsPanel.removeAll(); fieldComponents.clear(); previewContainer = null
        val gbc = GridBagConstraints().apply {
            gridx = 0; gridy = 0; weightx = 1.0; insets = JBUI.insets(2, 6); anchor = GridBagConstraints.WEST; fill = GridBagConstraints.HORIZONTAL
        }
        for (p in generator.parameters) {
            gbc.gridx = 0
            val label = JLabel(MyBundle.message(p.labelKey) + if (p.required) " *" else "")
            paramsPanel.add(label, gbc)
            gbc.gridx = 1
            val component: JComponent = when (p.type) {
                ParameterType.STRING -> JBTextField((p.defaultValue as? String) ?: "")
                ParameterType.BOOLEAN -> JBCheckBox().apply { isSelected = (p.defaultValue as? Boolean) ?: false }
                ParameterType.INTEGER -> JBTextField((p.defaultValue as? Int)?.toString() ?: "")
            }
            // Listener updates preview visibility
            when (component) {
                is JBTextField -> component.document.addDocumentListener(simpleDocListener { evaluatePreviewVisibility() })
                is JBCheckBox -> component.addItemListener { evaluatePreviewVisibility() }
            }
            fieldComponents[p.id] = component
            paramsPanel.add(component, gbc)
            gbc.gridy++
        }
        paramsPanel.revalidate(); paramsPanel.repaint()
        evaluatePreviewVisibility() // Initial evaluation
    }

    private fun buildGraphButtonHolder(): JComponent {
        if (graphButton == null) {
            graphButton = JButton(MyBundle.message("label.graphTitle")).apply {
                toolTipText = "Show class relationship graph for generated code"
                addActionListener { openGraphDialog() }
                isEnabled = lastPreviewSegments.isNotEmpty()
            }
        }
        val panel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            border = JBUI.Borders.emptyLeft(8)
            add(graphButton)
        }
        return panel
    }

    private fun evaluatePreviewVisibility() {
        if (!patternChosen) return
        if (!previewToggle.isSelected) {
            if (previewShown) hidePreview()
            return
        }
        // Ensure container exists
        if (!previewShown) showPreview()
        // Schedule debounced content update
        previewUpdateTimer.restart()
    }

    private fun performPreviewUpdate() {
        val gen = getSelectedGenerator() ?: return
        val values = getValues()
        val missing = gen.parameters.filter { it.required && (values[it.id] == null
                || (values[it.id] is String
                && (values[it.id] as String).isBlank())) }
        if (previewContainer == null) return
        val content = if (missing.isNotEmpty()) {
            "// " + MyBundle.message(
                "preview.notReady", missing.joinToString { it.id }
            ) + "\n"
        } else {
            (gen as? BaseJavaPatternGenerator)?.
                preview(values) ?: "// Preview not available"
        }
        updateTabbedPreview(content)
    }

    private fun updateTabbedPreview(combined: String) {
        // Detect file headers formatted as: // <filename>.java
        val headerRegex = Regex("^// (.+\\.java)")
        val lines = combined.lines()
        val fileBlocks = mutableListOf<Pair<String, MutableList<String>>>()
        var current: Pair<String, MutableList<String>>? = null
        for (line in lines) {
            val match = headerRegex.matchEntire(line)
            if (match != null) {
                current = match.groupValues[1] to mutableListOf()
                fileBlocks += current
            } else {
                current?.second?.add(line)
            }
        }
        if (fileBlocks.size <= 1) {
            lastPreviewSegments = listOf("Preview.java" to combined)
            previewArea.text = combined
            previewContainer?.viewport?.view = previewArea
            graphButton?.isEnabled = lastPreviewSegments.isNotEmpty()
            graphDialog?.updateSegments(lastPreviewSegments)
            return
        }
        lastPreviewSegments = fileBlocks.map { it.first to it.second.joinToString("\n") }
        val tabs = JBTabbedPane()
        for ((fileName, body) in lastPreviewSegments) {
            val doc = com.intellij.openapi.editor.EditorFactory.getInstance().createDocument(body.ifBlank { "// Empty file" })
            val field = EditorTextField(doc, project, JavaFileType.INSTANCE, true, false)
            field.border = JBUI.Borders.empty(4)
            tabs.addTab(fileName, JBScrollPane(field))
        }
        previewContainer?.viewport?.view = tabs
        graphButton?.isEnabled = lastPreviewSegments.isNotEmpty()
        graphDialog?.updateSegments(lastPreviewSegments)
    }

    private fun openGraphDialog() {
        if (lastPreviewSegments.isEmpty()) return
        // Reuse existing dialog if open
        val existing = graphDialog
        if (existing != null && existing.isShowing) {
            existing.updateSegments(lastPreviewSegments)
            existing.toFront()
            return
        }
        val dialog = PatternGraphDialog(project, lastPreviewSegments)
        graphDialog = dialog
        dialog.show()
    }

    private fun showPreview() {
        previewShown = true
        if (previewContainer == null) {
            previewContainer = JBScrollPane(previewArea).apply {
                border = JBUI.Borders.emptyTop(6)
                verticalScrollBar.unitIncrement = 16
                preferredSize = Dimension(600, 240)
            }
            contentHolder.add(previewContainer!!, BorderLayout.SOUTH)
            resizeForParameters(getSelectedGenerator()?.parameters?.size ?: 0, includePreview = true)
            contentHolder.revalidate(); contentHolder.repaint(); window?.pack()
        }
    }

    private fun hidePreview() {
        previewShown = false
        previewContainer?.let { contentHolder.remove(it) }
        previewContainer = null
        resizeForParameters(getSelectedGenerator()?.parameters?.size ?: 0, includePreview = false)
        contentHolder.revalidate(); contentHolder.repaint(); window?.pack()
    }

    private fun resizeForParameters(paramCount: Int, includePreview: Boolean) {
        val baseTop = 140
        val perRow = 30
        val paramsHeight = if (patternChosen) (paramCount * perRow) else 0
        val previewExtra = if (includePreview) 260 else 0
        val total = baseTop + paramsHeight + previewExtra
        val clamped = total.coerceIn(140, 800)
        val width = if (includePreview) 780 else 540
        centerRootPanel?.preferredSize = Dimension(width, clamped)
    }

    private fun categorize(gen: PatternGenerator): PatternCategory = when (gen.id.lowercase()) {
        // Creational
        "singleton", "builder", "factory", "factorymethod", "abstractfactory", "prototype" -> PatternCategory.CREATIONAL
        // Structural
        "adapter", "bridge", "composite", "decorator", "facade", "flyweight", "proxy" -> PatternCategory.STRUCTURAL
        // Behavioral
        "strategy", "observer", "command", "state", "iterator", "mediator", "memento", "visitor", "chainofresponsibility", "template", "testpattern" -> PatternCategory.BEHAVIORAL
        // Composite meta-patterns (pattern-of-patterns)
        "undoablecommand" -> PatternCategory.COMPOSITE
        else -> PatternCategory.CREATIONAL
    }

    private fun patternIcon(gen: PatternGenerator): Icon = when (categorize(gen)) {
        PatternCategory.CREATIONAL -> AllIcons.Nodes.Class
        PatternCategory.STRUCTURAL -> AllIcons.Nodes.AbstractClass
        PatternCategory.BEHAVIORAL -> AllIcons.Nodes.Interface
        PatternCategory.COMPOSITE -> AllIcons.Nodes.Package // Using package icon for composite category
    }

    fun getSelectedGenerator(): PatternGenerator? = (patternCombo.selectedItem as? GeneratorEntry)?.generator

    fun getValues(): Map<String, Any> {
        val g = getSelectedGenerator() ?: return emptyMap()
        val result = HashMap<String, Any>()
        for (p in g.parameters) {
            val comp = fieldComponents[p.id]
            val value: Any = when (p.type) {
                ParameterType.STRING -> (comp as JBTextField).text.trim().also { scheduleGraphRefresh() }
                ParameterType.BOOLEAN -> (comp as JBCheckBox).isSelected.also { scheduleGraphRefresh() }
                ParameterType.INTEGER -> (comp as JBTextField).text.trim().toIntOrNull()?.also { scheduleGraphRefresh() } ?: 0
            }
            result[p.id] = value
        }
        return result
    }

    private fun simpleDocListener(onChange: () -> Unit) = object : javax.swing.event.DocumentListener {
        override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = onChange()
        override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = onChange()
        override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = onChange()
    }

    private fun scheduleGraphRefresh() {
        // Update graph dialog's segments if open (no parsing, just reusing lastPreviewSegments)
        graphDialog?.updateSegments(lastPreviewSegments)
    }
}
