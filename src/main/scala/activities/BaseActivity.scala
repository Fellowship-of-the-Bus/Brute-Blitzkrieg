package com.github.fellowship_of_the_bus
package bruteb

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

class BaseActivity extends SActivity {
  override implicit val loggerTag = new LoggerTag("bruteb")

	override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    getActionBar().hide()
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
