package com.github.fellowship_of_the_bus
package bruteb

import models._

// import android.app.Activity
import org.scaloid.common._
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.view.View
import android.content.Intent
import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class BaseActivity extends SActivity {
  override implicit val loggerTag = new LoggerTag("bruteb")
  var layout: android.view.View = null

	override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    getActionBar().hide()

    //setContentView(
    //  layout
    //)
	}

  override def setContentView (v : View) {
    super.setContentView(
      new SLinearLayout {
         style {
          case b: SButton => b.allCaps(false)
          case viw: TraitViewGroup[_] =>
            for (i <- 0 until viw.basis.childCount) this.applyStyle(viw.basis.getChildAt(i))
            viw
        }
        v.here
        v.<<.fill
      }
    )
  }

  def customMapFiles = getFilesDir.listFiles

  def loadCustom(file: java.io.File): MapInfo = {
    import models.MapInfo._
    import models.MapID.{extractor}
    val customID = Custom(file.getName)
    implicit object Factory extends lib.game.IDFactory[MapID] {
      val ids = Vector(customID)
      addParser {
        case name if (name.startsWith(Custom.prefix)) => Custom(name.substring(Custom.prefix.length))
      }
    }
    val idmap = new lib.game.IDMap[MapID, MapInfo](new java.io.FileInputStream(file))
    idmap(customID)
  }
}
