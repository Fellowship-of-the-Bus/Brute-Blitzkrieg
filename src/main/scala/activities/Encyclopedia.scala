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
  override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    val txt = new STextView {
      text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed risus arcu, consequat ut sodales sit amet, semper nec dui. Quisque ullamcorper leo odio, sit amet cursus orci interdum sed. Pellentesque imperdiet scelerisque congue. Phasellus semper auctor auctor. Sed faucibus urna quam, vitae iaculis enim aliquam vel. Vivamus in mauris diam. Proin malesuada iaculis orci, nec rhoncus mi ullamcorper ac. Nullam vitae scelerisque nibh. Sed nunc lectus, porta dapibus neque pharetra, posuere sagittis ipsum. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed risus arcu, consequat ut sodales sit amet, semper nec dui. Quisque ullamcorper leo odio, sit amet cursus orci interdum sed. Pellentesque imperdiet scelerisque congue. Phasellus semper auctor auctor. Sed faucibus urna quam, vitae iaculis enim aliquam vel. Vivamus in mauris diam. Proin malesuada iaculis orci, nec rhoncus mi ullamcorper ac. Nullam vitae scelerisque nibh. Sed nunc lectus, porta dapibus neque pharetra, posuere sagittis ipsum."
      textSize = 16 dip
    }
    val img = new SImageView {
      background=R.drawable.ahmed
    }
    setContentView(

      new SLinearLayout {
        new SScrollView {
          new SVerticalLayout {
            img.<<.wrap.>>.here
            txt.<<.wrap.>>.here
          }.<<.wrap.>>.here
        }.<<(0,WRAP_CONTENT).Weight(3).>>.here

        new SScrollView {
          new SVerticalLayout {
            SButton(R.string.Ogre, {txt.text = "a"}).<<.fw.>>
            SButton(R.string.Goblin, {txt.text = "b"}).<<.fw.>>
            SButton(R.string.VampireBat, {txt.text = "c"}).<<.fw.>>
            SButton(R.string.GoblinShaman, {txt.text = "d"}).<<.fw.>>
            SButton(R.string.Spider, {txt.text = "e"}).<<.fw.>>
            SButton(R.string.FlameImp, {txt.text = "f"}).<<.fw.>>
            SButton(R.string.CageGoblin, {txt.text = "g"}).<<.fw.>>
            SButton(R.string.Troll, {txt.text = "h"}).<<.fw.>>
          }.<<.wrap.>>.here
        }.<<(0,WRAP_CONTENT).Weight(1).>>.here
      }
    )
  }
}
