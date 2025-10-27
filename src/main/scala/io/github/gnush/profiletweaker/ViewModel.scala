package io.github.gnush.profiletweaker

import io.github.gnush.profiletweaker.data.CharacterGuiStateItem
import io.github.gnush.profiletweaker.data.ini.Ini
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.collections.ObservableBuffer

object ViewModel:
  val doBackup: BooleanProperty = BooleanProperty(false)
  val overwriteBackup: BooleanProperty = BooleanProperty(false)
  val backupDir: StringProperty = StringProperty("")

  val guiStateSettings: StringProperty = StringProperty("foo bar")
  val guiStateSettingsSearch: StringProperty = new StringProperty(this, "", "") {
    onChange { (_, _, content) =>
      findFirstIndicesInGuiStateSettings(content) match {
        case Some((start, end)) => MainApp.guiStateSettings.selectRange(start, end)
        case None => MainApp.guiStateSettings.deselect()
      }
    }
  }
  val guiStateSettingsSearchVisible: BooleanProperty = BooleanProperty(false)

  val playerGuiStateTargets: ObservableBuffer[CharacterGuiStateItem] = ObservableBuffer.empty
  val playerGuiStateLocation: StringProperty = StringProperty("")

  def guiStateSettingsAsMap: Map[String, String] = Ini.from(s"[Settings]\n${guiStateSettings.value}") match {
    case Some(ini) => ini("Settings").toMap
    case None => Map.empty
  }

  def findFirstIndicesInGuiStateSettings(s: String): Option[(Int, Int)] = {
    val start = guiStateSettings.value.toLowerCase.indexOf(s.toLowerCase)
    if (start >= 0)
      Some((start, start+s.length))
    else
      None
  }