package io.github.gnush.profiletweaker

import io.github.gnush.profiletweaker.data.CharacterGuiStateItem
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.collections.ObservableBuffer

object ViewModel:
  val doBackup: BooleanProperty = BooleanProperty(false)
  val overwriteBackup: BooleanProperty = BooleanProperty(false)
  val backupDir: StringProperty = StringProperty("")

  val guiStateSettings: StringProperty = StringProperty("")

  val playerGuiStateTargets: ObservableBuffer[CharacterGuiStateItem] = ObservableBuffer.empty
  val playerGuiStateLocation: StringProperty = StringProperty("")