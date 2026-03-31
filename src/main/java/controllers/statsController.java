package controllers;

import dao.statsDao;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import java.util.List;

public class statsController {

    @FXML
    private BarChart<String, Number> revenueChart;
    @FXML
    private PieChart hotelBookingsChart;
    @FXML
    private Label messageLabel;

    private final statsDao dao = new statsDao();

    @FXML
    public void initialize() {
        String css = getClass().getResource("/stats.css").toExternalForm();
        revenueChart.getStylesheets().add(css);
        hotelBookingsChart.getStylesheets().add(css);
        loadRevenueChart();
        loadHotelBookingsChart();
    }

    private void loadRevenueChart() {
        List<String> months = dao.getRevenueMonths();
        List<Double> amounts = dao.getRevenueAmounts();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue (₹)");

        for (int i = 0; i < months.size(); i++) {
            series.getData().add(new XYChart.Data<>(months.get(i), amounts.get(i)));
        }

        revenueChart.getData().clear();
        revenueChart.getData().add(series);
        revenueChart.setLegendVisible(false);
    }

    private void loadHotelBookingsChart() {
        List<String> names = dao.getHotelNames();
        List<Integer> counts = dao.getHotelBookingCounts();

        hotelBookingsChart.getData().clear();

        for (int i = 0; i < names.size(); i++) {
            hotelBookingsChart.getData().add(
                    new PieChart.Data(names.get(i) + " (" + counts.get(i) + ")", counts.get(i)));
        }
    }
}