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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import lombok.Getter;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;

@Getter
public class MainWindowController implements Initializable {
    @FXML private Button upBtn;
    @FXML private TextField addressBar;
    @FXML private ScrollPane fileView;
    @FXML private TableView fileList;

    private final ObservableList<PathItem> data = FXCollections.observableArrayList();

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
//        String homeDir = System.getProperty("user.home");
        String homeDir = "/home/esantanna/projects/icd-file-processor/icd6";
//        this.currentPath.bindBidirectional(this.addressBar.textProperty());
        addressBar.setText(homeDir);



//        tableView.setPrefHeight();

        TableColumn iconColumn = new TableColumn("Icon");
        iconColumn.setMaxWidth(500);
        TableColumn nameColumn = new TableColumn("Name");
        TableColumn sizeColumn = new TableColumn("Size");
        sizeColumn.setMaxWidth(900);
        TableColumn modifiedColumn = new TableColumn("Modified");
        modifiedColumn.setMaxWidth(1000);

        fileList.setRowFactory(tableView -> {
            TableRow row = new TableRow();
            row.setOnMouseClicked(event -> {
                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    var item = (PathItem) row.getItem();
                    var path = Paths.get(addressBar.getText()).resolve(item.getName());
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

//        data.addAll(
//                new PathItem(folderImage, "folder1", "1.1Mb"),
//                new PathItem(folderImage, "folder2", "0b"),
//                new PathItem(fileImage, "file1.txt", "20Kb"),
//                new PathItem(fileImage, "file2.txt", "20Kb"),
//                new PathItem(fileImage, "file3.txt", "20Kb")
//        );

        fileList.setItems(data);


        addressBar.setOnAction(event -> {
            data.clear();
            listPaths(addressBar.getText());
        });

        listPaths(addressBar.getText());
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

    public void listPaths(String path) {
        try {
            var fileImage = new Image(getClass().getResource("/fxml/mainWindow/txt.jpeg").toString());
            var folderImage = new Image(getClass().getResource("/fxml/mainWindow/folder.png").toString());
            Files.list(Paths.get(path)).forEach(p -> {
                try {
                    var icon = Files.isDirectory(p) ? folderImage : fileImage;
                    var size = Files.isDirectory(p) && Files.isReadable(p) ? getFolderSize(p) : Files.size(p);
                    var modified = Files.getLastModifiedTime(p);
                    data.add(new PathItem(icon, p.getFileName().toString(), size, modified));
                } catch (AccessDeniedException e) {
                    System.out.println("Access denied in " + p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getFolderSize(Path path) throws IOException {
        FolderSizeChecker folderSizeChecker = new FolderSizeChecker();
        Files.walkFileTree(path, folderSizeChecker);
        return folderSizeChecker.getTotalSize();
    }

    static class FolderSizeChecker implements FileVisitor<Path> {
        private long totalSize;

        public long getTotalSize() {
            return totalSize;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try {
                totalSize += Files.size(file);
            } catch (Exception e) {
                // ignore
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
