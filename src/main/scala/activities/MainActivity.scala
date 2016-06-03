package com.github.fellowship_of_the_bus.bruteb

import models._

// import android.app.Activity
import org.scaloid.common._
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.widget.GridView
import android.content.{Intent, Context}
import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class MainActivity extends BaseActivity {
  def switchScreen[T](`class`: Class[T], b : Boolean) = {
    android.util.Log.e("bruteb", s"Trying to switch to ${`class`}")
    val intent = new Intent(this, `class`)
    intent.putExtra("brute",b)
    startActivity(intent)
  }

  def switchScreen(id: MapID, map: MapInfo) = {
    android.util.Log.e("bruteb", s"Trying to switch to BattleActivity")
    val intent = new Intent(this, classOf[PreBattleActivity])
    Game.mapId = id
    Game.map = map
    startActivity(intent)
  }



  override def onCreate(savedState: Bundle) {
    android.util.Log.e("bruteb", "Brute Blitzkrieg main activity started")
    super.onCreate(savedState)
    Game.res = getResources
    val prefs = getSharedPreferences("UserProgress", Context.MODE_PRIVATE)
    setContentView(

      // number of stars earned in level i

      new SLinearLayout {
        new SScrollView {
          new STableLayout {
            import MapID.Factory.ids
            def stars(i: Int): Int = stars(ids.lift(i-1).map(_.id).getOrElse("No Key"))
            def stars(key: String): Int = prefs.getInt(key, 0)
            for (range <- 1 to ids.length grouped 3) {
              this += new STableRow {
                for (i <- range) {
                  val mapid: MapID = ids(i-1)
                  new SVerticalLayout {
                    SButton(s"Level $i", switchScreen(mapid, maps(mapid))).<<.fill.>>
                    new SLinearLayout {
                      for (index <- 1 to stars(i)) {
                        SImageView(R.drawable.star).<<(50,50)
                      }
                      for (index <- stars(i)+1 to 3) {
                        SImageView(R.drawable.grey_star).<<(50,50)
                      }
                    }.gravity(Gravity.CENTER).here
                  }
                }.here
              }
            }
            // load custom maps
            val filenames = customMapFiles.map { _.getName }
            for (range <- 1 to filenames.length grouped 3) {
              this += new STableRow {
                for (i <- range) {
                  new SVerticalLayout {
                    val name = filenames(i-1)
                    val customid = Custom(name).id
                    SButton(name, switchScreen(Custom(name), loadCustom(customMapFiles(i-1)))).<<.fill.>>
                    new SLinearLayout {
                      for (index <- 1 to stars(customid)) {
                        SImageView(R.drawable.star).<<(50,50)
                      }
                      for (index <- stars(customid)+1 to 3) {
                        SImageView(R.drawable.grey_star).<<(50,50)
                      }
                    }.gravity(Gravity.CENTER).here
                  }
                }.here
              }
            }
          }.<<.fill.>>.here
        }.<<(0, MATCH_PARENT).Weight(3).>>.here
        new STableLayout {
          new STableRow {
            SButton(R.string.MinionButton, switchScreen(classOf[Encyclopedia],true)).<<.fw.>>
          }.<<.wrap.>>.here
          new STableRow {
            SButton(R.string.TrapButton, switchScreen(classOf[Encyclopedia],false)).<<.fw.>>
          }.<<.wrap.>>.here
          new STableRow {
            SButton(R.string.LevelEditorButton, switchScreen(classOf[LevelEditor],false)).<<.fw.>>
          }.<<.wrap.>>.here
          new STableRow {
            SButton(R.string.QuitButton, finish()).<<.fw.>>
          }.<<.wrap.>>.here
        }.<<(0,MATCH_PARENT).Weight(1).>>.gravity(Gravity.RIGHT).here
      }
    )
  }
}
