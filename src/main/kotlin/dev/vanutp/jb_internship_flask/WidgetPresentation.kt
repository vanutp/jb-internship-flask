// Based on https://github.com/JetBrains/intellij-community/blob/8c9a8c358b9fae54828ed283e7b6bab90b87ee9a/platform/platform-impl/src/com/intellij/openapi/wm/impl/status/PositionPanel.kt
package dev.vanutp.jb_internship_flask

import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.openapi.wm.TextWidgetPresentation
import com.intellij.openapi.wm.WidgetPresentationDataContext
import com.intellij.psi.util.parentOfTypes
import com.jetbrains.python.psi.PyTypedElement
import com.jetbrains.python.psi.types.TypeEvalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.job
import java.awt.Component
import kotlin.time.Duration.Companion.milliseconds


@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class WidgetPresentation(
    private val dataContext: WidgetPresentationDataContext,
    scope: CoroutineScope
) : TextWidgetPresentation {
    override val alignment = Component.CENTER_ALIGNMENT
    private val updateTextRequests =
        MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .also { it.tryEmit(Unit) }

    init {
        val disposable = Disposer.newDisposable()
        val multicaster = EditorFactory.getInstance().eventMulticaster
        multicaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(e: CaretEvent) {
                val editor = e.editor
                if (editor.caretModel.caretCount == 1) {
                    updatePosition(editor)
                }
            }
        }, disposable)
        scope.coroutineContext.job.invokeOnCompletion {
            Disposer.dispose(disposable)
        }
    }

    private fun updatePosition(editor: Editor) {
        val currentEditor = (dataContext.currentFileEditor.value as? TextEditor)?.editor
        if (editor === currentEditor) {
            check(updateTextRequests.tryEmit(Unit))
        }
    }

    override fun text() =
        combine(updateTextRequests, dataContext.currentFileEditor) { _, fileEditor ->
            (fileEditor as? TextEditor)?.editor
        }
            .debounce(100.milliseconds)
            .mapLatest { editor ->
                editor?.let {
                    readAction { getText(it) }
                }
            }

    private fun getText(editor: Editor): String {
        val project = dataContext.project
        val typedEl = editor.virtualFile
            .findPsiFile(project)
            ?.findElementAt(editor.caretModel.offset)
            ?.parentOfTypes(PyTypedElement::class) ?: return ""
        val typeEvalCtx = TypeEvalContext.deepCodeInsight(project)
        return typeEvalCtx.getType(typedEl)?.name ?: ""
    }
}
