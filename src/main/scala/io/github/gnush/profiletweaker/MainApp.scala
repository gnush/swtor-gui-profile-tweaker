package io.github.gnush.profiletweaker

import io.github.gnush.profiletweaker.MainApp.stage
import io.github.gnush.profiletweaker.data.ini.*
import io.github.gnush.profiletweaker.data.{CharacterGuiStateItem, Server}
import os.Path
import scalafx.application.JFXApp3
import scalafx.collections.ObservableBuffer
import scalafx.collections.CollectionIncludes.observableList2ObservableBuffer
import scalafx.geometry.Pos.{Center, CenterLeft, TopCenter}
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
  private val configFile = "config.ini"

  private var playGuiStateTargets: ListView[CharacterGuiStateItem] = null

  override def start(): Unit = {
    playGuiStateTargets = new ListView[CharacterGuiStateItem] {
      items = ViewModel.playerGuiStateTargets
      prefHeight = 486
      prefWidth = 320
    }
    playGuiStateTargets.selectionModel.value.setSelectionMode(SelectionMode.Multiple)

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
    config = if (os.exists(os.pwd / configFile) && os.isReadable(os.pwd / configFile))
      Config(os.read(os.pwd / configFile))
    else
      Config()
    populateViewModel(config)

    // Populate available character gui states
    loadInis(config.guiStateLocation)
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
    children = Seq(settings, availablePlayerGuiStatePane, editPane)
    spacing = 8
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

  private def availablePlayerGuiStatePane= new VBox {
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
                  loadInis(ViewModel.playerGuiStateLocation.value)
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
    children = Seq(
      Text(s"[$section]"),
      new TextArea {
        text <==> ViewModel.guiStateSettings
        prefHeight = 486
      },
      new HBox {
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
          new Button {
            text = "Apply"
            onAction = _ => {
              // backup selected items
              backup(
                location = ViewModel.playerGuiStateLocation.value,
                backupDirName = ViewModel.backupDir.value,
                paths = observableList2ObservableBuffer(playGuiStateTargets.selectionModel.value.getSelectedItems).toList.map(_.path)
              )

              // TODO: apply to selected items
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
    ViewModel.playerGuiStateLocation.value = config.guiStateLocation
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

    if (config.hasBeenChanged && os.isWritable(os.pwd))
      os.write.over(os.pwd / configFile, config.toIniFormat)
  }

  private def loadInis(location: String): Unit = try {
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
    case e: IllegalArgumentException => new Alert(Information) {
      initOwner(stage)
      title = "Could not load GUI State Directory"
      headerText = "Could not load GUI State Directory"
      contentText = e.getMessage
    }.show()
  }

  private def backup(location: String, backupDirName: String, paths: List[Path]): Unit = try {
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
  } catch {
    case e: IllegalArgumentException => information(
      titleSuffix = "Backup",
      header = "Could not backup files",
      content = e.getMessage
    )
    case e: NoSuchFileException => information(
      titleSuffix = "Backup",
      header = "Path does not exist",
      content = e.getMessage
    )
    case e: FileAlreadyExistsException => information(
      titleSuffix = "Backup",
      header = "Target already exists",
      content = e.getMessage
    )
  }

  private def information(titleSuffix: String, header: String, content: String): Unit = new Alert(Information) {
    initOwner(stage)
    title = s"SW:ToR GUI State Tweaker - $titleSuffix"
    headerText = header
    contentText = content
  }.showAndWait()