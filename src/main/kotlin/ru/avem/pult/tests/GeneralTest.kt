package ru.avem.pult.tests

import ru.avem.pult.controllers.TestController
import ru.avem.pult.view.TestView
import ru.avem.pult.viewmodels.MainViewModel

class GeneralTest(model: MainViewModel, view: TestView, controller: TestController) : Test(model, view, controller) {
    override fun getNotRespondingMessageFromTest() = ""
}
