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
              //new SView{}.<<(0,WRAP_CONTENT).Weight(1).>>.here
              //new STableLayout {

              //}.<<(0,MATCH_PARENT).Weight(2).>>.here
              //new SView{}.<<(0,WRAP_CONTENT).Weight(2).>>.here
              new STableLayout {
                new STableRow {
                  SButton(R.string.LevelSelectButton, switchScreen(classOf[MainActivity],false)).<<.wrap.>>
                }.<<.wrap.>>.here
                new STableRow {
                  SButton(R.string.OptionButton, switchScreen(classOf[OptionActivity],false)).<<.wrap.>>
                }.<<.wrap.>>.here
                new STableRow {
                  SButton(R.string.LevelEditorButton, switchScreen(classOf[LevelEditor],false)).<<.wrap.>>
                }.<<.wrap.>>.here
                new STableRow {
                  SButton(R.string.EncyclopediaButton, switchScreen(classOf[Encyclopedia],false)).<<.wrap.>>
                }.<<.wrap.>>.here
              }.<<.fill.>>.gravity(Gravity.CENTER_HORIZONTAL).here
              //new SView{}.<<(0,WRAP_CONTENT).Weight(1).>>.here
            }.here
          }.<<.fill.marginTop(10 dip).>>.gravity(Gravity.CENTER_HORIZONTAL).here
      }.background(R.drawable.mainsplash)
    )
  }
}
