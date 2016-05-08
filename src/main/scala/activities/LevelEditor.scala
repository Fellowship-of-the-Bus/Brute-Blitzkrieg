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
import android.text.InputType

import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class LevelEditor extends BaseActivity {
  case class Selection(val button: SImageButton, var trap: TrapID)
  var selections = Vector[Selection]()

  import Game.game
  var startButton: SButton = null

  import TrapID.Factory.{ids => trapIDs}


  override def onCreate(savedState: Bundle): Unit = {
    var currentSelection: Option[Selection] = None
    android.util.Log.e("bruteb", "Brute Blitzkrieg level editor started")
    super.onCreate(savedState)

    game = new Game(
      new MapInfo(
        List.fill(MapID.height, MapID.width)(new Tile(NoTrapID, NoTrapID)), 0, 0, 0, 0
      ),
      Level1
    )

    setContentView(
      new SLinearLayout {
        (new BattleCanvas(game.map, true)).<<(0,MATCH_PARENT).Weight(3).>>.here.onTouch {
          (view, event) => {
            val canvas = view.asInstanceOf[BattleCanvas]
            val (x, y) = (event.getX.toInt/canvas.cellX, event.getY.toInt/canvas.cellY)
            for (sel <- currentSelection; if (x < MapID.width)) {
              error(s"$x $y ${sel.trap}")
              sel.trap match {
                case _: models.FloorTrapID => game.map.tiles(y)(x).floorTrapID = sel.trap
                case _: models.WallTrapID => game.map.tiles(y)(x).wallTrapID = sel.trap
                case _ => ()
              }
              game.removeTrap(sel.trap, Coordinate(x, y))
              game.addTrapFromID(sel.trap, Coordinate(x, y))
            }
          }
          true
        }

        new STableLayout {
          for (range <- 0 until trapIDs.length grouped 2) {
            this += new STableRow {
              for (i <- range) {
                val newButton = SImageButton(new BitmapDrawable(getResources(), BattleCanvas.trapImages(trapIDs(i))), {
                  // selection clicked
                  selections.foreach(x => deselectButton(x.button))
                  selectButton(selections(i).button)
                  currentSelection = Some(selections(i))
                }).scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(150 dip).minimumHeight(150 dip).adjustViewBounds(true)
                selections = selections :+ Selection(newButton, trapIDs(i))
              }
            }
          }
          SButton(R.string.SaveButton, {
            val numeric = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL
            // open dialog
            val builder = new AlertDialogBuilder("Save the Map").setView(new STableLayout {
              val name = SEditText().hint("Name")
              val gold = SEditText().hint("Gold").inputType(numeric)
              val maxStar = SEditText().hint("Max Star Value").inputType(numeric)
              new STableRow {
                val SSeekBar().max(0)
                SSeekBar().max(0)
              }.here
            }).positiveButton("Done").show
          }).<<.fw.>>
        }.<<(0, MATCH_PARENT).Weight(1).>>.gravity(Gravity.RIGHT).here
      }
    )
  }
}
