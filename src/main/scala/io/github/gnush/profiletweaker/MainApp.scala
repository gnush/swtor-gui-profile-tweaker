package io.github.gnush.profiletweaker

import io.github.gnush.profiletweaker.data.ini.*
import io.github.gnush.profiletweaker.data.{CharacterGuiStateItem, Server}
import javafx.scene.input.KeyCode
import os.{FilePath, Path, RelPath, SubPath}
import scalafx.application.JFXApp3
import scalafx.collections.ObservableBuffer
import scalafx.collections.CollectionIncludes.observableList2ObservableBuffer
import scalafx.geometry.Pos.{Center, CenterLeft, CenterRight, TopCenter}
import scalafx.geometry.{HPos, Insets}
import scalafx.scene.control.*
import scalafx.scene.control.Alert.AlertType.Information
import scalafx.scene.layout.*
import scalafx.scene.layout.Priority.Always
import scalafx.scene.text.Text
import scalafx.scene.{Node, Scene}
import scalafx.stage.DirectoryChooser

import java.io.File
import java.nio.file.{FileAlreadyExistsException, NoSuchFileException}
import java.time.LocalDateTime

object MainApp extends JFXApp3:
  private var config = Config()

  private val configFileName = "config.ini"
  private val configFile: Path = Option(System.getProperty("prog.home")) match {
    case Some(path) => FilePath(path) match {
      case x: Path => x/configFileName
      case x: (RelPath|SubPath) => Path(x, os.pwd)/configFileName
    }
    case None => os.pwd/configFileName
  }

  // TODO:  - get rid of null
  //        - make guiStateSettings private (and refactor the dependency in ViewModel)
  private var playGuiStateTargets: ListView[CharacterGuiStateItem] = null
  var guiStateSettings: TextArea = null
  private var guiStateSettingsSearch: TextField = null

  override def start(): Unit = {
    playGuiStateTargets = new ListView[CharacterGuiStateItem] {
      items = ViewModel.playerGuiStateTargets
      prefWidth = 280
      vgrow = Always
    }
    playGuiStateTargets.selectionModel.value.setSelectionMode(SelectionMode.Multiple)

    guiStateSettings = new TextArea {
      text <==> ViewModel.guiStateSettings
      prefHeight = 600
      prefWidth = 800
      vgrow = Always

      onKeyPressed = ev => ev.getCode match {
        case KeyCode.F if ev.isShortcutDown =>
          ViewModel.guiStateSettingsSearchVisible.value = true
          guiStateSettingsSearch.requestFocus()

        case KeyCode.ESCAPE => ViewModel.guiStateSettingsSearchVisible.value = false
        case _ =>
      }
    }

    guiStateSettingsSearch = new TextField {
      text <==> ViewModel.guiStateSettingsSearch
      visible <==> ViewModel.guiStateSettingsSearchVisible

      onKeyPressed = ev => ev.getCode match {
        case KeyCode.ESCAPE =>
          ViewModel.guiStateSettingsSearchVisible.value = false
          guiStateSettings.requestFocus()
        case KeyCode.ENTER =>
          ViewModel.selectInGuiStateSettings()
        case _ =>
      }
    }

//    val root: Region = new BorderPane {
//      top = new Text { text = "Foo" }
//      center = centerContent
//    }
    val root: Region = centerContent
    val theScene: Scene = Scene(root)

    stage = new JFXApp3.PrimaryStage {
      title = "SWToR: Player GUI State Tweaker"
      scene = theScene
    }

    // Load config
    config = if (os.exists(configFile) && os.isReadable(configFile))
      Config(os.read(configFile))
    else
      Config()
    populateViewModel(config)

    // Populate available character gui states
    loadGuiStateInis(config.guiStateLocation.toString)
  }

  override def stopApp(): Unit = {
    saveConfig()
  }

  private def centerContent: HBox = new HBox {
    padding = Insets(
      top = 2,
      bottom = 10,
      left = 4,
      right = 2
    )
    spacing = 8
    children = Seq(availablePlayerGuiStatePane, editPane, settings)
  }

  private def settings = {
    val grid = new GridPane {
      columnConstraints = Seq(
        new ColumnConstraints {
          halignment = HPos.Left
        },
        new ColumnConstraints {
          halignment = HPos.Center
        }
      )
      hgap = 12
      vgap = 8
      padding = Insets(4)
    }

    grid.addRow(0, Text("Backup profiles"), new CheckBox{ selected <==> ViewModel.doBackup })
    grid.addRow(1, Text("Overwrite previous backup"), new CheckBox { selected <==> ViewModel.overwriteBackup })
    grid.addRow(2, Text("Backup directory"), new TextField { text <==> ViewModel.backupDir })

    new VBox{
      children = Seq(
        Text("Config"),
        grid
      )
      alignment = TopCenter
    }
  }

  private def availablePlayerGuiStatePane = new VBox {
    alignment = TopCenter
    children = Seq(
      Text("Player GUI State"),
      playGuiStateTargets,
      new HBox {
        children = Seq(
          new Button {
            text = "…"
            onAction = _ => {
              val dir = new DirectoryChooser {
                title = "Pick Player GUI State Location"

                File(ViewModel.playerGuiStateLocation.value) match {
                  case initial if initial.exists() => initialDirectory = initial
                  case _ =>
                }
              }

              Option(dir.showDialog(stage)) match {
                case Some(dir) if dir.exists() =>
                  ViewModel.playerGuiStateLocation.value = dir.getAbsolutePath
                  loadGuiStateInis(ViewModel.playerGuiStateLocation.value)
                case _ =>
              }
            }
          },
          new TextField {
            text <==> ViewModel.playerGuiStateLocation
            editable = false
            hgrow = Always
          }
        )
        alignment = CenterLeft
        spacing = 4
        padding = Insets(2)
        margin = Insets(
          top = 4,
          bottom = 0,
          left = 0,
          right = 0
        )
        //style = "-fx-border-color: black;-fx-border-radius: 4px; -fx-border-width: 1px"
      }
    )
  }

  private def editPane = new VBox {
    private val section = "Settings"

    alignment = TopCenter
    hgrow = Always
    children = Seq(
      Text(s"[$section]"),
//      new TextArea {
//        text <==> ViewModel.guiStateSettings
//        prefHeight = 486
//
//        onKeyPressed = ev => ev.getCode match {
//          case KeyCode.F if ev.isShortcutDown => ViewModel.guiStateSettingsSearchVisible.value = true
//          case KeyCode.ESCAPE => ViewModel.guiStateSettingsSearchVisible.value = false
//          case _ =>
//        }
//      },
      guiStateSettings,
      new HBox {
        spacing = 6
        children = Seq (
          new Button {
            text = "Load"
            onAction = _ => {
              val selected = Option(playGuiStateTargets.selectionModel.value.getSelectedItem)
              if (selected.isDefined)
                ViewModel.guiStateSettings.value =
                  (Ini.from(os.read(selected.get.path)) getOrElse Ini())
                    .format(section) getOrElse ""
            }
          },
          new HBox { hgrow = Always },
//          new TextField {
//            text <==> ViewModel.guiStateSettingsSearch
//            visible <==> ViewModel.guiStateSettingsSearchVisible
//          },
          guiStateSettingsSearch,
          new Button("Ʌ") {
            visible <==> ViewModel.guiStateSettingsSearchVisible
            onAction = _ => {
              ViewModel.guiStateSettingsSearchPosition.value match {
                case Some(pos) => ViewModel.guiStateSettingsSearchPosition.value = Some(pos - 1)
                case None => None
              }
            }
          },
          new Button("V") {
            visible <==> ViewModel.guiStateSettingsSearchVisible
            onAction = _ => {
              ViewModel.guiStateSettingsSearchPosition.value = ViewModel.guiStateSettingsSearchPosition.value match {
                case Some(pos) => Some(pos + 1)
                case None => None
              }
            }
          },
          new HBox { hgrow = Always },
          new Button {
            text = "Apply"
            onAction = _ => {
              val selectedPaths = observableList2ObservableBuffer(playGuiStateTargets.selectionModel.value.getSelectedItems).toList.map(_.path)

              if (backup( // backup selected items
                location = ViewModel.playerGuiStateLocation.value,
                  backupDirName = ViewModel.backupDir.value,
                  paths = selectedPaths
              ))
                applyGuiStateSettings(selectedPaths)
            }
          }
        )
        alignment = Center
        padding = Insets(2)
        margin = Insets(
          top = 4,
          bottom = 0,
          left = 0,
          right = 0
        )
      }
    )
  }

  private def populateViewModel(config: Config): Unit = {
    ViewModel.doBackup.value = config.backup
    ViewModel.overwriteBackup.value = config.overwriteBackup
    ViewModel.backupDir.value = config.backupDir
    ViewModel.guiStateSettings.value = config.guiStateSettings
    ViewModel.playerGuiStateLocation.value = config.guiStateLocation.toString
  }

  /**
   * Save the config options as well as the gui profile settings
   */
  private def saveConfig(): Unit = {
    config.backup = ViewModel.doBackup.value
    config.overwriteBackup = ViewModel.overwriteBackup.value
    config.backupDir = ViewModel.backupDir.value
    config.guiStateSettings = ViewModel.guiStateSettings.value
    config.guiStateLocation = ViewModel.playerGuiStateLocation.value

    if (config.hasBeenChanged && os.isWritable(configFile))
      os.write.over(configFile, config.toIniFormat)
  }

  /**
   * Load gui states inis into the [ViewModel]
   * @param location The location of the directory to load the inis from
   */
  private def loadGuiStateInis(location: String): Unit = try {
    val path = os.Path(location)
    if (os.exists(path) && os.isReadable(path))
      os.list(path).filter(_.segments.toList.last.contains("PlayerGUIState.ini")) foreach { path =>
        val file = path.segments.toList.last.split('_')

        if (file.length == 3 && Server.fromId.isDefinedAt(file(0)))
          ViewModel.playerGuiStateTargets.add(CharacterGuiStateItem(
            path = path,
            server = Server.fromId(file(0)),
            characterName = file(1)
          ))
      }
  } catch {
    case e: IllegalArgumentException => information(
      header = "Could not load GUI State Directory",
      content = e.getMessage
    )
  }

  private def backup(location: String, backupDirName: String, paths: List[Path]): Boolean = try {
    if (ViewModel.doBackup.value) {
      val wd = Path(location)
      val backupBasedir = wd / backupDirName
      val latest = backupBasedir / "latest"

      // Save old backup
      if (!ViewModel.overwriteBackup.value && os.isDir(latest)) {
        val now = if (System.getProperty("os.name").toLowerCase.contains("windows"))
          LocalDateTime.now().toString.replace(':', '-')
        else
          LocalDateTime.now().toString
        os.move(
          from = latest,
          to = backupBasedir / now,
          createFolders = true
        )
      }

      // Backup files
      paths foreach { path =>
        os.copy.over(
          from = path,
          to = latest / path.segments.toList.last,
          createFolders = true
        )
      }
    }
    true
  } catch {
    case e: IllegalArgumentException => information(
      titleSuffix = "Backup",
      header = "Could not backup files",
      content = e.getMessage
    )
      false
    case e: NoSuchFileException => information(
      titleSuffix = "Backup",
      header = "Path does not exist",
      content = e.getMessage
    )
      false
    case e: FileAlreadyExistsException => information(
      titleSuffix = "Backup",
      header = "Target already exists",
      content = e.getMessage
    )
      false
    case e: Exception => information(
      titleSuffix = "Backup",
      header = "Unexpected",
      content = e.getMessage
    )
      false
  }

  private def applyGuiStateSettings(paths: List[Path]): Unit = {
    val desiredGuiState = ViewModel.guiStateSettingsAsMap
    paths foreach { path => try {
      if (os.exists(path) && os.isReadable(path) && os.isWritable(path)) {
        val ini = Ini.from(os.read(path)) getOrElse Ini()
        desiredGuiState foreach { (key, value) =>
          ini.put("Settings", key, value)
        }
        os.write.over(path, ini.format)
      }
    } catch {
      case e: Exception => information(
        titleSuffix = "Apply Changes",
        header = s"Could not apply changes to '${path.segments.toList.last}'",
        content = e.getMessage
      )
    }}
  }

  private def information(header: String, content: String, titleSuffix: String = ""): Unit = new Alert(Information) {
    initOwner(stage)
    title = if (titleSuffix.isEmpty) s"SW:ToR GUI State Tweaker"
            else s"SW:ToR GUI State Tweaker - $titleSuffix"
    headerText = header
    contentText = content
  }.showAndWait()