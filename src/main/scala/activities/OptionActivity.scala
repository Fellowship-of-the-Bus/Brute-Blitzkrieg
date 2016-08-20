package com.github.fellowship_of_the_bus.bruteb

import models._

import org.scaloid.common._
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.widget.ImageView
import android.content.{Context, SharedPreferences}
import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

object OptionKeys {
  val tutorial = "ViewTutorial"
  val dancing = "Dancing"
}

class OptionActivity extends BaseActivity {
  import BruteID.Factory.{ids => bruteIDs}
  import TrapID.Factory.{ids => trapIDs}

  var options: SharedPreferences = null
  var editor: SharedPreferences.Editor = null

  override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    options = getSharedPreferences("Options", Context.MODE_PRIVATE)
    editor = options.edit()
    setContentView(
      new SRelativeLayout {
        new SVerticalLayout {
          val tutorialBox: SCheckBox = new SCheckBox("View Hints", {
            editor.putBoolean(OptionKeys.tutorial, tutorialBox.checked)
            editor.commit()
            Game.Options.tutorial = tutorialBox.checked
          }).<<.wrap.>>.checked(options.getBoolean(OptionKeys.tutorial, true)).here
          val danceBox: SCheckBox = new SCheckBox("Dancing", {
            editor.putBoolean(OptionKeys.dancing, danceBox.checked)
            editor.commit()
            Game.Options.dancing = danceBox.checked
          }).<<.wrap.>>.checked(options.getBoolean(OptionKeys.dancing, false)).here
          val resetButton = SButton("Reset Progress", {
            val builder = new AlertDialogBuilder("Confirm Reset") {
              negativeButton("Cancel")
              positiveButton("Confirm", resetProgress())
            }
            builder.setView({
              new STextView() {
                text = "Erase all progress in the preset levels?\n(This does not erase progress in user created levels.)"
                textSize = 16 sp
              }.<<.wrap.>>.padding(15 sp).gravity(Gravity.CENTER)
            })  
            builder.show
          }).<<.wrap.>>
        }.<<.fill.>>.gravity(Gravity.CENTER).here
      }
    )
  }
  def resetProgress(): Unit = {            
    val data = getSharedPreferences("UserProgress", Context.MODE_PRIVATE)
    val dataEditor = data.edit()
    for (level <- MapID.Factory.ids) {
      dataEditor.putInt(level.id, 0)
    }
    dataEditor.commit()
    ()
  }
}
