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
      text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed risus arcu, consequat ut sodales sit amet, semper nec dui. Quisque ullamcorper leo odio, sit amet cursus orci interdum sed. Pellentesque imperdiet scelerisque congue. Phasellus semper auctor auctor. Sed faucibus urna quam, vitae iaculis enim aliquam vel. Vivamus in mauris diam. Proin malesuada iaculis orci, nec rhoncus mi ullamcorper ac. Nullam vitae scelerisque nibh. Sed nunc lectus, porta dapibus neque pharetra, posuere sagittis ipsum. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed risus arcu, consequat ut sodales sit amet, semper nec dui. Quisque ullamcorper leo odio, sit amet cursus orci interdum sed. Pellentesque imperdiet scelerisque congue. Phasellus semper auctor auctor. Sed faucibus urna quam, vitae iaculis enim aliquam vel. Vivamus in mauris diam. Proin malesuada iaculis orci, nec rhoncus mi ullamcorper ac. Nullam vitae scelerisque nibh. Sed nunc lectus, porta dapibus neque pharetra, posuere sagittis ipsum."
      textSize = 16 dip
    }
    val nametxt = new STextView {
      text = "Ahmed"
      textSize = 16 dip
    }

    val img = new SImageView {
      imageResource=R.drawable.ahmed
    }.scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(100 dip).adjustViewBounds(true)
    val doBrute = getIntent().getBooleanExtra("brute", true)
    val ids = if (doBrute) bruteIDs else trapIDs
    setContentView(

      new SLinearLayout {
          new SVerticalLayout {
            new SLinearLayout {
              img.<<.wrap.>>.here
              nametxt.<<.wrap.>>.here
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
              SButton(name, {
                txt.text = s"${description}"
                img.imageResource = image
                nametxt.text = name
              })
            }
          }.<<.wrap.>>.here
        }.<<(0,WRAP_CONTENT).Weight(1).>>.here
      }
    )
  }
}
