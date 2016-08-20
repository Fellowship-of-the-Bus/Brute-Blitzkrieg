package com.github.fellowship_of_the_bus.bruteb

import models._

// import android.app.Activity
import org.scaloid.common._
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.graphics.drawable.{Drawable,BitmapDrawable, LayerDrawable}
import android.widget.GridView
import android.content.{Intent, Context}
import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class MainActivity extends BaseActivity {

  override def onRestart() = {
    super.onRestart()
    finish();
    startActivity(getIntent());
  }


  override def onCreate(savedState: Bundle) {
    android.util.Log.e("bruteb", "Brute Blitzkrieg main activity started")
    super.onCreate(savedState)
    Game.res = getResources
    val prefs = getSharedPreferences("UserProgress", Context.MODE_PRIVATE)
    val options = getSharedPreferences("Options", Context.MODE_PRIVATE)
    Game.Options.tutorial = options.getBoolean("ViewTutorial", true)

    setContentView(

      // number of stars earned in level i
      new SRelativeLayout {
        new SLinearLayout {
          new SScrollView {
            new STableLayout {
              import MapID.Factory.ids
              def stars(i: Int): Int = stars(ids.lift(i-1).map(_.id).getOrElse("No Key"))
              def stars(key: String): Int = prefs.getInt(key, 0)
              for (range <- 1 to ids.length grouped 4) {
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
              for (range <- 1 to filenames.length grouped 4) {
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
          }.<<.fill.>>.here
        }.<<.fill.>>.here.background(R.drawable.mainsplash)
      }
    )
  }
}
