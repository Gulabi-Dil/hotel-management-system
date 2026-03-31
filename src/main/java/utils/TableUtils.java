package utils;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.util.List;

import behaviors.buttonHovering;

public class TableUtils {

    public static <T> void styleTable(TableView<T> table) {
        table.setStyle(
                "-fx-background-color: #6b061c;" +
                        "-fx-control-inner-background: #6b061c;" +
                        "-fx-table-cell-border-color: rgba(255,255,255,0.2);" +
                        "-fx-table-header-border-color: white;" +
                        "-fx-border-color: #6b061c;");

        table.setRowFactory(tv -> new TableRow<>());
    }

    public static void centerColumns(TableColumn<?, ?>... cols) { // destructuring like in js
        for (TableColumn<?, ?> col : cols) {
            col.setStyle("-fx-alignment: CENTER;");
        }
    }

    public static <T> void fixColumns(TableView<T> table, TableColumn<?, ?>... cols) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (TableColumn<?, ?> col : cols) {
            col.setResizable(false);
        }
    }

    public static <T> void addActionButtons(TableColumn<T, Void> col, java.util.function.Consumer<T> onEdit,
        java.util.function.Consumer<T> onDelete) {
    col.setCellFactory(c -> new TableCell<>() {

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
                return;
            }

            HBox box = new HBox(10);
            box.setAlignment(Pos.CENTER);

            if (onEdit != null) {
                Button editBtn = new Button("Edit");
                editBtn.setOnAction(e -> onEdit.accept(getTableView().getItems().get(getIndex())));
                editBtn.setOnMouseEntered(e -> buttonHovering.hovering(e));
                editBtn.setOnMouseExited(e -> buttonHovering.notHovering(e));
                buttonHovering.defaultButtonStyle(editBtn);
                box.getChildren().add(editBtn);
            }

            if (onDelete != null) {
                Button deleteBtn = new Button("Delete");
                deleteBtn.setOnAction(e -> onDelete.accept(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnMouseEntered(e -> buttonHovering.hovering(e));
                deleteBtn.setOnMouseExited(e -> buttonHovering.notHovering(e));
                buttonHovering.defaultButtonStyle(deleteBtn);
                box.getChildren().add(deleteBtn);
            }

            setGraphic(box);
        }
    });
}

    public static <T> void setupSearch(
            TextField searchField,
            List<T> sourceList,
            TableView<T> table,
            java.util.function.Predicate<T> filterLogic) {
        searchField.textProperty().addListener((obs, oldText, newText) -> {

            if (newText == null || newText.isEmpty()) {
                table.getItems().setAll(sourceList);
                return;
            }

            String lower = newText.toLowerCase();
            List<T> filtered = new java.util.ArrayList<>();

            for (T item : sourceList) {
                if (filterLogic.test(item)) {
                    filtered.add(item);
                }
            }

            table.getItems().setAll(filtered);
        });
    }
}