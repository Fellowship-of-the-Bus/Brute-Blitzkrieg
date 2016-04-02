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

class BaseActivity extends SActivity {
  override implicit val loggerTag = new LoggerTag("bruteb")

	override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    getActionBar().hide()
	}
}
