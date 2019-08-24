package com.ericsantanna.filemanager.controllers

import com.ericsantanna.filemanager.models.PathItem
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.scene.image.Image

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime

class FileListingTask extends Task<Void> {
    private Path path
    private ObservableList<PathItem> data

    FileListingTask(Path path, ObservableList<PathItem> data) {
        this.path = path
        this.data = data
    }

    @Override
    protected Void call() throws Exception {
        try {
            Image fileImage = new Image(getClass().getResource("/fxml/mainWindow/txt.jpeg").toString())
            Image folderImage = new Image(getClass().getResource("/fxml/mainWindow/folder.png").toString())
            updateProgress(-1, 0)
            data.clear()
            Files.list(path).forEach( { p ->
                try {
                    Image icon = Files.isDirectory(p) ? folderImage : fileImage
                    long size = Files.isDirectory(p) && Files.isReadable(p) ? getFolderSize(p) : Files.size(p)
                    FileTime modified = Files.getLastModifiedTime(p)
                    data.add(new PathItem(icon, p, size, modified))
                } catch (AccessDeniedException e) {
                    System.out.println("Access denied in " + p)
                } catch (IOException e) {
                    e.printStackTrace()
                }
            })
            updateProgress(0, 0)
        } catch (IOException e) {
            e.printStackTrace()
        }
        return null
    }

    long getFolderSize(Path path) throws IOException {
        FolderSizeChecker folderSizeChecker = new FolderSizeChecker()
        Files.walkFileTree(path, folderSizeChecker)
        return folderSizeChecker.getTotalSize()
    }

    static class FolderSizeChecker implements FileVisitor<Path> {
        private long totalSize

        long getTotalSize() {
            return totalSize
        }

        @Override
        FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try {
                totalSize += Files.size(file)
            } catch (Exception e) {
                // ignore
            }
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE
        }
    }
}
