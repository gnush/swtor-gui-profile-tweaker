package io.github.gnush.profiletweaker

import io.github.gnush.profiletweaker.data.CharacterGuiStateItem
import javafx.collections.{FXCollections, ObservableList}
import scalafx.beans.property.{BooleanProperty, ObjectProperty, StringProperty}
import scalafx.collections.ObservableBuffer

object ViewModel:
  val doBackup: BooleanProperty = BooleanProperty(false)
  val overwriteBackup: BooleanProperty = BooleanProperty(false)
  val backupDir: StringProperty = StringProperty("")
  val inisPath: StringProperty = StringProperty("")

  val profileSettings: StringProperty = StringProperty("")

  val iniList: ObservableBuffer[CharacterGuiStateItem] = ObservableBuffer.empty