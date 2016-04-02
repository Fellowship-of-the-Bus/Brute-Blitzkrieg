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

  override def onCreate(savedState: Bundle) {
    var currentButton: Option[Selection] = None
    var selections = Vector[Selection]()
    var bruteButtons = Vector[SImageButton]()
    var confirmButton: SButton = null

    android.util.Log.e("bruteb", "Brute Blitzkrieg brute select activity started")
    super.onCreate(savedState)
    setContentView(
      new SLinearLayout {
        new STableLayout {
          for (range <- 0 until bruteIDs.length grouped 4) {
            this += new STableRow {
              for (i <- range) {
                val newButton = SImageButton(bruteIDs(i).image, {
                  for (sel <- currentButton) {
                    sel.brute = bruteIDs(i)
                    sel.button.imageResource = sel.brute.image
                  }
                  for (i <- 0 until bruteIDs.length) {
                    val button = bruteButtons(i)
                    val brute = bruteIDs(i)
                    button.enabled = selections.forall(_.brute != brute)
                  }
                  // must make a selection in each button before confirming
                  confirmButton.enabled = selections.forall(_.brute != null)
                }).scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(150 dip).adjustViewBounds(true)
                bruteButtons = bruteButtons :+ newButton
              }
            }
          }
        }.<<(0,WRAP_CONTENT).Weight(3).>>.here
        new SVerticalLayout {
          for (i <- 0 until 4) {
            val newButton = SImageButton(R.drawable.ahmed2, {
              currentButton = Some(selections(i))
            }).<<(WRAP_CONTENT, 0).Weight(1).>>.scaleType(ImageView.ScaleType.CENTER_INSIDE).adjustViewBounds(true)
            selections = selections :+ Selection(newButton, null)
          }
          confirmButton = SButton("Confirm", {
            Game.game.brutes = selections.map(_.brute)
            finish()
          }).enabled = false
        }.<<(0,WRAP_CONTENT).Weight(1).>>.gravity(Gravity.RIGHT).here
      }
    )
  }
}

