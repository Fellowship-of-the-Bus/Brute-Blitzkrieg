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
    val prefs = getSharedPreferences("UserProgress", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    //first time setup
    if (!prefs.contains("level1")) {
      for (range <- 1 to 12) {
        editor.putInt(s"level${range}", 0)
      }
    }
    editor.commit()
    setContentView(

      new SLinearLayout {
        new STableLayout {
          for (range <- 1 to 12 grouped 3) {
            this += new STableRow {
              for (i <- range) {
                new SVerticalLayout {
                  SButton(s"Level $i", switchScreen(i)).<<.fill.>>
                  new SLinearLayout {
                    // if (prefs.getInt(s"level${i}") == 0) {
                    //   this += SImageView(R.drawable.grey_star).<<(50,50).here
                    //   this += SImageView(R.drawable.grey_star).<<(50,50).here
                    //   this += SImageView(R.drawable.grey_star).<<(50,50).here
                    // } else {
                      for (index <- 1 to prefs.getInt(s"level$i",0 )) {
                        SImageView(R.drawable.star).<<(50,50)
                      }
                      for (index <- prefs.getInt(s"level$i", 0)+1 to 3) {
                        SImageView(R.drawable.grey_star).<<(50,50)
                      }
                    //}
                    // SImageView(R.drawable.grey_star).<<(50,50)
                    // SImageView(R.drawable.star).<<(50,50)
                    // SImageView(R.drawable.grey_star).<<(50,50)
                  }.gravity(Gravity.CENTER).here
                }
              }.here
            }
          }
        }.<<(0, MATCH_PARENT).Weight(3).>>.here//.stretchColumns("*")
        /*var grid = new SGridLayout {
          SButton("Level One").<<.wrap.>>
          SButton("Level Two").<<.wrap.>>
          SButton("Level Three").<<.wrap.>>
          SButton("Level Four").<<.wrap.>>
          SButton("Level Five").<<.wrap.>>
          SButton("Level Six").<<.wrap.>>
          SButton("Level Seven").<<.wrap.>>
          SButton("Level Eight").<<.wrap.>>
          SButton("Level Nine").<<.wrap.>>
          SButton("Level Ten").<<.wrap.>>
          SButton("Level Eleven").<<.wrap.>>
          SButton("Level Twelve").<<.wrap.>>
        }.<<.wrap.>>.columnCount(3).columnOrderPreserved(true).here*/
        new SRelativeLayout {
          SButton(R.string.MinionButton, switchScreen(classOf[Encyclopedia],true)).<<.wrap.alignParentTop.>>
          SButton(R.string.TrapButton, switchScreen(classOf[Encyclopedia],false)).<<.wrap.centerVertical.>>
          SButton(R.string.QuitButton, finish()).<<.wrap.alignParentBottom.>>
        }.<<(0,MATCH_PARENT).Weight(1).>>.gravity(Gravity.RIGHT).here
      }
    )
  }
}
