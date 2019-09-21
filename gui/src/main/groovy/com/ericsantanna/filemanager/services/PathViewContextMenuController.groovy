package com.ericsantanna.filemanager.services

import com.ericsantanna.filemanager.models.PathItem
import javafx.scene.control.ContextMenu

import java.nio.file.Path

interface PathViewContextMenuController {
    ContextMenu newContextMenu(Path basePath)
    void updatePathViewContextMenu(ContextMenu contextMenu, Path basePath, PathItem item);
}