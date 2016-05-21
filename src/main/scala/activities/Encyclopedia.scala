package com.github.fellowship_of_the_bus.bruteb

import models._

// import android.app.Activity
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
    val txt = new STextView {
      text = "Select a monster for more information."
      textSize = 16 dip
    }
    val nametxt = new STextView {
      text = "Monster Encyclopedia"
      textSize = 20 dip
    }
    val valuetxt = new STextView {
      text = ""
      textSize = 16 dip
    }

    val img = new SImageView {
      imageResource=R.drawable.unknown
    }.scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(100 dip).adjustViewBounds(true)
    val doBrute = getIntent().getBooleanExtra("brute", true)
    val ids = if (doBrute) bruteIDs else trapIDs
    setContentView(

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
                val baseValues = s"Hp: ${brute.maxHP}\nSpeed: ${brute.moveSpeed}\nCost:${brute.goldCost}"
                if (brute.flying) {
                  baseValues + "\nFlying"
                } else {
                  baseValues
                }
              } else {
                val trap = TrapAttributeMap(trapIDs(i))
                var stats = ""
                if (trap.damage != 0) {
                  stats = stats + s"Damage: ${trap.damage}\n"
                }
                if (trap.duration != 0) {
                  stats = stats + s"Active Duration: ${trap.duration}\n"
                }
                val shotPerSec = 20.0f / trap.shotInterval
                stats + f"Shots per Second: $shotPerSec%2.2f"
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
      }
    )
  }
}
