package com.github.exanim.designpatterngeneratorplugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.geom.RoundRectangle2D
import javax.swing.JComponent
import javax.swing.JPanel

class PatternGraphDialog(project: Project, @Suppress("UNUSED_PARAMETER") private var segments: List<Pair<String, String>>) : DialogWrapper(project) {
    init { title = "Pattern Graph"; init() }
    override fun createCenterPanel(): JComponent = GraphCanvas()
    // No longer used; keep for API stability
    fun updateSegments(@Suppress("UNUSED_PARAMETER") newSegments: List<Pair<String,String>>) { /* no-op */ }
}

private class GraphCanvas : JPanel() {
    init {
        background = JBColor.PanelBackground
        border = JBUI.Borders.empty(12)
        preferredSize = Dimension(760, 420)
    }
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        paintBackgroundPattern(g2)
        paintLegend(g2)
    }
    private fun paintBackgroundPattern(g2: Graphics2D) {
        // Lighter, higher-contrast dots (white-ish in light theme, soft light gray in dark theme)
        g2.color = JBColor(Color(255,255,255,170), Color(200,200,200,170))
        val step = 18
        val dotSize = 2
        for (y in 0 until height step step) {
            for (x in 0 until width step step) {
                g2.fillOval(x - dotSize/2, y - dotSize/2, dotSize, dotSize)
            }
        }
    }
    private fun paintLegend(g2: Graphics2D) {
        val w = 160; val h = 70; val x = width - w - 20; val y = 20
        val rr = RoundRectangle2D.Double(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble(), 10.0, 10.0)
        g2.color = JBColor(Color(250,250,255), Color(70,70,75)); g2.fill(rr)
        g2.color = JBColor(Color(170,170,180), Color(110,110,120)); g2.stroke = BasicStroke(1.2f); g2.draw(rr)
        g2.font = g2.font.deriveFont(Font.BOLD, 12f); g2.color = JBColor.foreground(); g2.drawString("Legend", x+10, y+18)
        g2.font = g2.font.deriveFont(Font.PLAIN, 11f); g2.color = JBColor.GRAY
        g2.drawString("Dotted background", x+10, y+34)
        g2.drawString("(Preview simplified)", x+10, y+50)
    }
}
