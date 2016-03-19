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

class BattleActivity extends SActivity {
  override def onCreate(savedState: Bundle) {
    android.util.Log.e("bruteb", "Brute Blitzkrieg battle activity started")
    super.onCreate(savedState)

    val img = new SImageView {
      background=R.drawable.ahmed
    }

    val id = MapID.fromInt(getIntent().getIntExtra("level", -1))
    error(s"got level id $id")

    setContentView(
      new SLinearLayout {
        (new BattleCanvas).<<(0,MATCH_PARENT).Weight(3).>>.here

        new SRelativeLayout {
          SButton("Pick Minions", {
            android.util.Log.e("bruteb", "Trying to switch to Encyclopedia")
            val intent = new Intent(BattleActivity.this, classOf[Encyclopedia])
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
