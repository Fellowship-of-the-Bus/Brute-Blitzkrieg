package com.github.fellowship_of_the_bus.bruteb

import models._

import org.scaloid.common._

// import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.widget.GridView
import android.content.Intent
import android.graphics.Canvas

import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class BattleActivity extends BaseActivity {
  override def onCreate(savedState: Bundle) {
    android.util.Log.e("bruteb", "Brute Blitzkrieg battle activity started")
    super.onCreate(savedState)

    val mapId: MapID = MapID.fromInt(getIntent().getIntExtra("level", -1))
    Game.game = new Game(maps(mapId))
    error(s"got level id $mapId")

    setContentView(
      new SLinearLayout {
        (new BattleCanvas(Game.game.map)).<<(0,MATCH_PARENT).Weight(3).>>.here

        new SRelativeLayout {
          SButton("Pick Minions", {
            val intent = new Intent(BattleActivity.this, classOf[BruteSelectActivity])
            startActivity(intent)
          }).<<.wrap.alignParentTop.>>
          SButton("Start Level").<<.wrap.centerVertical.>>
          SButton("Menu", {
            finish()
            }).<<.wrap.alignParentBottom.>>
        }.<<(0, MATCH_PARENT).Weight(1).>>.gravity(Gravity.RIGHT).here
      }
    )
  }
}
