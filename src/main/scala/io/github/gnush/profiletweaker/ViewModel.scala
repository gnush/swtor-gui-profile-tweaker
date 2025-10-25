package io.github.gnush.profiletweaker

import scalafx.beans.property.{BooleanProperty, StringProperty}

object ViewModel:
  val doBackup: BooleanProperty = new BooleanProperty(this, "doBackup", false)
  val overwriteBackup: BooleanProperty = new BooleanProperty(this, "overwriteBackup", false)
  val backupDir: StringProperty = new StringProperty(this, "backupDir", "backup")
  val profileSettings: StringProperty = StringProperty("")