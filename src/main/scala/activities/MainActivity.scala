package com.github.fellowship_of_the_bus.bruteb

import models._

// import android.app.Activity
import org.scaloid.common._
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.widget.GridView
import android.content.Intent
import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class MainActivity extends SActivity {
  override implicit val loggerTag = new LoggerTag("bruteb")

  val viewSeq = new AtomicInteger(0)

  def switchScreen[T](`class`: Class[T]) = {
    android.util.Log.e("bruteb", s"Trying to switch to ${`class`}")
    val intent = new Intent(this, `class`)
    startActivity(intent)
  }

  def switchScreen(id: Int) = {
    android.util.Log.e("bruteb", s"Trying to switch to BattleActivity")
    val intent = new Intent(this, classOf[BattleActivity])
    intent.putExtra("level", id)
    startActivity(intent)
  }

  override def onCreate(savedState: Bundle) {
    android.util.Log.e("bruteb", "Brute Blitzkrieg main activity started")
    super.onCreate(savedState)
    val prefs = Preferences()
    viewSeq.set(0)
    setContentView(

      new SLinearLayout {
        new STableLayout {
          this += new STableRow {
            SButton("Level One", switchScreen(1)).<<.wrap.>>
            SButton("Level Two").<<.wrap.>>
            SButton("Level Three").<<.wrap.>>
          }
          this += new STableRow {
            SButton("Level Four").<<.wrap.>>
            SButton("Level Five").<<.wrap.>>
            SButton("Level Six").<<.wrap.>>
          }
          this += new STableRow {
            SButton("Level Seven").<<.wrap.>>
            SButton("Level Eight").<<.wrap.>>
            SButton("Level Nine").<<.wrap.>>
          }
          this += new STableRow {
            SButton("Level Ten").<<.wrap.>>
            SButton("Level Eleven").<<.wrap.>>
            SButton("Level Twelve").<<.wrap.>>
          }
        }.<<.wrap.>>.here//.stretchColumns("*")
        /*var grid = new SGridLayout {
          SButton("Level One").<<.wrap.>>
          SButton("Level Two").<<.wrap.>>
          SButton("Level Three").<<.wrap.>>
          SButton("Level Four").<<.wrap.>>
          SButton("Level Five").<<.wrap.>>
          SButton("Level Six").<<.wrap.>>
          SButton("Level Seven").<<.wrap.>>
          SButton("Level Eight").<<.wrap.>>
          SButton("Level Nine").<<.wrap.>>
          SButton("Level Ten").<<.wrap.>>
          SButton("Level Eleven").<<.wrap.>>
          SButton("Level Twelve").<<.wrap.>>
        }.<<.wrap.>>.columnCount(3).columnOrderPreserved(true).here*/
        new SRelativeLayout {
          SButton("Minions", switchScreen(classOf[Encyclopedia])).<<.wrap.alignParentTop.>>
          SButton("Traps").<<.wrap.centerVertical.>>
          SButton("Quit", finish()).<<.wrap.alignParentBottom.>>
        }.<<.fw.>>.gravity(Gravity.RIGHT).here
      }
    )
  }
}
