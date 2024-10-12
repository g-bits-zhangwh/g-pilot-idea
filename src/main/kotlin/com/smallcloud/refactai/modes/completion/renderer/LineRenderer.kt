package com.smallcloud.refactai.modes.completion.renderer

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.Font

class AsyncLineRenderer(
    initialText: String,
    private val editor: Editor,
    private val deprecated: Boolean
) : EditorCustomElementRenderer {
    private var color: Color? = null
    var text: String = initialText
        set(value) {
            synchronized(this) {
                field = value
            }
        }
        get() {
            return field.replace("\t", " ".repeat(editor.settings.getTabSize(editor.project)))
        }

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        synchronized(this) {
            var totalWidth = 0
            val userFont = RenderHelper.getFont(editor, deprecated)
            val chineseFont = Font("Microsoft YaHei", userFont.style, userFont.size)

            for (t in text) {
                val font = if (isChineseCharacter(t)) chineseFont else userFont
                val fontMetrics = editor.contentComponent.getFontMetrics(font)
                totalWidth += fontMetrics.charWidth(t)
            }

            return maxOf(totalWidth, 1)
        }
    }

    private fun isChineseCharacter(ch: Char): Boolean {
        return (ch in '\u4E00'..'\u9FFF') ||  // CJK Unified Ideographs
                (ch in '\u3400'..'\u4DBF')   // CJK Unified Ideographs Extension A
    }

    override fun paint(
        inlay: Inlay<*>,
        g: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes
    ) {
        val userFont = RenderHelper.getFont(editor, deprecated)
        val chineseFont = Font("Microsoft YaHei", userFont.style, userFont.size)
        synchronized(this) {
            color = color ?: RenderHelper.color
            g.color = color
            var x = targetRegion.x // 用于控制每个字符的横向位置

            for (t in text) {
                g.font = if (isChineseCharacter(t)){
                    chineseFont
                } else {
                    userFont
                }
                g.drawString(t.toString(), x, targetRegion.y + editor.ascent)

                // 更新 x 坐标，确保下一个字符绘制在右边
                x += g.fontMetrics.charWidth(t)
            }
        }
    }
}
