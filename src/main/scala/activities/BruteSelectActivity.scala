package com.github.fellowship_of_the_bus.bruteb

import models._

// import android.app.Activity
import org.scaloid.common._
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.widget.GridView
import android.widget.ImageView
import android.content.Intent

import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class BruteSelectActivity extends SActivity {
  import BruteID.Factory.{ids => bruteIDs}
  val viewSeq = new AtomicInteger(0)
  var currentButton: Option[SImageButton] = None
  var buttons = Vector[SImageButton]()

  override def onCreate(savedState: Bundle) {
    android.util.Log.e("bruteb", "Brute Blitzkrieg brute select activity started")
    super.onCreate(savedState)
    setContentView(

      new SLinearLayout {
        new STableLayout {
          for (range <- 0 until bruteIDs.length grouped 4) {
            this += new STableRow {
              for (i <- range) {
                SImageButton(bruteIDs(i).image, {
                  currentButton.map { _.imageResource = bruteIDs(i).image }
                }).scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(150 dip).adjustViewBounds(true)
              }
            }
          }
        }.<<(0,WRAP_CONTENT).Weight(3).>>.here
        new SVerticalLayout {
          for (i <- 0 until 4) {
            buttons = buttons :+ SImageButton(R.drawable.ahmed2, {
              currentButton = Some(buttons(i))
            }).<<(WRAP_CONTENT, 0).Weight(1).>>.scaleType(ImageView.ScaleType.CENTER_INSIDE).adjustViewBounds(true)
          }
          SButton("Confirm", ())
        }.<<(0,WRAP_CONTENT).Weight(1).>>.gravity(Gravity.RIGHT).here
      }
    )
  }
}

