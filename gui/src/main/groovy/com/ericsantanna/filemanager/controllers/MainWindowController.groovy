package com.ericsantanna.filemanager.controllers

import com.ericsantanna.filemanager.models.PathItem
import com.ericsantanna.filemanager.services.ClipboardService
import com.ericsantanna.filemanager.utils.FileManagerDataFormat
import com.ericsantanna.filemanager.utils.FxmlUtils
import com.sun.javafx.PlatformUtil
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.stage.Modality
import javafx.stage.Stage

import java.awt.*
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.List
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.stream.Collectors

class MainWindowController implements Initializable {
    @FXML private TextField addressBar
    @FXML private ScrollPane fileView
    @FXML private TableView fileList
    @FXML private ProgressBar prgsBarMain

    final ClipboardService clipboardService = new ClipboardService()

    private final ObservableList<PathItem> data = FXCollections.observableArrayList()
    private ExecutorService executorService = Executors.newFixedThreadPool(4)

    @FXML
    void initialize(URL location, ResourceBundle resources) {
//        String homeDir = System.getProperty("user.home");
        String homeDir = "/tmp/filemanager"

        addressBar.setText(homeDir)

        Path currentPath = Paths.get(addressBar.getText())
        data.clear()
        FileListingTask fileListingTask = new FileListingTask(currentPath, data)
        prgsBarMain.progressProperty().bind(fileListingTask.progressProperty())

        TableColumn iconColumn = new TableColumn("Icon")
        iconColumn.setMaxWidth(500)
        TableColumn nameColumn = new TableColumn("Name")
        TableColumn sizeColumn = new TableColumn("Size")
        sizeColumn.setMaxWidth(900)
        TableColumn modifiedColumn = new TableColumn("Modified")
        modifiedColumn.setMaxWidth(1000)

        fileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE)

        fileList.setOnKeyPressed({ keyEvent ->
            def selectedItems = fileList.getSelectionModel().getSelectedItems()
            def paths = selectedItems.collect { i -> (i as PathItem).path } as List<Path>

            def ctrlC = new KeyCodeCombination(KeyCode.C, KeyCodeCombination.CONTROL_DOWN)
            if (ctrlC.match(keyEvent)) {
                clipboardService.copy(paths)
                keyEvent.consume()
            }

            def ctrlX = new KeyCodeCombination(KeyCode.X, KeyCodeCombination.CONTROL_DOWN)
            if (ctrlX.match(keyEvent)) {
                clipboardService.cut(paths)
                keyEvent.consume()
            }

            def ctrlV = new KeyCodeCombination(KeyCode.V, KeyCodeCombination.CONTROL_DOWN)
            if (ctrlV.match(keyEvent)) {
                clipboardService.paste(currentPath)
                keyEvent.consume()
            }
        })

        def contextMenuTable = new ContextMenu()
        contextMenuTable.minWidth = 300d
        contextMenuTable.prefWidth = 500
        contextMenuTable.width = 300d
        contextMenuTable.style = '-fx-pref-width: 200'
        updateMenu(contextMenuTable, Paths.get(addressBar.text), null)
        fileList.contextMenu = contextMenuTable

        fileList.setRowFactory({ tableView ->
            def row = new TableRow()
            row.setOnMouseClicked({ event ->
                def item = row.item as PathItem
                if(!item) {
                    return
                }
                def itemPath = item ? Paths.get(addressBar.text).resolve(item.name) : null
                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    if(Files.isDirectory(itemPath)) {
                        addressBar.setText(itemPath.toString())
                        addressBar.fireEvent(new ActionEvent())
                    } else {
                        openFile(itemPath)
                    }
                }
            })

            def contextMenuRow = new ContextMenu()
            contextMenuRow.minWidth = 300d
            contextMenuRow.prefWidth = 500
            contextMenuRow.width = 300d
            contextMenuRow.style = '-fx-pref-width: 200'

            row.contextMenu = contextMenuRow

//            row.emptyProperty().addListener({ obs, wasEmpty, isNowEmpty ->
//                println "empty: $row.index / $isNowEmpty"
//                updateMenu(contextMenuRow, basePath, row.item as PathItem)
//            } as ChangeListener)

            def basePath = Paths.get(addressBar.text)
            updateMenu(contextMenuRow, basePath, row.item as PathItem)
            row.itemProperty().addListener({ obs, PathItem oldItem, PathItem newItem ->
                updateMenu(contextMenuRow, Paths.get(addressBar.text), row.item as PathItem)
            } as ChangeListener)

            return row
        })

        iconColumn.cellValueFactory = new PropertyValueFactory<PathItem, ImageView>("icon")
        nameColumn.cellValueFactory = new PropertyValueFactory<PathItem, String>("name")
        sizeColumn.cellValueFactory = new PropertyValueFactory<PathItem, String>("size")
        modifiedColumn.cellValueFactory = new PropertyValueFactory<PathItem, String>("modified")
        fileList.columns.addAll(iconColumn, nameColumn, sizeColumn, modifiedColumn)

        fileList.items = data

        try {
            addressBar.setOnAction({ event ->
                try {
                    reload(Paths.get(addressBar.text))
                } catch (Exception e) {
                    e.printStackTrace()
                }
            })
            reload(Paths.get(addressBar.text))
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    private void updateMenu(ContextMenu contextMenu, Path basePath, PathItem item) {
        def itemPath = item ? Paths.get(addressBar.text).resolve(item.name) : null
        contextMenu.items.clear()
        if(item) {
            if(!Files.isDirectory(item.path)) {
                def menuItemOpen = new MenuItem("Open")
                menuItemOpen.setOnAction({ event ->
                    openFile(basePath.resolve(item.name))
                })
                contextMenu.items << menuItemOpen
            }

            def menuItemDelete = new MenuItem("Delete")
            menuItemDelete.setOnAction({ event ->
                Files.deleteIfExists(itemPath)
                reload(basePath)
            })
            contextMenu.items << menuItemDelete
        } else {
            def menuItemNew = new Menu("New")
            def menuItemNewFile = new MenuItem("File")
            menuItemNewFile.setOnAction({ event ->
                onNewFile()
            })
            def menuItemNewFolder = new MenuItem("Folder")
            menuItemNewFolder.setOnAction({ event ->
                onNewFolder()
            })
            menuItemNew.items.addAll(menuItemNewFile, menuItemNewFolder)
            contextMenu.items << menuItemNew
        }
    }

    private void openFile(Path path) {
        if(Desktop.isDesktopSupported()) {
            new Thread({
                try {
                    Desktop.getDesktop().open(path.toFile())
                } catch (IOException e) {
                    e.printStackTrace()
                }
            }).start()
        } else {
            System.err.println("Desktop not supported")
        }
    }

    @FXML
    void onUp() {
        Path path = Paths.get(addressBar.getText()).getParent()
        addressBar.setText(path.toString())
        addressBar.fireEvent(new ActionEvent())
    }

    @FXML
    void onNewFolder() throws Exception {
        def fxml = FxmlUtils.loadFxml("/fxml/mainWindow/new-folder.fxml")
        def scene = new Scene(fxml.node, 400, 200)
        def stage = new Stage()
        stage.title = "New folder"
        stage.resizable = false
        stage.scene = scene
        stage.initModality(Modality.APPLICATION_MODAL)

        def controller = fxml.controller as NewFolderController
        controller.currentPath = Paths.get(addressBar.getText())

        stage.showAndWait()

        reload(Paths.get(addressBar.text))
    }

    @FXML
    void onNewFile() throws Exception {
        def fxml = FxmlUtils.loadFxml("/fxml/mainWindow/new-file.fxml")
        def scene = new Scene(fxml.node, 400, 200)
        def stage = new Stage()
        stage.title = "New file"
        stage.resizable = false
        stage.scene = scene
        stage.initModality(Modality.APPLICATION_MODAL)

        def controller = fxml.controller as NewFileController
        controller.currentPath = Paths.get(addressBar.getText())

        stage.showAndWait()

        reload(Paths.get(addressBar.text))
    }

    void reload(Path path) throws Exception {
        def fileListingTask = new FileListingTask(path, data)
        prgsBarMain.progressProperty().bind(fileListingTask.progressProperty())
        executorService.submit(fileListingTask)
    }
}
