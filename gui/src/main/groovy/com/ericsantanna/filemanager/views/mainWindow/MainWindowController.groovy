package com.ericsantanna.filemanager.views.mainWindow

import com.ericsantanna.filemanager.controllers.FileListingTask
import com.ericsantanna.filemanager.views.newFile.NewFileController
import com.ericsantanna.filemanager.views.newFolder.NewFolderController
import com.ericsantanna.filemanager.models.PathItem
import com.ericsantanna.filemanager.services.ClipboardService
import com.ericsantanna.filemanager.utils.FxmlUtils
import javafx.beans.binding.Bindings
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.stage.Modality
import javafx.stage.Stage

import java.awt.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.List
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainWindowController implements Initializable {
    @FXML private TextField addressBar
    @FXML private ScrollPane fileView
    @FXML private TableView fileList
    @FXML private ProgressBar prgsBarMain

    final ClipboardService clipboardService = new ClipboardService()

    private final ObservableList<PathItem> data = FXCollections.observableArrayList()
    private ExecutorService executorService = Executors.newFixedThreadPool(4)
    private ContextMenu contextMenuTable
    private BooleanProperty pastable = new SimpleBooleanProperty(false)

    @FXML
    void initialize(URL location, ResourceBundle resources) {
//        String homeDir = System.getProperty("user.home");
        String homeDir = "/tmp/filemanager"

        addressBar.setText(homeDir)

        Path currentPath = Paths.get(addressBar.text)
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

        contextMenuTable = new ContextMenu()
        contextMenuTable.minWidth = 300d
        contextMenuTable.prefWidth = 500
        contextMenuTable.width = 300d
        contextMenuTable.style = '-fx-pref-width: 200'
//        updateMenu(contextMenuTable, null, Paths.get(addressBar.text), null)
        def menuItemNew = new Menu("New")
        def menuItemNewFile = new MenuItem("File")
        menuItemNewFile.setOnAction({ event ->
            onNewFile()
        })
        def menuItemNewFolder = new MenuItem("Folder")
        menuItemNewFolder.setOnAction({ event ->
            onNewFolder()
        })
//        menuItemNew.setOnShowing({ event ->
//            println "setOnShowing: $event"
//        })
//        menuItemNew.setOnMenuValidation({ event ->
//            (event.source as Menu).visible = false
//            println "setOnMenuValidation: $event"
//        })
        menuItemNew.items.addAll(menuItemNewFile, menuItemNewFolder)
        contextMenuTable.items << menuItemNew

        def menuItemPaste = getContextMenuPaste(currentPath)
        menuItemPaste.visibleProperty().bind(pastable)
        contextMenuTable.items << menuItemPaste

        contextMenuTable.setOnShowing({ event ->
            pastable.set(clipboardService.hasContentPastable())
        })
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

//            row.contextMenu = contextMenuRow

//            row.emptyProperty().addListener({ obs, wasEmpty, isNowEmpty ->
//                println "empty: $row.index / $isNowEmpty"
//                updateMenu(contextMenuRow, basePath, row.item as PathItem)
//            } as ChangeListener)

            def basePath = Paths.get(addressBar.text)
            updateMenu(tableView, contextMenuRow, basePath, row.item as PathItem)
            row.itemProperty().addListener({ obs, PathItem oldItem, PathItem newItem ->
                updateMenu(tableView, contextMenuRow, Paths.get(addressBar.text), row.item as PathItem)
            } as ChangeListener)

            row.contextMenuProperty().bind(
                    Bindings.when(Bindings.isNotNull(row.itemProperty()))
                            .then(contextMenuRow)
                            .otherwise((ContextMenu)null)
            )

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

    private void updateMenu(TableView tableView, ContextMenu contextMenu, Path basePath, PathItem item) {
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

            def menuItemCopy = new MenuItem("Copy")
            menuItemCopy.setOnAction({ event ->
                clipboardService.copy([itemPath])
                reload(basePath)
//                addContextMenuPaste(basePath)
            })
            contextMenu.items << menuItemCopy

            def menuItemCut = new MenuItem("Cut")
            menuItemCut.setOnAction({ event ->
                clipboardService.cut([itemPath])
                reload(basePath)
//                addContextMenuPaste(basePath)
            })
            contextMenu.items << menuItemCut

            def menuItemDelete = new MenuItem("Delete")
            menuItemDelete.setOnAction({ event ->
                Files.deleteIfExists(itemPath)
                reload(basePath)
            })
            contextMenu.items << menuItemDelete
        } else {
//            def menuItemNew = new Menu("New")
//            def menuItemNewFile = new MenuItem("File")
//            menuItemNewFile.setOnAction({ event ->
//                onNewFile()
//            })
//            def menuItemNewFolder = new MenuItem("Folder")
//            menuItemNewFolder.setOnAction({ event ->
//                onNewFolder()
//            })
//            menuItemNew.items.addAll(menuItemNewFile, menuItemNewFolder)
//            contextMenu.items << menuItemNew
        }

        contextMenu.items << new SeparatorMenuItem()
        contextMenu.items << getContextMenuPaste(basePath)
    }

    private MenuItem getContextMenuPaste(Path basePath) {
        def menuItemPaste = new MenuItem("Paste")
        menuItemPaste.setOnAction({ event ->
            clipboardService.paste(basePath)
            reload(basePath)
        })
        return menuItemPaste
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
        def fxml = FxmlUtils.loadFxml("/com/ericsantanna/filemanager/views/newFolder/new-folder.fxml")
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
        def fxml = FxmlUtils.loadFxml("/com/ericsantanna/filemanager/views/newFile/new-file.fxml")
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
