package io.github.gnush.profiletweaker

import io.github.gnush.profiletweaker.data.CharacterGuiStateItem
import io.github.gnush.profiletweaker.data.ini.Ini
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

object ViewModel:
  val doBackup: BooleanProperty = BooleanProperty(false)
  val overwriteBackup: BooleanProperty = BooleanProperty(false)
  val backupDir: StringProperty = StringProperty("")

  val guiStateSettingsSearchPosition: ObjectProperty[Option[Int]] = new ObjectProperty(this, "", Option.empty[Int]) {
    onChange { (_, old, current) => (old, current) match {
      case (Some(0), Some(index)) if index < 0 => value = Some(guiStateSettingsSearchMatches.value.size-1)
      case (_, Some(index)) if index < guiStateSettingsSearchMatches.value.size => selectInGuiStateSettings()
      case (_, Some(index)) => value = Some(0)
      case (_, None) => MainApp.guiStateSettings.deselect()
    }}
  }

  private val guiStateSettingsSearchMatches: ObjectProperty[List[(Int, Int)]] = new ObjectProperty(this, "", List.empty[(Int, Int)]) {
    onChange { (_, _, value) =>
      value match {
        case Nil => guiStateSettingsSearchPosition.value = None
        case _ =>
          guiStateSettingsSearchPosition.value = Some(0)
          selectInGuiStateSettings()
      }
    }
  }

  val guiStateSettings: StringProperty = StringProperty("")
  val guiStateSettingsSearch: StringProperty = new StringProperty(this, "", "") {
    onChange { (_, _, content) =>
      guiStateSettingsSearchMatches.value = findIndicesInGuiStateSettings(content)
    }
  }
  val guiStateSettingsSearchVisible: BooleanProperty = BooleanProperty(false)

  val playerGuiStateTargets: ObservableBuffer[CharacterGuiStateItem] = ObservableBuffer.empty
  val playerGuiStateLocation: StringProperty = StringProperty("")

  def guiStateSettingsAsMap: Map[String, String] = Ini.from(s"[Settings]\n${guiStateSettings.value}") match {
    case Some(ini) => ini("Settings").toMap
    case None => Map.empty
  }

  private def findIndicesInGuiStateSettings(find: String): List[(Int, Int)] =
    if (find.nonEmpty)
      guiStateSettings.value.toLowerCase
        .sliding(find.length).zipWithIndex
        .filter((s, _) => s == find.toLowerCase)
        .map((_, index) => (index, index+find.length))
        .toList
    else
      Nil

  def selectInGuiStateSettings(): Unit = guiStateSettingsSearchPosition.value match {
    case Some(index) if index >= 0 && index < guiStateSettingsSearchMatches.value.size =>
      val (start, end) = guiStateSettingsSearchMatches.value(index)
      MainApp.guiStateSettings.selectRange(start, end)
    case _ =>
      MainApp.guiStateSettings.deselect()
  }