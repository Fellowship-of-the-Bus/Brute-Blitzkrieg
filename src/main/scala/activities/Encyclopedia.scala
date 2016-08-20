package com.github.fellowship_of_the_bus.bruteb

import models._

import org.scaloid.common._
import android.os.Bundle
import android.view.{Gravity,View}
import android.graphics.Color
import android.widget.{ImageView,Button}
import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class Encyclopedia extends BaseActivity {
  import BruteID.Factory.{ids => bruteIDs}
  import TrapID.Factory.{ids => trapIDs}

  override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    val txt = new STextView {
      text = "Select a brute for more information."
      textSize = 16 sp
    }
    val nametxt = new STextView {
      text = "Encyclopedia"
      textSize = 20 sp
    }
    val valuetxt = new STextView {
      text = ""
      textSize = 16 sp
    }

    val img = new SImageView {
      imageResource=R.drawable.unknown
    }.scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(100 dip).adjustViewBounds(true)
    var trapList: View = null
    var bruteList: View = null
    setContentView(
      new SRelativeLayout {
        new SVerticalLayout {
          new SLinearLayout {

            val bruteButton: Button = SButton("Brutes", {
                trapButton.enable
                bruteButton.disable
                trapList.visibility(View.INVISIBLE)
                bruteList.visibility(View.VISIBLE)
                nametxt.text = "Brute Encyclopedia"
                valuetxt.text = ""
                txt.text = "Select a brute for more information."
                img.imageResource=R.drawable.unknown
              }).<<(0,WRAP_CONTENT).Weight(1).>>.disable

            val trapButton: Button = SButton("Traps", {
                bruteButton.enable
                trapButton.disable
                bruteList.visibility(View.INVISIBLE)
                trapList.visibility(View.VISIBLE)
                nametxt.text = "Trap Encyclopedia"
                valuetxt.text = ""
                txt.text = "Select a trap for more information."
                img.imageResource=R.drawable.unknown
              }).<<(0,WRAP_CONTENT).Weight(1).>>
          }.<<(MATCH_PARENT,WRAP_CONTENT).>>.here

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
            new SRelativeLayout {
              bruteList = new SScrollView {
                new SVerticalLayout {
                  for(i <- 0 until bruteIDs.length) {
                    val image = bruteIDs(i).image
                    val name = bruteIDs(i).name
                    val description = BruteAttributeMap(bruteIDs(i)).description
                    val values = {
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
                    }
                    SButton(name, {
                      txt.text = s"${description}"
                      img.imageResource = image
                      nametxt.text = name
                      valuetxt.text = values
                    })
                  }
                }.<<.wrap.>>.here
              }.<<.fill.>>.here
              trapList = new SScrollView {
                new SVerticalLayout {
                  for(i <- 0 until trapIDs.length) {
                    val image = trapIDs(i).image
                    val name = trapIDs(i).name
                    val description = TrapAttributeMap(trapIDs(i)).description
                    val values = {
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
              }.<<.fill.>>.here.visibility(View.INVISIBLE)
            }.<<(0,WRAP_CONTENT).Weight(1).>>.here
          }.<<.fill.>>.here
        }.<<.fill.>>.here
      }
    )
  }
}
