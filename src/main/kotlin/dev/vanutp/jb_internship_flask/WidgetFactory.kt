package dev.vanutp.jb_internship_flask

import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WidgetPresentationDataContext
import com.intellij.openapi.wm.WidgetPresentationFactory
import kotlinx.coroutines.CoroutineScope

class WidgetFactory : StatusBarWidgetFactory, WidgetPresentationFactory {
    companion object {
        const val ID = "VariableTypeWidget"
    }

    override fun getId() = ID
    override fun getDisplayName() = "VariableType"

    override fun createPresentation(
        context: WidgetPresentationDataContext,
        scope: CoroutineScope
    ) = WidgetPresentation(context, scope)
}
