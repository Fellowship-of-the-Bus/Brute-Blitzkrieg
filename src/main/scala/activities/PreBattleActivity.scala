package com.github.fellowship_of_the_bus.bruteb

import models._

import org.scaloid.common._

// import android.app.Activity
import android.os.Bundle
import android.view.Gravity
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

    val mapId: MapID = MapID.fromInt(getIntent().getIntExtra("level", -1))
    game = new Game(maps(mapId), mapId)
    error(s"got level id $mapId")


    setContentView(
      new SLinearLayout {
        (new BattleCanvas(game.map)).<<(0,MATCH_PARENT).Weight(3).>>.here

        new SRelativeLayout {
          SButton("Pick Minions", {
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
        }.<<(0, MATCH_PARENT).Weight(1).>>.gravity(Gravity.RIGHT).here
      }
    )
  }

  override def onResume(): Unit = {
    super.onResume()
    startButton.enabled = game.brutes.forall(_ != null)
    ()
  }
}
