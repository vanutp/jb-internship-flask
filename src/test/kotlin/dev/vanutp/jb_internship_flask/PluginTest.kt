package dev.vanutp.jb_internship_flask

import com.intellij.driver.sdk.openFile
import com.intellij.driver.sdk.ui.components.codeEditor
import com.intellij.driver.sdk.ui.components.editorTabs
import com.intellij.driver.sdk.ui.components.ideFrame
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.runner.Starter
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.minutes

class PluginTest {
    @Test
    fun simpleTest() {
        Starter.newContext(
            testName = "test",
            TestCase(
                IdeProductProvider.PY,
                projectInfo = LocalProjectInfo(Path("src/test/resources/project")),
            )
                .withVersion("2024.3")
        ).apply {

            val pathToPlugin = System.getProperty("path.to.build.plugin")
            PluginConfigurator(this).installPluginFromFolder(File(pathToPlugin))
        }.runIdeWithDriver().useDriverAndCloseIde {
            ideFrame {
                waitForIndicators(5.minutes)
                openFile("main.py")
                editorTabs().isTabOpened("main.py")
                codeEditor {
                    clickOnPosition(1, 1)
                }
            }
        }
    }
}
