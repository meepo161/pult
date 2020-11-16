package ru.avem.pult.view

import javafx.geometry.Pos
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val regularLabels by cssclass()
        val headerLabels by cssclass()
        val testHeaderLabels by cssclass()
        val anchorPaneBorders by cssclass()
        val anchorPaneStatusColor by cssclass()
        val roundButton by cssclass()
        val powerButtons by cssclass()
        val tableRowCell by cssclass()
    }

    init {
        tableRowCell {
            text {
                fontSize = 20.px
            }
        }

        text {
            fontSize = 16.px
        }

        regularLabels {
            fontSize = 16.px
        }

        headerLabels {
            fontSize = 16.px
        }

        testHeaderLabels {
            text {
                fontSize = 30.px
                fontWeight = FontWeight.EXTRA_BOLD
            }
        }

        anchorPaneBorders {
            borderColor += CssBox(
                top = c("grey"),
                bottom = c("grey"),
                left = c("grey"),
                right = c("grey")
            )
        }

        anchorPaneStatusColor {
            backgroundColor += c("#B4AEBF")
        }

        roundButton {
            backgroundRadius += CssBox(
                top = 30.px,
                bottom = 30.px,
                left = 30.px,
                right = 30.px
            )
        }

        tableColumn {
            alignment = Pos.CENTER
            fontWeight = FontWeight.EXTRA_BOLD
            fontSize = 16.px
        }

        tableRowCell {
            cellSize = 50.px
        }

        checkBox {
            selected {
                mark {
                    backgroundColor += c("black")
                }
            }
        }
    }
}
