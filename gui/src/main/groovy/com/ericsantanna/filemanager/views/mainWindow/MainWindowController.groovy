package com.ericsantanna.filemanager.views.mainWindow

import com.ericsantanna.filemanager.controllers.FileListingTask
import com.ericsantanna.filemanager.models.PathItem
import com.ericsantanna.filemanager.services.*
import com.ericsantanna.filemanager.utils.FxmlUtils
import com.ericsantanna.filemanager.views.newFile.NewFileController
import com.ericsantanna.filemanager.views.newFolder.NewFolderController
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.ProgressBar
import javafx.scene.control.ScrollPane
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Pane
import javafx.scene.layout.TilePane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.Callback
import org.controlsfx.control.GridCell
import org.controlsfx.control.GridView
import org.controlsfx.control.cell.ImageGridCell

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainWindowController implements Initializable {
    @FXML private TextField addressBar
    @FXML private ScrollPane fileView
//    @FXML private TableView fileList
    @FXML private GridView<PathItem> gridView
    @FXML private ProgressBar progressBarMain

    final ClipboardService clipboardService = new ClipboardService()
    PathService pathService
    PathViewContextMenuController pathViewContextMenuController
    PathView pathView

    private final ObservableList<PathItem> data = FXCollections.observableArrayList()
    private ExecutorService executorService = Executors.newFixedThreadPool(4)

    private ObjectProperty<Path> pathProperty = new SimpleObjectProperty<>()

    @FXML
    void initialize(URL location, ResourceBundle resources) {
//        String homeDir = System.getProperty("user.home");
        String homeDir = "/tmp/filemanager"
        Path currentPath = Paths.get(homeDir)

        Files.createDirectories(currentPath)

        pathProperty.addListener({ observableValue, oldValue, newValue ->
            addressBar.text = newValue.toString()
            reload(newValue)
        } as ChangeListener<Path>)

        pathProperty.set(currentPath)
        data.clear()
        FileListingTask fileListingTask = new FileListingTask(currentPath, data)
        progressBarMain.progressProperty().bind(fileListingTask.progressProperty())

        addressBar.setOnAction({ event ->
            pathProperty.set(Paths.get(addressBar.text))
        })


//        pathService = new PathService()
//        pathViewContextMenuController = new DefaultPathViewContextMenuController(this, clipboardService, pathService)
//        pathView = new com.ericsantanna.filemanager.views.fileView.tableView.TableView(fileList, clipboardService, data, pathProperty, pathViewContextMenuController, pathService)

//        def a = new ListView<PathItem>()
//        a.setCellFactory({new ListView<PathItem>() {}})
        gridView.setCellFactory({ p ->
            new GridCell<PathItem>() {
                @Override
                protected void updateItem(PathItem item, boolean empty) {
                    super.updateItem(item, empty)
                    setGraphic(empty ? null : item.getIcon())
                }
            }
        } as Callback<GridView<PathItem>, GridCell<PathItem>>)

        gridView.items = tiles
    }
    private final ObservableList<PathItem> tiles = FXCollections.observableArrayList()
    int count = 0
    @FXML
    void onUp() {
//        Path parent = pathProperty.get().getParent()
//        pathProperty.set(parent)
        def image = new Image(getClass().getResource("/fxml/images/txt.jpeg").toString())
        def label = new ImageView(image)
//        label.fitHeight = 80
//        label.fitWidth = 80
//        label.preserveRatio = true
////        gridView.prefHeight = 80
////        gridView.prefWidth = 80
////        label.minWidth(100)
////        label.minHeight(100)
////        label.maxWidth(150)
////        label.maxHeight(150)
//        if(count == 0) {
//            label.style = '-fx-background-color: #F00;'
////            label.text = 'Label ' + count
//            count++
//        } else if(count == 1) {
//            label.style = '-fx-background-color: #0F0;'
////            label.text = 'Label ' + count
//            count++
//        } else {
//            label.style = '-fx-background-color: #00F;'
////            label.text = 'Label ' + count
//            count = 0
//        }

        def pi = new PathItem(image, null, 0, null)
        tiles << pi
//        gridView.children.add(label)
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
        controller.currentPath = pathProperty.get()

        stage.showAndWait()

        reload(pathProperty.get())
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
        controller.currentPath = pathProperty.get()

        stage.showAndWait()

        reload(pathProperty.get())
    }

    void reload(Path path) throws Exception {
        def fileListingTask = new FileListingTask(path, data)
        progressBarMain.progressProperty().bind(fileListingTask.progressProperty())
        executorService.submit(fileListingTask)
    }
}
