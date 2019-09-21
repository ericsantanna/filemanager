package com.ericsantanna.filemanager.services

import java.awt.Desktop
import java.nio.file.Path

class PathService {
    void openFile(Path path) {
        if(Desktop.isDesktopSupported()) {
            new Thread({
                try {
                    Desktop.desktop.open(path.toFile())
                } catch (IOException e) {
                    e.printStackTrace()
                }
            }).start()
        } else {
            System.err.println("Desktop not supported")
        }
    }
}
