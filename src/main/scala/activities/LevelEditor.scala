package com.github.fellowship_of_the_bus
package bruteb

import models._

import org.scaloid.common._

// import android.app.Activity
import android.os.Bundle
import android.view.{Gravity, View}
import android.widget.{GridView, ImageView, AdapterView}
import android.content.{Intent, Context}
import android.graphics.{Color, Canvas}
import android.graphics.drawable.BitmapDrawable
import android.text.InputType

import java.util.concurrent.atomic.AtomicInteger
import java.util.Arrays

import scala.language.postfixOps

object LevelEditor {
  implicit class RichEditText(val view: SEditText) extends AnyVal {
    def int() = view.text.toString.toInt
    def str() = view.text.toString
  }
}
import LevelEditor._

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
    def map = game.map

    setContentView(
      new SLinearLayout {
        (new BattleCanvas(map, true)).<<(0,MATCH_PARENT).Weight(3).>>.here.onTouch {
          (view, event) => {
            val canvas = view.asInstanceOf[BattleCanvas]
            val (x, y) = (event.getX.toInt/canvas.cellX, event.getY.toInt/canvas.cellY)
            for (sel <- currentSelection; if (x < MapID.width)) {
              error(s"$x $y ${sel.trap}")
              sel.trap match {
                case _: models.FloorTrapID => map.tiles(y)(x).floorTrapID = sel.trap
                case _: models.WallTrapID => map.tiles(y)(x).wallTrapID = sel.trap
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

            var doneFunction = () => ()

            // open dialog
            val builder = new AlertDialogBuilder("Save the Map")
            builder.setView(new STableLayout {
              val name = SEditText().hint("Name").singleLine(true)
              val gold = SEditText().hint("Gold").inputType(numeric)
              val starOne = SEditText().hint("One Star Value").inputType(numeric)
              val starTwo = SEditText().hint("Two Star Value").inputType(numeric)
              val starThree = SEditText().hint("Three Star Value").inputType(numeric)

              doneFunction = () => {
                import scala.language.implicitConversions
                val out = openFileOutput(name.str, Context.MODE_WORLD_READABLE)
                val customID = Custom(name.str)
                game = new Game(
                  map.copy(
                    startingGold = gold.int,
                    oneStar = starOne.int,
                    twoStar = starTwo.int,
                    threeStar = starThree.int
                  ), customID
                )

                import rapture.json._
                import rapture.json.jsonBackends.jackson._
                val json: Json = Json(Map[String, MapInfo](customID.id -> map))
                out.write((json.toString + "\n").getBytes)
                android.util.Log.e("bruteb", s"${json} stored in ${getFilesDir}")
                android.util.Log.e("bruteb", s"${getFilesDir.listFiles.mkString(" ")}")
              }
              // new STableRow {
              //   val starOne = SSeekBar().max(0)
              //   val starTwo = SSeekBar().max(0)
              // }.here
            })
            builder.positiveButton("Done", doneFunction())
            builder.show
          }).<<.fw.>>
          SButton(R.string.LoadButton, {
            val files = customMapFiles
            var index = -1
            val listView = new SListView
            listView.adapter = SArrayAdapter(files.map{ _.getName })
            listView.onItemClick((parent: AdapterView[_], item: View, idx: Int, id: Long) => index = idx)

            val builder = new AlertDialogBuilder("Load a Map")
            builder.setView(listView)
            builder.positiveButton("Load", {
              val file = files(index)
              game = new Game(loadCustom(file), Custom(file.getName))
            })
            builder.show
          })
        }.<<(0, MATCH_PARENT).Weight(1).>>.gravity(Gravity.RIGHT).here
      }
    )
  }
}
