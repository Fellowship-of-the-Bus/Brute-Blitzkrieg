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
  var popUp: SLinearLayout = null


  override def onCreate(savedState: Bundle): Unit = {
    android.util.Log.e("bruteb", "Brute Blitzkrieg pre battle activity started")
    super.onCreate(savedState)

    game = new Game(Game.map.copy(), Game.mapId)


    setContentView(
      new SRelativeLayout{
        new SLinearLayout {
          (new BattleCanvas(game.map)).<<(0,MATCH_PARENT).Weight(3).>>.here

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
        if (Game.Options.firstGame) {
          popUp = new SLinearLayout {
            new SView().<<(0, MATCH_PARENT).Weight(3).>>.here
            new SRelativeLayout {
              val text = new STextView {
                text = "After selecting the level, you need to select 4 brutes to use in the level.\nOn the top Left, you have the amount of gold available in the level."
                textSize = 20 sp
              }.<<.wrap.alignParentTop.>>.here
              val button = SButton(R.string.NextButton, switchScreen(classOf[BruteSelectActivity],true)).<<.fw.alignParentBottom
            }.<<(0, MATCH_PARENT).Weight(1).>>.backgroundColor(Color.GRAY).here
          }.<<.fill.>>.here
        }
      }
    )
  }

  override def onRestart(): Unit = {
    super.onRestart()
    if (popUp != null) {
      popUp.visibility(View.GONE)
    }
    ()
  }

  override def onResume(): Unit = {
    super.onResume()
    startButton.enabled = game.brutes.exists(_ != null)
    ()
  }
}
