package io.github.gnush.profiletweaker

import data.ini.{Ini, Key, Section, Value, format, hasKey, hasSection, put}

// TODO:
//  - merge with ViewModel  (eliminates double data holding, but mixes "scopes")?
//  - change hasBeenChanged from tracking updates to comparing with initial value
class Config(private val ini: Ini = Ini()) {
  def this(config: String) = {
    this(Ini.from(config) getOrElse Ini())
  }

  private val ConfigSection = "config"
  private val backupKey = "doBackup"
  private val overwriteBackupKey = "overwriteBackup"
  private val backupDirKey = "backupDir"
  private val guiStateLocationKey = "guiPlayerStateLocation"
  private val ProfileSection = "gui_state_settings"

  private var _hasBeenChanged = false
  def hasBeenChanged: Boolean = _hasBeenChanged

  def backup: Boolean = getBoolean(ConfigSection, backupKey) getOrElse false

  def backup_=(value: Boolean): Unit = update(ConfigSection, backupKey, value.toString)

  def overwriteBackup: Boolean = getBoolean(ConfigSection, overwriteBackupKey) getOrElse false

  def overwriteBackup_=(value: Boolean): Unit = update(ConfigSection, overwriteBackupKey, value.toString)

  def backupDir: String = get(ConfigSection, backupDirKey) getOrElse "backups"

  def backupDir_=(dirName: String): Unit = update(ConfigSection, backupDirKey, dirName)

  def guiStateLocation: String = get(ConfigSection, guiStateLocationKey) getOrElse (
    if (System.getProperty("os.name").toLowerCase.contains("windows"))
      "/%appdata%/SWTOR/swtor/settings"
    else
      os.home.toString
  )

  def guiStateLocation_=(location: String): Unit = update(ConfigSection, guiStateLocationKey, location)

  def guiStateSettings: String = ini.format(ProfileSection) getOrElse ""

  def guiStateSettings_=(settings: String): Unit = {
    // current profile settings as ini
    val ini = Ini.from(s"[$ProfileSection]\n" + settings) getOrElse Ini()

    // remove old/unused profile settings
    if (this.ini.hasSection(ProfileSection))
      this.ini(ProfileSection).keySet foreach { key =>
        if (!ini.hasKey(ProfileSection, key))
          remove(ProfileSection, key)
      }

    // update profile settings
    ini foreach { (section, inner) =>
      inner foreach { (key, value) =>
        //println(s"updating $section with $key = $value")
        update(section, key, value)
      }
    }
  }

  def toIniFormat: String = ini.format

  private def get(section: Section, key: Key): Option[Value] =
    if (ini.hasKey(section, key))
      Some(ini(section)(key))
    else
      None

  private def getBoolean(section: Section, key: Key): Option[Boolean] =
    try
      Some(ini(section)(key).toBoolean)
    catch
      case _: Exception => None

  private def update(section: Section, key: Key, value: Value): Unit = {
    if (ini.hasKey(section, key) && ini(section)(key) == value)
      return

    _hasBeenChanged = ini.put(section, key, value)
  }

  private def remove(section: Section, key: Key): Unit = if (ini.hasKey(section, key)) {
    ini(section) -= key

    // If the last key was removed, remove the empty section
    if (ini(section).keySet.isEmpty)
      ini -= section

    _hasBeenChanged = true
  }

//  private def get[T](section: Section, key: Key): Option[T] =
//    try {
//      Some(ini(section)(key).asInstanceOf[T])
//      //Some(ini(section)(key))
//    } catch
//      case _: Exception => None
//
//  private implicit def valueAs[T](value: Value, default: T): T = default match {
//    case _: Boolean => value.toBoolean
//    case _: String => value
//    case _ => value.asInstanceOf[T]
//  }
//
//  private implicit def valueAs[T](value: Value): T = T match {
//    case Boolean => value.toBoolean
//    case String => value
//    case _ => value.asInstanceOf[T]
//  }
}