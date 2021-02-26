package ru.avem.pult.view

import javafx.geometry.Pos
import javafx.scene.text.FontWeight
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val warningDecorator by cssclass()
        val regularLabels by cssclass()
        val headerLabels by cssclass()
        val testHeaderLabels by cssclass()
        val anchorPaneBorders by cssclass()
        val hard by cssclass()
        val medium by cssclass()
        val superHard by cssclass()
        val anchorPaneStatusColor by cssclass()
        val roundButton by cssclass()
        val powerButtons by cssclass()
        val tableRowCell by cssclass()
    }

    init {
        warningDecorator {
            borderColor += CssBox(
                top = c("orange"),
                bottom = c("orange"),
                left = c("orange"),
                right = c("orange"),
            )
            borderWidth += CssBox(
                top = 3.px,
                bottom = 3.px,
                left = 3.px,
                right = 3.px,
            )
        }

        tableRowCell {
            cellSize = 50.px
            text {
                fontSize = 36.px
                fontWeight = FontWeight.BOLD
            }
        }

        tableColumn {
            alignment = Pos.CENTER
            fontWeight = FontWeight.BOLD
            text {
                fontSize = 30.px
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

        hard {
            fontSize = 40.px
            fontWeight = FontWeight.BOLD
            text {
                fontSize = 40.px
                fontWeight = FontWeight.BOLD
            }
        }


        medium {
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
            text {
                fontSize = 40.px
                fontWeight = FontWeight.BOLD
            }
        }

        superHard {
            fontSize = 64.px
            fontWeight = FontWeight.BOLD
            text {
                fontSize = 64.px
                fontWeight = FontWeight.BOLD
            }
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

        checkBox {
            selected {
                mark {
                    backgroundColor += c("black")
                }
            }
        }
    }
}
