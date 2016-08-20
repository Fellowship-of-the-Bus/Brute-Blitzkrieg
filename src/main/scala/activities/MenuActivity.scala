package com.github.fellowship_of_the_bus.bruteb

import models._

// import android.app.Activity
import org.scaloid.common._
import android.os.Bundle
import android.view.Gravity
import android.graphics.{Color, Point}
import android.graphics.drawable.{Drawable,BitmapDrawable, LayerDrawable}
import android.widget.GridView
import android.content.{Intent, Context}
import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class MenuActivity extends BaseActivity {

  override def onRestart() = {
    super.onRestart()
    finish();
    startActivity(getIntent());
  }


  override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    Game.res = getResources
    val point = new Point()
    getWindowManager().getDefaultDisplay().getSize(point)

    setContentView(

      // number of stars earned in level i
      new SRelativeLayout {
          new SVerticalLayout {
            SImageView(R.drawable.brute_blitzkrieg).<<(point.x/2,WRAP_CONTENT).>>.adjustViewBounds(true)
            new SLinearLayout {
              new SView{}.<<(0,WRAP_CONTENT).Weight(4).>>.here
              new STableLayout {
                new STableRow {
                  SButton(R.string.LevelSelectButton, switchScreen(classOf[MainActivity],false)).<<.fw.>>
                }.<<.wrap.>>.here
                new STableRow {
                  SButton(R.string.OptionButton, switchScreen(classOf[OptionActivity],false)).<<.fw.>>
                }.<<.wrap.>>.here
                new STableRow {
                  SButton(R.string.LevelEditorButton, switchScreen(classOf[LevelEditor],false)).<<.fw.>>
                }.<<.wrap.>>.here
                new STableRow {
                  SButton(R.string.BruteButton, switchScreen(classOf[Encyclopedia],true)).<<.fw.>>
                }.<<.wrap.>>.here
                new STableRow {
                  SButton(R.string.TrapButton, switchScreen(classOf[Encyclopedia],false)).<<.fw.>>
                }.<<.wrap.>>.here
              }.<<(0,WRAP_CONTENT).Weight(1).>>.gravity(Gravity.CENTER_HORIZONTAL).here
              new SView{}.<<(0,WRAP_CONTENT).Weight(1).>>.here
            }.here
          }.<<.fill.marginTop(10 dip).>>.gravity(Gravity.CENTER_HORIZONTAL).here
      }.background(R.drawable.mainsplash)
    )
  }
}
