package com.github.fellowship_of_the_bus.bruteb

import models._

// import android.app.Activity
import org.scaloid.common._
import android.os.Bundle
import android.view.Gravity
import android.graphics.{Color,ColorMatrix,ColorMatrixColorFilter}
import android.widget.GridView
import android.widget.ImageView
import android.content.Intent

import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class BruteSelectActivity extends BaseActivity {
  import BruteID.Factory.{ids => bruteIDs}
  case class Selection(val button: SImageButton, var brute: BruteID)

  var confirmButton: SButton = null
  var selections = Vector[Selection]()
  var bruteButtons = Vector[SImageButton]()

  def enableButtons(): Unit = {
    // must make a selection in each button before confirming
    confirmButton.enabled = selections.forall(_.brute != null)
    for (i <- 0 until bruteIDs.length) {
      val button = bruteButtons(i)
      val brute = bruteIDs(i)
      button.enabled = selections.forall(_.brute != brute)
    }
  }

  def selectButton(button: SImageButton): Unit = {
    val _ = button.enabled = false
  }

  def deselectButton(button: SImageButton): Unit = {
    val _ = button.enabled = true
  }

  def nextSelection: Option[Selection] = selections.find(_.brute == null)

  override def onCreate(savedState: Bundle) {
    import Game.game
    var currentSelection: Option[Selection] = None

    error("Brute Blitzkrieg brute select activity started")
    super.onCreate(savedState)
    setContentView(
      new SLinearLayout {
        new STableLayout {
          for (range <- 0 until bruteIDs.length grouped 4) {
            this += new STableRow {
              for (i <- range) {
                val newButton = SImageButton(bruteIDs(i).image, {
                  // brute button clicked
                  val cur = if (currentSelection.isEmpty) {
                    val next = nextSelection
                    for (sel <- next) {
                      deselectButton(sel.button)
                    }
                    next
                  } else {
                    currentSelection
                  }
                  for (sel <- cur) {
                    sel.brute = bruteIDs(i)
                    sel.button.imageResource = sel.brute.image
                  }
                  if (currentSelection.isEmpty) {
                    for (sel <- nextSelection) {
                      selectButton(sel.button)
                    }
                  }
                  enableButtons()
                }).scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(150 dip).adjustViewBounds(true)
                bruteButtons = bruteButtons :+ newButton
              }
            }
          }
        }.<<(0,WRAP_CONTENT).Weight(3).>>.here
        new SVerticalLayout {
          for (i <- 0 until 4) {
            val newButton = SImageButton(R.drawable.ahmed2, {
              // selection clicked
              selections.foreach(x => deselectButton(x.button))
              selectButton(selections(i).button)
              currentSelection = Some(selections(i))
            }).<<(WRAP_CONTENT, 0).Weight(1).>>.scaleType(ImageView.ScaleType.CENTER_INSIDE).adjustViewBounds(true)
            selections = selections :+ Selection(newButton, game.brutes(i))
          }
          confirmButton = SButton("Confirm", {
            game.brutes = selections.map(_.brute)
            finish()
          }).enabled = false
        }.<<(0,WRAP_CONTENT).Weight(1).>>.gravity(Gravity.RIGHT).here
      }
    )
  }

  override def onResume(): Unit = {
    super.onResume()
    enableButtons()
    for (i <- 0 until 4) {
      if (selections(i).brute != null) {
        selections(i).button.imageResource = selections(i).brute.image
      }
      deselectButton(selections(i).button)
    }
    for (sel <- nextSelection) {
      selectButton(sel.button)
    }
  }
}

