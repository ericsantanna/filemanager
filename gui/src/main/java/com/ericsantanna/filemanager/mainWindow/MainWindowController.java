package com.ericsantanna.filemanager.mainWindow;

import com.ericsantanna.filemanager.models.PathItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import lombok.Getter;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Getter
public class MainWindowController implements Initializable {
//    private Property<Path> pathProperty = new SimpleObjectProperty<>();
    @FXML private Button upBtn;
    @FXML private TextField addressBar;
    @FXML private ScrollPane fileView;
    @FXML private TableView fileList;
    @FXML private ProgressBar mainProgressBar;

    private final ObservableList<PathItem> data = FXCollections.observableArrayList();
    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
//        String homeDir = System.getProperty("user.home");
        String homeDir = "/home/esantanna/projects/icd-file-processor/icd6";
//        this.addressBar.textProperty().addListener();
//        addressBar.textProperty().bindBidirectional(pathProperty, Bindings.createStringBinding(() -> {return null;}));
//        Bindings.createStringBinding(addressBar.textProperty(), pathProperty, new Format() {
//
//            @Override
//            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
//                if(obj == null) return null;
//                var path = ((Path) obj);
//                toAppendTo.delete(0, toAppendTo.length());
//                toAppendTo.append(path.toString());
//                return toAppendTo;
//            }
//
//            @Override
//            public Object parseObject(String source, ParsePosition pos) {
//                return Paths.get(source);
//            }
//        });

        addressBar.setText(homeDir);

        Path currentPath = Paths.get(addressBar.getText());
        data.clear();
        var fileListingTask = new FileListingTask(currentPath, data);
        mainProgressBar.progressProperty().bind(fileListingTask.progressProperty());
//        Future<PathItem> future = (Future<PathItem>) executorService.submit(fileListingTask);

//        tableView.setPrefHeight();

        TableColumn iconColumn = new TableColumn("Icon");
        iconColumn.setMaxWidth(500);
        TableColumn nameColumn = new TableColumn("Name");
        TableColumn sizeColumn = new TableColumn("Size");
        sizeColumn.setMaxWidth(900);
        TableColumn modifiedColumn = new TableColumn("Modified");
        modifiedColumn.setMaxWidth(1000);

        fileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

//        fileList.setOnKeyPressed(keyEvent -> {
////            row.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
//            var keyCodeCombination = new KeyCodeCombination(KeyCode.C, KeyCodeCombination.CONTROL_DOWN);
//            if(keyCodeCombination.match(keyEvent)) {
//                System.out.println("hi");
//                keyEvent.consume();
//            }
//        });
        fileList.setOnKeyPressed(keyEvent -> {
            var selectedItems = fileList.getSelectionModel().getSelectedItems();
//            var item = (PathItem) row.getItem();
            var keyCodeCombination = new KeyCodeCombination(KeyCode.C, KeyCodeCombination.CONTROL_DOWN);
            if(keyCodeCombination.match(keyEvent)) {
                var clipboardContent = new ClipboardContent();
                List<File> files = (List<File>) selectedItems.stream().map(i -> ((PathItem) i).getPath().toFile()).collect(Collectors.toList());
//                clipboardContent.putFiles(files);
//                clipboardContent.putFiles(java.util.Collections.singletonList(new File("/home/esantanna/IdeaProjects/filemanager/gui/pom.xml")));
                DataFormat GNOME_FILES = new DataFormat("x-special/gnome-copied-files");
//                DataFormat GNOME_FILES = new DataFormat("text/uri-list");

//                if (platform == Platform.Linux64 || platform == Platform.Linux32) {

                    final StringBuilder builder = new StringBuilder("copy\n");

                    files.forEach(file ->
                            builder.append(file.toPath().toUri().toASCIIString()).append('\n'));
                    builder.append("\0");

//                    final StringBuilder builder = new StringBuilder();
//
//                    files.forEach(file ->
//                            builder.append(file.toPath()).append('\n'));
//                    builder.append("\\" + "0");
//                    builder.delete(builder.length() - 1, builder.length());

                    final ByteBuffer buffer = ByteBuffer.allocate(builder.length());

                    for (int i = 0, length = builder.length(); i < length; i++) {
                        buffer.put((byte) builder.charAt(i));
                    }

                    buffer.flip();

                    clipboardContent.put(GNOME_FILES, buffer);
                Clipboard.getSystemClipboard().setContent(clipboardContent);

//                }
                keyEvent.consume();
            }
        });

        fileList.setRowFactory(tableView -> {
            TableRow row = new TableRow();
            row.setOnMouseClicked(event -> {
                var item = (PathItem) row.getItem();
                var path = Paths.get(addressBar.getText()).resolve(item.getName());
                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    if(Files.isDirectory(path)) {
                        addressBar.setText(path.toString());
                        addressBar.fireEvent(new ActionEvent());
                    } else {
                        openFile(path);
                    }
                }
            });
            var openMenuItem = new MenuItem("Open");
            openMenuItem.setOnAction(event -> {
                var item = (PathItem) row.getItem();
                var path = Paths.get(addressBar.getText()).resolve(item.getName());
                openFile(path);
            });
            var deleteMenuItem = new MenuItem("Delete");
            deleteMenuItem.setOnAction(event -> {
                var item = (PathItem) row.getItem();
                var path = Paths.get(addressBar.getText()).resolve(item.getName());
                System.out.println("Deleted: " + path);
            });
            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setMinWidth(300d);
            contextMenu.setPrefWidth(500);
            contextMenu.setWidth(300d);
            contextMenu.setStyle("-fx-pref-width: 200");
            contextMenu.getItems().addAll(
                    openMenuItem,
                    deleteMenuItem
            );
            row.setContextMenu(contextMenu);
            return row;
        });
//
        iconColumn.setCellValueFactory(new PropertyValueFactory<PathItem, ImageView>("icon"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<PathItem, String>("name"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<PathItem, String>("size"));
        modifiedColumn.setCellValueFactory(new PropertyValueFactory<PathItem, String>("modified"));
        fileList.getColumns().addAll(iconColumn, nameColumn, sizeColumn, modifiedColumn);

//        try {
//            List<PathItem> pathItems = (List<PathItem>) future.get(1000, TimeUnit.MINUTES);
//            data.addAll(pathItems);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
//        }


        fileList.setItems(data);

        try {
            addressBar.setOnAction(event -> {
                data.clear();
                try {
                    listPaths(addressBar.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            listPaths(addressBar.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
//        fileView.setContent(tableView);
    }

    private void openFile(Path path) {
        if(Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().open(path.toFile());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            System.err.println("Desktop not supported");
        }
    }

    @FXML
    public void onUp(ActionEvent event) {
        var path = Paths.get(addressBar.getText()).getParent();
        addressBar.setText(path.toString());
        addressBar.fireEvent(new ActionEvent());
    }

    public void listPaths(String path) throws Exception {

        var fileListingTask = new FileListingTask(Paths.get(path), data);
        mainProgressBar.progressProperty().bind(fileListingTask.progressProperty());
        executorService.submit(fileListingTask);
    }
}
