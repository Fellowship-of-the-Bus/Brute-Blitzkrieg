package com.github.fellowship_of_the_bus.bruteb

import models._

import org.scaloid.common._

// import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.widget.GridView
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView

import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class LevelEditor extends BaseActivity {
  import Game.game
  var startButton: SButton = null

  import TrapID.Factory.{ids => trapIDs}

  override def onCreate(savedState: Bundle): Unit = {
    android.util.Log.e("bruteb", "Brute Blitzkrieg level editor started")
    super.onCreate(savedState)

    // game = new Game(maps(mapId))
    // error(s"got level id $mapId")

    //

    setContentView(
      new SLinearLayout {
        (new BattleCanvas(game.map)).<<(0,MATCH_PARENT).Weight(3).>>.here

        new STableLayout {
          for (range <- 0 until trapIDs.length grouped 2) {
            this += new STableRow {
              for (i <- range) {
                val newButton = SImageButton(new BitmapDrawable(getResources(), BattleCanvas.trapImages(trapIDs(i))), {
                  // // brute button clicked
                  // val cur = if (currentSelection.isEmpty) {
                  //   val next = nextSelection
                  //   for (sel <- next) {
                  //     deselectButton(sel.button)
                  //   }
                  //   next
                  // } else {
                  //   currentSelection
                  // }
                  // for (sel <- cur) {
                  //   sel.brute = bruteIDs(i)
                  //   sel.button.imageBitmap = BattleCanvas.bruteImages(sel.brute).head
                  // }
                  // if (currentSelection.isEmpty) {
                  //   for (sel <- nextSelection) {
                  //     selectButton(sel.button)
                  //   }
                  // }
                  // enableButtons()
                }).scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(150 dip).minimumHeight(150 dip)//.adjustViewBounds(true)
                // bruteButtons = bruteButtons :+ newButton
              }
            }
          }
        }.<<(0, MATCH_PARENT).Weight(1).>>.gravity(Gravity.RIGHT).here

        // new SRelativeLayout {
        //   SButton

        //   SButton("Pick Minions", {
        //     val intent = new Intent(PreBattleActivity.this, classOf[BruteSelectActivity])
        //     startActivity(intent)
        //   }).<<.wrap.alignParentTop.>>
        //   startButton = SButton("Start Level", {
        //     val intent = new Intent(PreBattleActivity.this, classOf[BattleActivity])
        //     startActivity(intent)
        //   }).<<.wrap.centerVertical.>>
        //   SButton("Menu", {
        //     finish()
        //   }).<<.wrap.alignParentBottom.>>
        // }.<<(0, MATCH_PARENT).Weight(1).>>.gravity(Gravity.RIGHT).here
      }
    )
  }

  override def onResume(): Unit = {
    super.onResume()
    // startButton.enabled = game.brutes.forall(_ != null)
    ()
  }
}
