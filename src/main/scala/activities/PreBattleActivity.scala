package com.github.fellowship_of_the_bus.bruteb

import models._

import org.scaloid.common._

import android.os.Bundle
import android.view.{Gravity, View}
import android.graphics.Color
import android.widget.GridView
import android.content.{Intent, Context}
import android.graphics.Canvas

import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class PreBattleActivity extends BaseActivity {
  import Game.game
  var startButton: SButton = null


  override def onCreate(savedState: Bundle): Unit = {
    android.util.Log.e("bruteb", "Brute Blitzkrieg pre battle activity started")
    super.onCreate(savedState)

    setContentView(
      new SRelativeLayout{
        new SLinearLayout {
          (new BattleCanvas()).<<(0,MATCH_PARENT).Weight(3).>>.here

          new SRelativeLayout {
            SButton("Pick Brutes", {
              val intent = new Intent(PreBattleActivity.this, classOf[BruteSelectActivity])
              startActivity(intent)
            }).<<.wrap.alignParentTop.>>
            startButton = SButton("Start Level", {
              val intent = new Intent(PreBattleActivity.this, classOf[BattleActivity])
              startActivity(intent)
            }).<<.wrap.centerVertical.>>
            SButton("Menu", {
              finish()
            }).<<.wrap.alignParentBottom.>>
          }.<<(0, MATCH_PARENT).Weight(1).>>.gravity(Gravity.CENTER_HORIZONTAL).here
        }.<<.fill.>>.here
      }
    )
  }

  override def onRestart(): Unit = {
    super.onRestart()
    ()
  }

  override def onResume(): Unit = {
    super.onResume()
    startButton.enabled = game.brutes.exists(_ != null)

    ()
  }
}
