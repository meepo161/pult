package ru.avem.pult.viewmodels

import ru.avem.pult.view.LatrSettingsView
import tornadofx.ViewModel
import java.io.File
import javax.xml.bind.JAXBContext

class LatrSettingsViewModel : ViewModel() {
    companion object {
        const val MODULE_1_FRAGMENT = "MODULE_1_FRAGMENT"
        const val MODULE2_U1_FRAGMENT = "MODULE2_U1_FRAGMENT"
        const val MODULE2_U3_FRAGMENT = "MODULE2_U3_FRAGMENT"
        const val MODULE2_U5_FRAGMENT = "MODULE2_U5_FRAGMENT"
        const val MODULE2_U8_FRAGMENT = "MODULE2_U8_FRAGMENT"
        const val MODULE2_U11_FRAGMENT = "MODULE2_U11_FRAGMENT"
        const val MODULE2_U13_FRAGMENT = "MODULE2_U13_FRAGMENT"
        const val MODULE2_U15_FRAGMENT = "MODULE2_U15_FRAGMENT"
        const val MODULE2_U17_FRAGMENT = "MODULE2_U17_FRAGMENT"
        const val MODULE2_LAST_FRAGMENT = "MODULE2_LAST_FRAGMENT"
        const val MODULE3_U1_FRAGMENT = "MODULE3_U1_FRAGMENT"
        const val MODULE3_U3_FRAGMENT = "MODULE3_U3_FRAGMENT"
        const val MODULE3_U5_FRAGMENT = "MODULE3_U5_FRAGMENT"
        const val MODULE3_U8_FRAGMENT = "MODULE3_U8_FRAGMENT"
        const val MODULE3_U11_FRAGMENT = "MODULE3_U11_FRAGMENT"
        const val MODULE3_U13_FRAGMENT = "MODULE3_U13_FRAGMENT"
        const val MODULE3_U15_FRAGMENT = "MODULE3_U15_FRAGMENT"
        const val MODULE3_U17_FRAGMENT = "MODULE3_U17_FRAGMENT"
        const val MODULE3_U20_FRAGMENT = "MODULE3_U20_FRAGMENT"
        const val MODULE3_LAST_FRAGMENT = "MODULE3_LAST_FRAGMENT"
    }

    val fragments = mapOf<String, LatrSettingsView.FormFragment>(
        MODULE_1_FRAGMENT to find(),
        MODULE2_U1_FRAGMENT to find(),
        MODULE2_U3_FRAGMENT to find(),
        MODULE2_U5_FRAGMENT to find(),
        MODULE2_U8_FRAGMENT to find(),
        MODULE2_U11_FRAGMENT to find(),
        MODULE2_U13_FRAGMENT to find(),
        MODULE2_U15_FRAGMENT to find(),
        MODULE2_U17_FRAGMENT to find(),
        MODULE2_LAST_FRAGMENT to find(),
        MODULE3_U1_FRAGMENT to find(),
        MODULE3_U3_FRAGMENT to find(),
        MODULE3_U5_FRAGMENT to find(),
        MODULE3_U8_FRAGMENT to find(),
        MODULE3_U11_FRAGMENT to find(),
        MODULE3_U13_FRAGMENT to find(),
        MODULE3_U15_FRAGMENT to find(),
        MODULE3_U17_FRAGMENT to find(),
        MODULE3_U20_FRAGMENT to find(),
        MODULE3_LAST_FRAGMENT to find()
    )

    val isDataValid
        get() = fragments.values.map { it.model.validationCtx.isValid }.reduce { acc, b ->
            acc and b
        }

    init {
        val ctx = JAXBContext.newInstance(LatrSettingsFragmentModel::class.java)
        val unmarshaller = ctx.createUnmarshaller()
        with(File("latr")) {
            if (this.isDirectory) {
                this.listFiles()?.forEach {
                    with(unmarshaller.unmarshal(it) as LatrSettingsFragmentModel) {
                        fragments[it.nameWithoutExtension]?.model?.corridor?.value = this.corridor.value
                        fragments[it.nameWithoutExtension]?.model?.minDutty?.value = this.minDutty.value
                        fragments[it.nameWithoutExtension]?.model?.maxDutty?.value = this.maxDutty.value
                        fragments[it.nameWithoutExtension]?.model?.timeMinPulse?.value = this.timeMinPulse.value
                        fragments[it.nameWithoutExtension]?.model?.timeMaxPulse?.value = this.timeMaxPulse.value
                        fragments[it.nameWithoutExtension]?.model?.delta?.value = this.delta.value
                    }
                }
            }
        }
    }
}
