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

class TrapEncyclopedia extends SActivity {
  override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    val txt = new STextView {
      text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed risus arcu, consequat ut sodales sit amet, semper nec dui. Quisque ullamcorper leo odio, sit amet cursus orci interdum sed. Pellentesque imperdiet scelerisque congue. Phasellus semper auctor auctor. Sed faucibus urna quam, vitae iaculis enim aliquam vel. Vivamus in mauris diam. Proin malesuada iaculis orci, nec rhoncus mi ullamcorper ac. Nullam vitae scelerisque nibh. Sed nunc lectus, porta dapibus neque pharetra, posuere sagittis ipsum. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed risus arcu, consequat ut sodales sit amet, semper nec dui. Quisque ullamcorper leo odio, sit amet cursus orci interdum sed. Pellentesque imperdiet scelerisque congue. Phasellus semper auctor auctor. Sed faucibus urna quam, vitae iaculis enim aliquam vel. Vivamus in mauris diam. Proin malesuada iaculis orci, nec rhoncus mi ullamcorper ac. Nullam vitae scelerisque nibh. Sed nunc lectus, porta dapibus neque pharetra, posuere sagittis ipsum."
      textSize = 16 dip
    }
    val img = new SImageView {
      imageResource=R.drawable.ahmed
    }.scaleType(ImageView.ScaleType.CENTER_INSIDE).maxHeight(100 dip).adjustViewBounds(true)
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
            SButton(R.string.Trapdoor, {
              txt.text = s"${TrapAttributeMap(TrapDoorID).description}"
              //img.imageResource = R.drawable.trapdoor
              }).<<.fw.>>
            SButton(R.string.ReusableTrapdoor, {
              txt.text = s"${TrapAttributeMap(ReuseTrapDoorID).description}"
              //img.imageResource = R.drawable.reusetrapdoor
              }).<<.fw.>>
            SButton(R.string.Tar, {
              txt.text = s"${TrapAttributeMap(TarID).description}"
              //img.imageResource = R.drawable.tartrap
              }).<<.fw.>>
            SButton(R.string.Poison, {
              txt.text = s"${TrapAttributeMap(PoisonID).description}"
              //img.imageResource = R.drawable.poisontrap
              }).<<.fw.>>
            SButton(R.string.Arrow, {
              txt.text = s"${TrapAttributeMap(ArrowID).description}"
              img.imageResource = R.drawable.arrowtrap
              }).<<.fw.>>
            SButton(R.string.Lightning, {
              txt.text = s"${TrapAttributeMap(LightningID).description}"
              img.imageResource = R.drawable.lightningtrap
              }).<<.fw.>>
            SButton(R.string.FlameVent, {
              txt.text = s"${TrapAttributeMap(FlameVentID).description}"
              //img.imageResource = R.drawable.flameventtrap
              }).<<.fw.>>
            SButton(R.string.HighBlade, {
              txt.text = s"${TrapAttributeMap(HighBladeID).description}"
              //img.imageResource = R.drawable.highbladetrap
              }).<<.fw.>>
          }.<<.wrap.>>.here
        }.<<(0,WRAP_CONTENT).Weight(1).>>.here
      }
    )
  }
}
