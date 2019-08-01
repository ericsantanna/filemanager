package com.ericsantanna.filemanager.models;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class PathItem {
    private Image icon;
    private String name;
    private long size;

    public PathItem(Image icon, String name, long size) {
        this.icon = icon;
        this.name = name;
        this.size = size;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return formatSize(size);
    }

    public void setSize(long size) {
        this.size = size;
    }

    public ImageView getIcon() {
        var imageView = new ImageView(icon);
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);
        return imageView;
    }

    private String formatSize(long sizeInBytes) {
        final long KB = 1000;
        final long MB = KB * 1000;
        final long GB = MB * 1000;
        DecimalFormat decimalFormater = new DecimalFormat("#.#");
        decimalFormater.setRoundingMode(RoundingMode.DOWN);
        if(sizeInBytes < KB) {
            return String.format("%d bytes", sizeInBytes);
        }
        if(sizeInBytes < MB) {
            return String.format("%s kB", decimalFormater.format(((double)sizeInBytes / KB)));
        }
        if(sizeInBytes < GB) {
            return String.format("%s MB", decimalFormater.format(((double)sizeInBytes / MB)));
        }
        else {
            return String.format("%s GB", decimalFormater.format(((double)sizeInBytes / GB)));
        }
    }
}
