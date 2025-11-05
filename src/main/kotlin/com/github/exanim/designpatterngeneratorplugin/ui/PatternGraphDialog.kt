package com.github.exanim.designpatterngeneratorplugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import javax.swing.*
import java.awt.*
import java.awt.geom.Line2D

/** Dialog showing a simple class-method-field graph parsed from generated preview segments. */
class PatternGraphDialog(project: Project, private val segments: List<Pair<String, String>>) : DialogWrapper(project) {
    init { title = "Pattern Graph"; init() }

    override fun createCenterPanel(): JComponent = GraphCanvas(segments)
}

private data class ClassNode(val name: String, val fields: List<String>, val methods: List<String>)
private data class Edge(val from: ClassNode, val to: ClassNode, val label: String)

private class GraphCanvas(private val segments: List<Pair<String,String>>) : JPanel() {
    private val nodes = mutableListOf<ClassNode>()
    private val edges = mutableListOf<Edge>()
    private val boxBounds = mutableMapOf<ClassNode, Rectangle>()

    init {
        background = JBColor.PanelBackground
        border = JBUI.Borders.empty(12)
        parseSegments()
        layoutGraph()
        preferredSize = Dimension(800, 380)
    }

    private fun parseSegments() {
        // Naive Java parsing: capture class declarations, fields, and method signatures at top-level
        val classRegex = Regex("class\\s+(\\w+)")
        val fieldRegex = Regex("(private|public|protected)\\s+[\\w<>\\[\\]]+\\s+(\\w+)\\s*([=;])")
        val methodRegex = Regex("(public|private|protected)\\s+[\\w<>\\[\\]]+\\s+(\\w+)\\s*\\(")
        for ((_, body) in segments) {
            val classMatch = classRegex.find(body) ?: continue
            val className = classMatch.groupValues[1]
            val fields = fieldRegex.findAll(body).map { it.groupValues[2] }.distinct().toList()
            val methods = methodRegex.findAll(body).map { it.groupValues[2] }.distinct().toList()
            nodes += ClassNode(className, fields, methods)
        }
        // Simple edge heuristic: if method body contains another class name, draw arrow
        for (n in nodes) {
            for (other in nodes) {
                if (n != other && segments.any { it.second.contains(other.name) && it.second.contains(n.name) }) {
                    edges += Edge(n, other, "ref")
                }
            }
        }
    }

    private fun layoutGraph() {
        // Simple grid layout
        val cols = maxOf(1, Math.ceil(Math.sqrt(nodes.size.toDouble())).toInt())
        val cellW = 220; val cellH = 160
        nodes.forEachIndexed { idx, node ->
            val r = idx / cols; val c = idx % cols
            boxBounds[node] = Rectangle(40 + c * (cellW + 40), 40 + r * (cellH + 40), cellW, cellH)
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        paintDottedBackground(g2)
        paintEdges(g2)
        paintNodes(g2)
        paintLegend(g2)
    }

    private fun paintDottedBackground(g2: Graphics2D) {
        g2.color = JBColor(Gray._230, Gray._60)
        val step = 16
        for (y in 0 until height step step) {
            for (x in 0 until width step step) {
                g2.fillRect(x, y, 1, 1)
            }
        }
    }

    private fun paintNodes(g2: Graphics2D) {
        for (n in nodes) {
            val rect = boxBounds[n]!!
            g2.color = JBColor(Gray._250, Gray._70)
            g2.fillRect(rect.x, rect.y, rect.width, rect.height)
            g2.color = JBColor.GRAY
            g2.drawRect(rect.x, rect.y, rect.width, rect.height)
            // Class name
            g2.font = g2.font.deriveFont(Font.BOLD, 14f)
            g2.drawString(n.name, rect.x + 8, rect.y + 20)
            // Fields
            g2.font = g2.font.deriveFont(Font.PLAIN, 12f)
            var y = rect.y + 40
            g2.drawString("Fields:", rect.x + 8, y); y += 16
            n.fields.take(5).forEach { f -> g2.drawString(f, rect.x + 16, y); y += 14 }
            if (n.fields.size > 5) { g2.drawString("…", rect.x + 16, y); y += 14 }
            // Methods
            y += 8; g2.drawString("Methods:", rect.x + 8, y); y += 16
            n.methods.take(6).forEach { m -> g2.drawString(m + "()", rect.x + 16, y); y += 14 }
            if (n.methods.size > 6) g2.drawString("…", rect.x + 16, y)
        }
    }

    private fun paintEdges(g2: Graphics2D) {
        g2.color = JBColor(Color(120,120,255), Color(150,150,255))
        for (e in edges) {
            val from = boxBounds[e.from]!!; val to = boxBounds[e.to]!!
            val x1 = from.centerX(); val y1 = from.centerY()
            val x2 = to.centerX(); val y2 = to.centerY()
            drawArrow(g2, x1, y1, x2, y2)
        }
    }

    private fun Rectangle.centerX() = this.x + this.width / 2
    private fun Rectangle.centerY() = this.y + this.height / 2

    private fun drawArrow(g2: Graphics2D, x1: Int, y1: Int, x2: Int, y2: Int) {
        g2.stroke = BasicStroke(1.6f)
        g2.draw(Line2D.Float(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat()))
        // Arrow head
        val dx = x2 - x1; val dy = y2 - y1
        val angle = Math.atan2(dy.toDouble(), dx.toDouble())
        val len = 10
        val ax1 = (x2 - len * Math.cos(angle - 0.35)).toInt()
        val ay1 = (y2 - len * Math.sin(angle - 0.35)).toInt()
        val ax2 = (x2 - len * Math.cos(angle + 0.35)).toInt()
        val ay2 = (y2 - len * Math.sin(angle + 0.35)).toInt()
        g2.draw(Line2D.Float(x2.toFloat(), y2.toFloat(), ax1.toFloat(), ay1.toFloat()))
        g2.draw(Line2D.Float(x2.toFloat(), y2.toFloat(), ax2.toFloat(), ay2.toFloat()))
    }

    private fun paintLegend(g2: Graphics2D) {
        val legendW = 160; val legendH = 70
        val x = width - legendW - 20; val y = 20
        g2.color = JBColor(Gray._250, Gray._55)
        g2.fillRect(x, y, legendW, legendH)
        g2.color = JBColor.GRAY
        g2.drawRect(x, y, legendW, legendH)
        g2.font = g2.font.deriveFont(Font.BOLD, 12f)
        g2.drawString("Legend", x + 8, y + 16)
        g2.font = g2.font.deriveFont(Font.PLAIN, 11f)
        g2.drawString("Blue Arrow: reference", x + 8, y + 34)
        g2.drawString("Rect: class", x + 8, y + 50)
        g2.drawString("Fields/Methods listed", x + 8, y + 62)
    }
}

