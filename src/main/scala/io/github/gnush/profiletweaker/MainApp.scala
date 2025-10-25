package io.github.gnush.profiletweaker

import io.github.gnush.profiletweaker.MainApp.stage
import scalafx.application.JFXApp3
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Pos.{Center, CenterLeft, TopCenter}
import scalafx.geometry.{HPos, Insets}
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.scene.layout.Priority.Always
import scalafx.scene.text.Text
import scalafx.scene.{Node, Scene}
import scalafx.stage.{DirectoryChooser, Stage}

import java.io.File

object MainApp extends JFXApp3:
  private var config = Config("")
  private val configFile = "config.ini"

  override def start(): Unit = {
    //os.list(os.pwd).filter(_.segments.toList.last.startsWith(".")).foreach(println)
    //os.list(os.pwd / "src").foreach(println)
    //os.copy(FROM, TO, mergeFolders = true, replaceExisting = true)

//    val root: Region = new BorderPane {
//      top = new Text { text = "Foo" }
//      center = centerContent
//    }
    val root: Region = centerContent
    val theScene: Scene = Scene(root)

    stage = new JFXApp3.PrimaryStage {
      title = "SWToR: GUI Profile Tweaker"
      scene = theScene
    }

//    println(stage.getIcons.size())
//    stage.getIcons.forEach(println)
//
//    println(s"pwd is writable: ${os.isWritable(os.pwd)}")
//    println(s"backup dir exists: ${os.exists(os.pwd / ViewModel.backupDir())}")
//
//    println(s"user dir = ${System.getProperty("user.dir")}")

    config = if (os.exists(os.pwd / configFile) && os.isReadable(os.pwd / configFile))
      Config(os.read(os.pwd / configFile))
    else
      Config()
    populateViewModel(config)
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
    children = Seq(settings, inisPane, editPane)
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
        grid,
        Text(s"working dir = ${os.pwd}"),
        Text(s"home dir = ${os.home}"),
        Text(s"root dir = ${os.root}")
      )
      alignment = TopCenter
    }
  }



  private def inisPane = new VBox {

    val f = File("/foo")

    val dir = new DirectoryChooser {
      title = "GUI Profile Location"
      //if (f.exists()) initialDirectory = f
      initialDirectory = if (f.exists()) f else File(".")
    }

    val list = new ListView[String]{
      items = ObservableBuffer.from(List("Foo", "Bar", "Baz"))
      prefHeight = 486
    }
    list.selectionModel.value.setSelectionMode(SelectionMode.Multiple)

    children = Seq(
      Text("GUI Profiles"),
      list,
      new HBox {
        children = Seq(
          new Button {
            text = "…"
            onMouseClicked = _ => {
              println(dir.showDialog(stage))
            }
          },
          Text("path/to/profiles")
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
        style = "-fx-border-color: black;-fx-border-radius: 4px; -fx-border-width: 1px"
      }
    )
  }

  private def editPane = new VBox {
    alignment = TopCenter
    children = Seq(
      Text("[settings]"),
      new TextArea {
        text <==> ViewModel.profileSettings
        prefHeight = 486
      },
      new HBox {
        children = Seq (
          new Button {
            text = "save for later"
            onMouseClicked = _ => println("foo")
          },
          new HBox { hgrow = Always },
          new Button {
            text = "apply to selected"
            onMouseClicked = _ => println("foo")
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
    ViewModel.profileSettings.value = config.profileSettings
  }

  /**
   * Save the config options as well as the gui profile settings
   */
  private def saveConfig(): Unit = {
    config.backup = ViewModel.doBackup.value
    config.overwriteBackup = ViewModel.overwriteBackup.value
    config.backupDir = ViewModel.backupDir.value
    config.profileSettings = ViewModel.profileSettings.value

    if (config.hasBeenChanged && os.isWritable(os.pwd))
      os.write.over(os.pwd / configFile, config.toIniFormat)
  }