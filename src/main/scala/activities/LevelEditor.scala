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
import android.text.{InputType,InputFilter,Spanned}

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

  case class LevelAttributes(name: String, gold: Int, starOne: Int, starTwo: Int, starThree: Int)
  var attributes: Option[LevelAttributes] = None
  def saveAttributes(name: String, map: MapInfo): Unit = {
    attributes = Some(LevelAttributes(name, map.startingGold, map.oneStar, map.twoStar, map.threeStar))
  }

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

    val battleCanvas = new BattleCanvas(map, true)

    setContentView(
      new SRelativeLayout {
        new SLinearLayout {
          battleCanvas.<<(0,MATCH_PARENT).Weight(3).>>.here.onTouch {
            (view, event) => {
              val canvas = view.asInstanceOf[BattleCanvas]
              val (x, y) = (event.getX.toInt/canvas.cellX, event.getY.toInt/canvas.cellY)
              if (x > 0 && x < MapID.width-1 && y >= 0 && y < MapID.height) {
                deleteButton.setX(x*canvas.cellX)
                deleteButton.setY(y*canvas.cellY)
                // can't put anything on far edges of the map
                for (sel <- currentSelection; if (x < MapID.width)) {
                  error(s"$x $y ${sel.trap}")
                  val trapAdded = sel.trap match {
                    case id: models.FloorTrapID if ((id != TrapdoorID && id != ReuseTrapdoorID) || y < MapID.height-1) =>
                      map.tiles(y)(x).floorTrapID = sel.trap
                      true
                    case _: models.WallTrapID =>
                      map.tiles(y)(x).wallTrapID = sel.trap
                      true
                    case _ => false
                  }
                  if (trapAdded) {
                    game.removeTrap(sel.trap, Coordinate(x, y))
                    game.addTrapFromID(sel.trap, Coordinate(x, y))
                  }
                }
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
                val txtfilts = Array[InputFilter](new InputFilter.LengthFilter(40))
                val numfilts = Array[InputFilter](new InputFilter.LengthFilter(9))

                val name = SEditText().hint("Name").singleLine(true).filters(txtfilts)
                val gold = SEditText().hint("Gold").inputType(numeric).filters(numfilts)
                val starOne = SEditText().hint("One Star Value").inputType(numeric).filters(numfilts)
                val starTwo = SEditText().hint("Two Star Value").inputType(numeric).filters(numfilts)
                val starThree = SEditText().hint("Three Star Value").inputType(numeric).filters(numfilts)
                for (attr <- attributes) {
                  // set defaults based on the currently loaded map
                  name.text(attr.name)
                  gold.text(attr.gold.toString)
                  starOne.text(attr.starOne.toString)
                  starTwo.text(attr.starTwo.toString)
                  starThree.text(attr.starThree.toString)
                }

                def writeFile(): Unit = {
                  import scala.language.implicitConversions
                  val out = openFileOutput(name.str, Context.MODE_WORLD_READABLE)
                  val customID = Custom(name.str)
                  val newMap = map.copy(
                    startingGold = gold.int,
                    oneStar = starOne.int,
                    twoStar = starTwo.int,
                    threeStar = starThree.int
                  )
                  saveAttributes(name.str, newMap)
                  game = new Game(newMap, customID)

                  import rapture.json._
                  import rapture.json.jsonBackends.jackson._
                  val json: Json = Json(Map[String, MapInfo](customID.id -> map))
                  out.write((json.toString + "\n").getBytes)
                  android.util.Log.e("bruteb", s"${json} stored in ${getFilesDir}")
                  android.util.Log.e("bruteb", s"${getFilesDir.listFiles.mkString(" ")}")

                  // reset level's completion
                  val data = getSharedPreferences("UserProgress", Context.MODE_PRIVATE)
                  val editor = data.edit()
                  editor.putInt(customID.id, 0)
                  val _ = editor.commit()
                }

                doneFunction = () => {
                  saveAttributes(name.str, map)
                  if (customMapFiles.exists(x => x.getName == name.str)) {
                    // ask for confirmation before overwriting map
                    val _ = new AlertDialogBuilder("Overwrite Map?") {
                      negativeButton("Cancel")
                      positiveButton("Confirm", writeFile())
                    }.show
                  } else {
                    writeFile()
                  }
                }
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
                val map = loadCustom(file)
                saveAttributes(file.getName, map)
                game = new Game(map, Custom(file.getName))
              })
              builder.negativeButton("Delete", {
                val file = files(index)
                val _ = new AlertDialogBuilder(s"Delete Map '${file.getName}'? This cannot be undone.") {
                  negativeButton("Cancel")
                  positiveButton("Confirm", { file.delete(); () })
                }.show
                val map = loadCustom(file)
                saveAttributes(file.getName, map)
                game = new Game(map, Custom(file.getName))
              })
              builder.show
            })
          }.<<(0, MATCH_PARENT).Weight(1).>>.gravity(Gravity.RIGHT).here

          // object ContextMenu extends SView {
          //   // var tilex: Int = 0
          //   // var tiley: Int = 0

          //   override def onDraw(canvas: Canvas) = {
          //     button.setX(tilex*battleCanvas.cellX)
          //     button.setY(tiley*battleCanvas.cellY)
          //     button.draw(canvas)
          //   }
          // }
          // ContextMenu.visibility(View.INVISIBLE).here
        }.here
        val deleteButton: SImageButton = SImageButton(R.drawable.delete, {
          val x = deleteButton.getX
          val y = deleteButton.getY
          val tilex = (x/battleCanvas.cellX).toInt
          val tiley = (y/battleCanvas.cellY).toInt
          for (sel <- currentSelection; if (tilex < MapID.width)) {
            sel.trap match {
              case _: models.FloorTrapID => map.tiles(tiley)(tilex).floorTrapID = NoTrapID
              case _: models.WallTrapID => map.tiles(tiley)(tilex).wallTrapID = NoTrapID
              case _ => ()
            }
            error(s"delete ${sel.trap} $tilex $tiley $x $y")
            game.removeTrap(sel.trap, Coordinate(tilex, tiley))
          }
        }).scaleType(ImageView.ScaleType.CENTER_INSIDE).padding(6 dip).adjustViewBounds(true).<<(25 dip, 25 dip).>>.visibility(View.VISIBLE)

      }
    )
  }
}
