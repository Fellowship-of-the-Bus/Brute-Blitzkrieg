package com.github.fellowship_of_the_bus.bruteb

import models._

import org.scaloid.common._
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.widget.ImageView
import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class Encyclopedia extends BaseActivity {
  import BruteID.Factory.{ids => bruteIDs}
  import TrapID.Factory.{ids => trapIDs}

  override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    val doBrute = getIntent().getBooleanExtra("brute", true)
    val txt = new STextView {
      text = if (doBrute) "Select a monster for more information." else "Select a trap for more information."
      textSize = 16 sp
    }
    val nametxt = new STextView {
      text = if (doBrute) "Monster Encyclopedia" else "Trap Encyclopedia"
      textSize = 20 sp
    }
    val valuetxt = new STextView {
      text = ""
      textSize = 16 sp
    }

    val img = new SImageView {
      imageResource=R.drawable.unknown
    }.scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(100 dip).adjustViewBounds(true)
    val ids = if (doBrute) bruteIDs else trapIDs
    setContentView(
      new SRelativeLayout {
        new SLinearLayout {
            new SVerticalLayout {
              new STextView {
                nametxt.<<.wrap.>>.here
              }.setGravity(Gravity.CENTER_HORIZONTAL)
              new SLinearLayout {
                img.<<.wrap.>>.here
                valuetxt.<<.wrap.>>.here
              }.<<.wrap.>>.here
              new SScrollView {
                txt.<<.wrap.>>.here
              }.<<.wrap.>>.here
          }.<<(0,WRAP_CONTENT).Weight(3).>>.here

          new SScrollView {
            new SVerticalLayout {
              for(i <- 0 until ids.length) {
                val image = if (doBrute) bruteIDs(i).image else trapIDs(i).image
                val name = if (doBrute) bruteIDs(i).name else trapIDs(i).name
                val description = if (doBrute) BruteAttributeMap(bruteIDs(i)).description else TrapAttributeMap(trapIDs(i)).description
                val values = if (doBrute) {
                  val brute = BruteAttributeMap(bruteIDs(i))
                  var baseValues = s"Hp: ${brute.maxHP}\nSpeed: ${(brute.moveSpeed * Game.ticksPerSecond).toInt} tiles per second\nCost:${brute.goldCost}"
                  if (brute.regen != 0) baseValues += s"\nRegen: ${(brute.regen * Game.ticksPerSecond).toInt} per second"
                  if (brute.radius != 0) baseValues += s"\nRange: ${(brute.radius).toInt} tiles"
                  if (brute.auraRegen != 0) baseValues += s"\nAura Regen: ${(brute.auraRegen * Game.ticksPerSecond).toInt} per second"
                  if (brute.flying) {
                    baseValues + "\nFlying"
                  } else {
                    baseValues
                  }
                } else {
                  val trap = TrapAttributeMap(trapIDs(i))
                  var stats = ""
                  if (trap.damage != 0) {
                    stats = stats + s"Damage: ${trap.damage.toInt}\n"
                  }
                  if (trap.duration != 0) {
                    stats = stats + f"Active Duration: ${trap.duration / Game.ticksPerSecond.toFloat}%.1f seconds\n"
                  }
                  if (trap.shotInterval != 0) {
                    val shotPerSec = Game.ticksPerSecond / trap.shotInterval
                    if (trapIDs(i) == ReuseTrapdoorID || trapIDs(i) == FlameVentID) stats = stats + s"Can activate every ${trap.shotInterval / Game.ticksPerSecond} seconds" else stats = stats + s"Shots per Second: $shotPerSec"
                  }
                  stats
                }
                SButton(name, {
                  txt.text = s"${description}"
                  img.imageResource = image
                  nametxt.text = name
                  valuetxt.text = values
                })
              }
            }.<<.wrap.>>.here
          }.<<(0,WRAP_CONTENT).Weight(1).>>.here
        }.<<.fill.>>.here
        if (Game.Options.firstGame) { // tutorial
          new SRelativeLayout {
            new SVerticalLayout {
              val text = new STextView {
                if (getIntent().getBooleanExtra("brute", true)) {
                  text = "Here you can view stats of each of the brutes."
                } else {
                  text = "Here you can view stats of each of the traps."
                }
                textSize = 20 sp
              }.<<.wrap.>>.here
              if (getIntent().getBooleanExtra("brute", true)) {
                SButton(R.string.NextButton, {
                  finish()
                  switchScreen(classOf[Encyclopedia],false)
                }).<<.fw
              } else {
                SButton(R.string.NextButton, {
                  // go to first level
                  finish()
                  switchScreen(MapID.Factory.ids(0), maps(MapID.Factory.ids(0)))
                }).<<.fw
              }

            }.<<(500, WRAP_CONTENT).alignParentBottom.centerHorizontal.>>.backgroundColor(Color.GRAY).here
          }.<<.fill.>>.here
        }
      }
    )
  }
}
