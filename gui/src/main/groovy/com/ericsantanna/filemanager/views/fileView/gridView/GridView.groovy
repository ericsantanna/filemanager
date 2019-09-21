package com.ericsantanna.filemanager.views.fileView.gridView

import com.ericsantanna.filemanager.models.PathItem
import com.ericsantanna.filemanager.services.ClipboardService
import com.ericsantanna.filemanager.services.PathService
import com.ericsantanna.filemanager.services.PathView
import com.ericsantanna.filemanager.services.PathViewContextMenuController
import javafx.beans.property.ObjectProperty
import javafx.beans.value.ChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.MouseButton

import java.nio.file.Files
import java.nio.file.Path

class GridView implements PathView {
    javafx.scene.control.TableView<PathItem> tableView
    ClipboardService clipboardService
    PathViewContextMenuController pathViewContextMenuController

    GridView(javafx.scene.control.TableView<PathItem> tableView, ClipboardService clipboardService, ObservableList<PathItem> data, ObjectProperty<Path> pathProperty, PathViewContextMenuController pathViewContextMenuController, PathService pathService) {
        this.pathViewContextMenuController = pathViewContextMenuController
        this.clipboardService = clipboardService
        this.tableView = tableView

        TableColumn iconColumn = new TableColumn("Icon")
        iconColumn.maxWidth = 500
        TableColumn nameColumn = new TableColumn("Name")
        TableColumn sizeColumn = new TableColumn("Size")
        sizeColumn.maxWidth = 900
        TableColumn modifiedColumn = new TableColumn("Modified")
        modifiedColumn.maxWidth = 1000

        tableView.selectionModel.selectionMode = SelectionMode.MULTIPLE

        tableView.setOnKeyPressed({ keyEvent ->
            def selectedItems = tableView.selectionModel.selectedItems
            def paths = selectedItems.collect { i -> (i as PathItem).path } as List<Path>

            def ctrlC = new KeyCodeCombination(KeyCode.C, KeyCodeCombination.CONTROL_DOWN)
            if (ctrlC.match(keyEvent)) {
                clipboardService.copy(paths)
                keyEvent.consume()
            }

            def ctrlX = new KeyCodeCombination(KeyCode.X, KeyCodeCombination.CONTROL_DOWN)
            if (ctrlX.match(keyEvent)) {
                clipboardService.cut(paths)
                keyEvent.consume()
            }

            def ctrlV = new KeyCodeCombination(KeyCode.V, KeyCodeCombination.CONTROL_DOWN)
            if (ctrlV.match(keyEvent)) {
                clipboardService.paste(pathProperty.get())
                keyEvent.consume()
            }
        })

        tableView.contextMenu = pathViewContextMenuController.newContextMenu(pathProperty.get())

        tableView.setRowFactory({ tb ->
            def row = new TableRow()
            row.setOnMouseClicked({ event ->
                def item = row.item as PathItem
                if(!item) {
                    return
                }
                def itemPath = item ? pathProperty.get().resolve(item.name) : null
                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    if(Files.isDirectory(itemPath)) {
                        pathProperty.set(itemPath)
                    } else {
                        pathService.openFile(itemPath)
                    }
                }
            })

            row.contextMenu = pathViewContextMenuController.newContextMenu(pathProperty.get())

            row.itemProperty().addListener({ obs, PathItem oldItem, PathItem newItem ->
                pathViewContextMenuController.updatePathViewContextMenu(row.contextMenu, pathProperty.get(), newItem)
            } as ChangeListener)

            return row
        })

        iconColumn.cellValueFactory = new PropertyValueFactory<PathItem, ImageView>("icon")
        nameColumn.cellValueFactory = new PropertyValueFactory<PathItem, String>("name")
        sizeColumn.cellValueFactory = new PropertyValueFactory<PathItem, String>("size")
        modifiedColumn.cellValueFactory = new PropertyValueFactory<PathItem, String>("modified")
        tableView.columns.addAll(iconColumn, nameColumn, sizeColumn, modifiedColumn)

        tableView.items = data
    }
}
