package com.ericsantanna.filemanager.models

import javafx.scene.image.Image
import javafx.scene.image.ImageView

import java.math.RoundingMode
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PathItem {
    private Path path
    private Image icon
    private long size
    private FileTime modified

    PathItem(Image icon, Path path, long size, FileTime modified) {
        this.icon = icon
        this.path = path
        this.size = size
        this.modified = modified
    }

    Path getPath() {
        return path
    }

    void setPath(Path path) {
        this.path = path
    }

    void setIcon(Image icon) {
        this.icon = icon
    }

    String getName() {
        return path.getFileName().toString()
    }

    String getSize() {
        return formatSize(size)
    }

    void setSize(long size) {
        this.size = size
    }

    String getModified() {
        LocalDateTime dateTime = modified.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return dateTime.format(dateTimeFormatter)
    }

    void setModified(FileTime modified) {
        this.modified = modified
    }

    ImageView getIcon() {
        ImageView imageView = new ImageView(icon)
        imageView.setFitHeight(30)
        imageView.setFitWidth(30)
        return imageView
    }

    private String formatSize(long sizeInBytes) {
        final long KB = 1000
        final long MB = KB * 1000
        final long GB = MB * 1000
        DecimalFormat decimalFormater = new DecimalFormat("#.#")
        decimalFormater.setRoundingMode(RoundingMode.DOWN)
        if(sizeInBytes < KB) {
            return String.format("%d bytes", sizeInBytes)
        }
        if(sizeInBytes < MB) {
            return String.format("%s kB", decimalFormater.format(((double)sizeInBytes / KB)))
        }
        if(sizeInBytes < GB) {
            return String.format("%s MB", decimalFormater.format(((double)sizeInBytes / MB)))
        }
        else {
            return String.format("%s GB", decimalFormater.format(((double)sizeInBytes / GB)))
        }
    }
}
