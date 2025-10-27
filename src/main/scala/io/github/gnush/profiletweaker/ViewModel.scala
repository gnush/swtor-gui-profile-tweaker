package io.github.gnush.profiletweaker

import io.github.gnush.profiletweaker.data.CharacterGuiStateItem
import io.github.gnush.profiletweaker.data.ini.Ini
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.collections.ObservableBuffer

object ViewModel:
  val doBackup: BooleanProperty = BooleanProperty(false)
  val overwriteBackup: BooleanProperty = BooleanProperty(false)
  val backupDir: StringProperty = StringProperty("")

  val guiStateSettings: StringProperty = StringProperty("")

  val playerGuiStateTargets: ObservableBuffer[CharacterGuiStateItem] = ObservableBuffer.empty
  val playerGuiStateLocation: StringProperty = StringProperty("")

  def guiStateSettingsAsMap: Map[String, String] = Ini.from(s"[Settings]\n${guiStateSettings.value}") match {
    case Some(ini) => ini("Settings").toMap
    case None => Map.empty
  }