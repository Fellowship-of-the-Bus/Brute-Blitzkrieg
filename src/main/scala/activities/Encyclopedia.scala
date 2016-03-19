package com.github.fellowship_of_the_bus.bruteb

import models._

// import android.app.Activity
import org.scaloid.common._
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.widget.GridView
import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class Encyclopedia extends SActivity {
  val viewSeq = new AtomicInteger(0)

  override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    val prefs = Preferences()
    viewSeq.set(0)
    val txt = new STextView {
      text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed risus arcu, consequat ut sodales sit amet, semper nec dui. Quisque ullamcorper leo odio, sit amet cursus orci interdum sed. Pellentesque imperdiet scelerisque congue. Phasellus semper auctor auctor. Sed faucibus urna quam, vitae iaculis enim aliquam vel. Vivamus in mauris diam. Proin malesuada iaculis orci, nec rhoncus mi ullamcorper ac. Nullam vitae scelerisque nibh. Sed nunc lectus, porta dapibus neque pharetra, posuere sagittis ipsum."
      textSize = 16 dip
    }
    setContentView(

      new SLinearLayout {
        txt.<<(0,WRAP_CONTENT).Weight(4).>>.here

        new SScrollView {
          new SVerticalLayout {
            SButton("one", {txt.text = "a"}).<<.wrap.>>
            SButton("one", {txt.text = "b"}).<<.wrap.>>
            SButton("one", {txt.text = "c"}).<<.wrap.>>
            SButton("one", {txt.text = "d"}).<<.wrap.>>
            SButton("one", {txt.text = "e"}).<<.wrap.>>
            SButton("one", {txt.text = "f"}).<<.wrap.>>
            SButton("one", {txt.text = "g"}).<<.wrap.>>
          }.<<.wrap.>>.here
        }.<<(0,WRAP_CONTENT).Weight(1).>>.here
      }
    )
  }
}
