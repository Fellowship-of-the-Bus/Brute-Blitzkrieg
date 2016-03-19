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
  override def onCreate(savedState: Bundle) {
    android.util.Log.e("bruteb", "Brute Blitzkrieg main acitivity started")
    super.onCreate(savedState)
    setContentView(

      new SLinearLayout {
        new STableLayout {
          this += new STableRow {
            SButton("Level One").<<.wrap.>>
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
          SButton(R.string.MinionButton, {
            val intent = new Intent(MainActivity.this, classOf[Encyclopedia])
            startActivity(intent)
            }).<<.wrap.alignParentTop.>>
          SButton(R.string.TrapButton, {
            val intent = new Intent(MainActivity.this, classOf[TrapEncyclopedia])
            startActivity(intent)
            }).<<.wrap.centerVertical.>>
          SButton(R.string.QuitButton, {
            finish()
            }).<<.wrap.alignParentBottom.>>
        }.<<.fw.>>.gravity(Gravity.RIGHT).here
      }
    )
  }
}
