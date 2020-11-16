package ru.avem.pult.viewmodels

import ru.avem.pult.view.CoefficientsSettingsView
import tornadofx.ViewModel
import java.io.File
import javax.xml.bind.JAXBContext

class CoefficientsSettingsViewModel : ViewModel() {
    companion object {
        const val MODULE_1_FRAGMENT = "MODULE_1_FRAGMENT"
        const val MODULE_2_FRAGMENT = "MODULE_2_FRAGMENT"
        const val MODULE_3_FRAGMENT = "MODULE_3_FRAGMENT"
    }

    val fragments = mapOf<String, CoefficientsSettingsView.FormFragment>(
        MODULE_1_FRAGMENT to find(),
        MODULE_2_FRAGMENT to find(),
        MODULE_3_FRAGMENT to find()
    )

    val isDataValid: Boolean
        get() = fragments.values.map { it.model.validationCtx.isValid }.reduce { acc, b ->
            acc and b
        }

    init {
        val ctx = JAXBContext.newInstance(CoefficientSettingsFragmentModel::class.java)
        val unmarshaller = ctx.createUnmarshaller()
        with(File("coef")) {
            if (this.isDirectory) {
                this.listFiles()?.forEach {
                    with(unmarshaller.unmarshal(it) as CoefficientSettingsFragmentModel) {
                        fragments[it.nameWithoutExtension]?.model?.latr?.value = this.latr.value
                        fragments[it.nameWithoutExtension]?.model?.obj?.value = this.obj.value
                        fragments[it.nameWithoutExtension]?.model?.tap?.value = this.tap.value
                    }
                }
            }
        }
    }
}