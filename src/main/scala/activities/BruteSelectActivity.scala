package com.github.fellowship_of_the_bus.bruteb

import models._

// import android.app.Activity
import org.scaloid.common._
import android.os.Bundle
import android.view.{Gravity,View}
import android.graphics.{Color,ColorMatrix,ColorMatrixColorFilter,PorterDuff}
import android.graphics.drawable.{Drawable,BitmapDrawable}
import android.widget.GridView
import android.widget.ImageView
import android.content.Intent
import android.view.ViewGroup.LayoutParams.MATCH_PARENT

import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class BruteSelectActivity extends BaseActivity {
  import BruteID.Factory.{ids => bruteIDs}
  case class Selection(val button: SImageButton, var brute: BruteID)

  var confirmButton: SButton = null
  var selections = Vector[Selection]()
  var bruteButtons = Vector[SImageButton]()
  var popUp: SRelativeLayout = null

  def enableButtons(): Unit = {
    // must make at least one selection before confirming
    confirmButton.enabled = selections.exists(_.brute != null)
    for (i <- 0 until bruteIDs.length) {
      val button = bruteButtons(i)
      val brute = bruteIDs(i)
      // this sets the buttons so they appear depressed when selected
      if (selections.forall(_.brute != brute)) button.background.setColorFilter(null)
      else button.background.setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN)
    }
  }

  def nextSelection: Option[Selection] = selections.find(_.brute == null)

  override def onCreate(savedState: Bundle) {
    import Game.game
    var currentSelection: Option[Selection] = None

    error("Brute Blitzkrieg brute select activity started")
    super.onCreate(savedState)

    setContentView(
      new SVerticalLayout {
        new SLinearLayout {
          // map gold
          new SLinearLayout {
            SImageView(new BitmapDrawable(getResources(), BattleCanvas.goldImage)).scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(20 dip).minimumHeight(20 dip).adjustViewBounds(true).<<.wrap.marginTop(4 dip).>>
            new STextView {
              text = s"${Game.game.map.startingGold}"
              textSize = 20 sp
            }.<<.wrap.>>.here
          }.<<(0,MATCH_PARENT).Weight(1).>>.here


          // tutorial
          popUp = new SRelativeLayout {
            new SVerticalLayout {
              val text = new STextView {
                text = "Select 4 Brutes. When you are done press confirm."
                textSize = 20 sp
              }.<<.wrap.>>.here
            }.<<(500, WRAP_CONTENT).alignParentBottom.centerHorizontal.>>.backgroundColor(Color.GRAY).here
          }.<<.fill.>>.visibility(View.INVISIBLE).here
          if (Game.Options.firstGame) {
            popUp.visibility(View.VISIBLE)
          }
        }.<<(MATCH_PARENT, 0).Weight(1).>>.here

        // buttons
        new SLinearLayout {
          new SVerticalLayout {
            for (range <- 0 until bruteIDs.length grouped 4) {
              this += new SLinearLayout {
                for (i <- range) {
                  new SVerticalLayout {
                    val bruteBitmap = BattleCanvas.bruteImages(bruteIDs(i)).head
                    val newButton = SImageButton(new BitmapDrawable(getResources(), bruteBitmap), {
                      // brute button clicked
                      for (sel <- currentSelection) {
                        for (otherSel <- selections.find(_.brute == bruteIDs(i))) {
                          // another selection has the selected brute, so swap them
                          otherSel.brute = sel.brute
                          if (sel.brute == null) {
                            otherSel.button.imageResource = R.drawable.unknown
                          } else {
                            otherSel.button.imageBitmap = BattleCanvas.bruteImages(sel.brute).head
                          }
                        }
                        sel.brute = bruteIDs(i)
                        sel.button.imageBitmap = bruteBitmap
                      }
                      for (sel <- nextSelection) {
                        selections.foreach(x => deselectButton(x.button))
                        selectButton(sel.button)
                        currentSelection = Some(sel)
                      }
                      enableButtons()
                    }).<<(MATCH_PARENT, 0).Weight(4).>>.scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(150 dip).minimumHeight(150 dip).adjustViewBounds(true).enabled(BruteAttributeMap(bruteIDs(i)).goldCost <= Game.game.map.startingGold)
                    bruteButtons = bruteButtons :+ newButton
                    // cost display
                    new SLinearLayout {
                      SImageView(new BitmapDrawable(getResources(), BattleCanvas.goldImage)).scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(20 dip).minimumHeight(20 dip).adjustViewBounds(true).<<.wrap.marginTop(4 dip).>>
                      new STextView {
                        text = s"${BruteAttributeMap(bruteIDs(i)).goldCost}"
                        textSize = 20 sp
                      }.<<.wrap.>>.here
                    }.<<(MATCH_PARENT,0).Weight(1).>>.gravity(Gravity.CENTER_HORIZONTAL).here
                  }.<<.wrap.>>.here
                }
              }.<<(MATCH_PARENT,0).Weight(1).>>
            }
          }.<<(0,MATCH_PARENT).Weight(6).>>.here
          new SVerticalLayout {
            for (i <- 0 until 4) {
              val newButton = SImageButton(R.drawable.unknown, {
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
        }.<<(MATCH_PARENT, 0).Weight(4).>>.here
      }
    )
    currentSelection = nextSelection
  }

  override def onResume(): Unit = {
    super.onResume()
    enableButtons()
    for (i <- 0 until 4) {
      if (selections(i).brute != null) {
        selections(i).button.imageBitmap = BattleCanvas.bruteImages(selections(i).brute).head
      }
      deselectButton(selections(i).button)
    }
    for (sel <- nextSelection) {
      selectButton(sel.button)
    }
  }
}

