package com.ericsantanna.filemanager.services

import com.ericsantanna.filemanager.models.PathItem
import com.ericsantanna.filemanager.views.mainWindow.MainWindowController
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem

import java.nio.file.Files
import java.nio.file.Path

class DefaultPathViewContextMenuController implements PathViewContextMenuController {
    private MainWindowController mainWindowController
    private PathService pathService
    private BooleanProperty menuItemPasteProperty = new SimpleBooleanProperty(false)
    private ClipboardService clipboardService

    DefaultPathViewContextMenuController(MainWindowController mainWindowController, ClipboardService clipboardService, PathService pathService) {
        this.mainWindowController = mainWindowController
        this.clipboardService = clipboardService
        this.pathService = pathService
    }

    private void addMenuItemPaste(ContextMenu contextMenu, Path basePath) {
        def menuItemPaste = new MenuItem("Paste")
        menuItemPaste.setOnAction({ event ->
            clipboardService.paste(basePath)
            mainWindowController.reload(basePath)
        })

        menuItemPaste.visibleProperty().bind(menuItemPasteProperty)

        contextMenu.items << menuItemPaste

        contextMenu.setOnShowing({ event ->
            menuItemPasteProperty.set(clipboardService.hasContentPastable())
        })
    }

    private void addMenuItemNew(ContextMenu contextMenu) {
        def menuItemNew = new Menu("New")
        def menuItemNewFile = new MenuItem("File")
        menuItemNewFile.setOnAction({ event ->
            mainWindowController.onNewFile()
        })
        def menuItemNewFolder = new MenuItem("Folder")
        menuItemNewFolder.setOnAction({ event ->
            mainWindowController.onNewFolder()
        })
        menuItemNew.items.addAll(menuItemNewFile, menuItemNewFolder)

        contextMenu.items << menuItemNew
    }

    ContextMenu newContextMenu(Path basePath) {
        def contextMenu = new ContextMenu()
        contextMenu.minWidth = 300d
        contextMenu.prefWidth = 500
        contextMenu.width = 300d
        contextMenu.style = '-fx-pref-width: 200'

        addMenuItemPaste(contextMenu, basePath)
        addMenuItemNew(contextMenu)

        return contextMenu
    }

    void updatePathViewContextMenu(ContextMenu contextMenu, Path basePath, PathItem item) {
        def itemPath = item ? basePath.resolve(item.name) : null

        contextMenu.items.clear()

        addMenuItemPaste(contextMenu, basePath)

        if(item) {
            def menuItemCopy = new MenuItem("Copy")
            menuItemCopy.setOnAction({ event ->
                clipboardService.copy([itemPath])
                mainWindowController.reload(basePath)
            })
            contextMenu.items << menuItemCopy

            def menuItemCut = new MenuItem("Cut")
            menuItemCut.setOnAction({ event ->
                clipboardService.cut([itemPath])
                mainWindowController.reload(basePath)
            })
            contextMenu.items << menuItemCut

            def menuItemOpen = new MenuItem("Open")
            menuItemOpen.setOnAction({ event ->
                pathService.openFile(itemPath)
            })
            contextMenu.items << menuItemOpen

            def menuItemDelete = new MenuItem("Delete")
            menuItemDelete.setOnAction({ event ->
                Files.deleteIfExists(itemPath)
                mainWindowController.reload(basePath)
            })
            contextMenu.items << menuItemDelete

            contextMenu.items << new SeparatorMenuItem()
        }

        addMenuItemNew(contextMenu)
    }
}
