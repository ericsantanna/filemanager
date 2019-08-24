package com.ericsantanna.filemanager.controllers;

import com.ericsantanna.filemanager.models.PathItem;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileListingTask extends Task<Void> {
    private Path path;
    private ObservableList<PathItem> data;

    public FileListingTask(Path path, ObservableList<PathItem> data) {
        this.path = path;
        this.data = data;
    }

    @Override
    protected Void call() throws Exception {
        try {
            var fileImage = new Image(getClass().getResource("/fxml/mainWindow/txt.jpeg").toString());
            var folderImage = new Image(getClass().getResource("/fxml/mainWindow/folder.png").toString());
            updateProgress(-1, 0);
            Files.list(path).forEach(p -> {
                try {
                    var icon = Files.isDirectory(p) ? folderImage : fileImage;
                    var size = Files.isDirectory(p) && Files.isReadable(p) ? getFolderSize(p) : Files.size(p);
                    var modified = Files.getLastModifiedTime(p);
                    data.add(new PathItem(icon, p, size, modified));
                } catch (AccessDeniedException e) {
                    System.out.println("Access denied in " + p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
