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

  def switchScreen(id: Int) = {
    android.util.Log.e("bruteb", s"Trying to switch to BattleActivity")
    val intent = new Intent(this, classOf[PreBattleActivity])
    intent.putExtra("level", id)
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
        new STableLayout {
          import MapID.Factory.ids
          def stars(i: Int): Int = prefs.getInt(ids.lift(i).map(_.id).getOrElse("No Key"), 0)
          for (range <- 1 to ids.length grouped 3) {
            this += new STableRow {
              for (i <- range) {
                new SVerticalLayout {
                  SButton(s"Level $i", switchScreen(i)).<<.fill.>>
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
