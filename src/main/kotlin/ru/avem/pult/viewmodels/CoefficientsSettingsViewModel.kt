package ru.avem.pult.viewmodels

import ru.avem.pult.view.CoefficientsSettingsView
import tornadofx.ViewModel
import java.io.File
import javax.xml.bind.JAXBContext

class CoefficientsSettingsViewModel : ViewModel() {
    companion object {
        const val MODULE_1_FRAGMENT = "MODULE_1_FRAGMENT"
        const val MODULE_2_FRAGMENT = "MODULE_2_FRAGMENT"
        const val SIMPLE_FRAGMENT_1 = "SIMPLE_FRAGMENT_1"
        const val SIMPLE_FRAGMENT_2 = "SIMPLE_FRAGMENT_2"
    }

    val voltmeterFragments = mapOf<String, CoefficientsSettingsView.VoltmeterFormFragment>(
        MODULE_1_FRAGMENT to find(),
        MODULE_2_FRAGMENT to find(),
    )
    val simpleFragments = mapOf(
        SIMPLE_FRAGMENT_1 to CoefficientsSettingsView.SimpleTwoFieldFormFragment(
            "Первичная обмотка",
            "Вторичная обмотка"
        ),
        SIMPLE_FRAGMENT_2 to CoefficientsSettingsView.SimpleTwoFieldFormFragment(
            "Коэффициент датчика 1",
            "Коэффициент датчика 2"
        ),
    )

    val isDataValid: Boolean
        get() = voltmeterFragments.values.map { it.model.validationCtx.isValid }.reduce { acc, b ->
            acc and b
        } && simpleFragments.values.map { it.model.validationCtx.isValid }.reduce { acc, b ->
            acc and b
        }

    init {
        loadVoltmeterCoefficients()
        loadSimpleCoefficients()
    }

    private fun loadVoltmeterCoefficients() {
        val ctx = JAXBContext.newInstance(CoefficientSettingsFragmentModel::class.java)
        val unmarshaller = ctx.createUnmarshaller()
        with(File("coef")) {
            if (this.isDirectory) {
                this.listFiles()?.forEach {
                    with(unmarshaller.unmarshal(it) as CoefficientSettingsFragmentModel) {
                        voltmeterFragments[it.nameWithoutExtension]?.model?.latr?.value = this.latr.value
                        voltmeterFragments[it.nameWithoutExtension]?.model?.obj?.value = this.obj.value
                        voltmeterFragments[it.nameWithoutExtension]?.model?.tap?.value = this.tap.value
                    }
                }
            }
        }
    }

    private fun loadSimpleCoefficients() {
        val ctx = JAXBContext.newInstance(SimpleTwoFieldFormModel::class.java)
        val unmarshaller = ctx.createUnmarshaller()
        with(File("coef")) {
            if (this.isDirectory) {
                this.listFiles()?.forEach {
                    with(unmarshaller.unmarshal(it) as SimpleTwoFieldFormModel) {
                        simpleFragments[it.nameWithoutExtension]?.model?.firstValue?.value = this.firstValue.value
                        simpleFragments[it.nameWithoutExtension]?.model?.secondValue?.value = this.secondValue.value
                    }
                }
            }
        }
    }
}