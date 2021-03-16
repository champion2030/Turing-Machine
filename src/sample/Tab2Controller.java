package sample;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class Tab2Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField input;

    @FXML
    private Button stop;

    @FXML
    private Button start;

    @FXML
    private TextArea output_2;

    @FXML
    private Button draw;

    @FXML
    private TextArea output_1;

    @FXML
    private GridPane gridpane;

    private XYChart.Series<String, Number> lineSeries;
    static final List<Integer> numbers = Collections.synchronizedList(new ArrayList<Integer>());
    boolean isAlive = false, clicked = false;
    private int k, count, right, left, k1, right1, left1;
    private volatile int maxCount = 0;
    private int cycle = 0;
    Service<Void> service1, service;
    LineChart<String, Number> lineChart;
    private NumberAxis yAxis;
    private double lowerBound = Double.MAX_VALUE;
    private double upperBound = Double.MIN_VALUE;
    StringBuffer tempStr, viewStr, firstStr, secondStr, tempStr1, viewStr1 = new StringBuffer("L");
    String z = "";

    public void initialize() {

        start.setOnAction(actionEvent -> {
            output_1.clear();
            output_2.clear();
            k = 0;
            try {
                MainWork();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        draw.setOnAction(actionEvent -> {
            if (isAlive){
                service.reset();
                service1.reset();
                lineSeries.getData().clear();
                gridpane.getChildren().remove(lineChart);
                numbers.clear();
                initSeries();
                lineChart = createLineChart();
                gridpane.add(lineChart, 2, 1);
                cycle = 0;
                service.start();
                service1.start();
                stop.setText("Остановить график");
            }else {
                initSeries();
                lineChart = createLineChart();
                gridpane.add(lineChart, 2, 1);
                secondThread();
                service.start();
                firstThread();
                service1.start();
            }
            stop.setDisable(false);
            draw.setDisable(true);
        });

        stop.setOnAction(actionEvent -> {
            if(service1.isRunning()){
                isAlive = true;
                service.cancel();
                service1.cancel();
                stop.setText("Возобновить");
                stop.setDisable(false);
                draw.setDisable(false);
            }
            else {
                service.restart();
                service1.restart();
                stop.setText("Остановить график");
                draw.setDisable(true);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initSeries() {
        lineSeries = new XYChart.Series(FXCollections.observableArrayList());
        createYaxis();
    }

    private void firstThread(){
        service1 = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() throws Exception {
                        while (true)
                        {
                            synchronized (numbers)
                            {
                                maxCount = 0;
                                cycle += 1;
                                PermutationsWithRepetition gen = new PermutationsWithRepetition(new String[]{"a", "b", "c", "d"},cycle);
                                String[][] variations = gen.getVariations();
                                for (String[] s : variations) {
                                    count = 0;
                                    k1 = 0;
                                    q0T(s);
                                    if(count > maxCount){
                                        maxCount = count;
                                    }
                                }
                                numbers.add(maxCount);
                                numbers.add(cycle);
                                numbers.notify();
                            }
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                if (isCancelled()){
                                    break;
                                }
                            }
                        }
                        return null;
                    }
                };
            }
        };
    }

    private void secondThread(){
        service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call() throws Exception {
                        while (numbers.isEmpty())
                        {
                            synchronized (numbers)
                            {
                                try {
                                    numbers.wait();
                                } catch (Exception e) {
                                    if (isCancelled()){
                                        break;
                                    }
                                }
                                int A = numbers.remove(0);
                                int B = numbers.remove(0);
                                Platform.runLater(() -> {
                                    lineSeries.getData().add(new XYChart.Data<>(String.valueOf(B), A));
                                    lowerBound = Double.MAX_VALUE;
                                    upperBound = Double.MIN_VALUE;
                                    for (int j = 0; j < lineSeries.getData().size(); j++) {
                                        lowerBound = Math.min(lowerBound, lineSeries.getData().get(j).getYValue().doubleValue());
                                        upperBound = Math.max(upperBound, lineSeries.getData().get(j).getYValue().doubleValue());
                                    }
                                    yAxis.setLowerBound(lowerBound);
                                    yAxis.setUpperBound(upperBound);
                                });
                            }
                        }
                        return null;
                    }
                };
            }
        };

    }

    private void createYaxis() {
        yAxis = new NumberAxis();
        yAxis.setAutoRanging(false);
        yAxis.setPrefWidth(25);
        yAxis.setMinorTickCount(15);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(20);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override public String toString(Number object) {
                return String.format("%7.2f", object.floatValue());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private LineChart<String, Number> createLineChart() {
        final LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), yAxis);
        setDefaultChartProperties(chart);
        chart.setCreateSymbols(false);
        chart.getData().addAll(lineSeries);
        return chart;
    }

    private void setDefaultChartProperties(final XYChart<String, Number> chart) {
        chart.setLegendVisible(false);
        chart.setAnimated(false);
    }

    private void MainWork() throws FileNotFoundException {
        output_1.clear();
        output_2.clear();
        left = right = 0;
        String[] word = new String[input.getText().length()];
        String str = input.getText();
        clicked = true;
        viewStr = new StringBuffer();
        viewStr1 = new StringBuffer();
        tempStr1 = new StringBuffer();
        secondStr = new StringBuffer();
        for (int i = 0; i < str.length(); i++) { word[i] = String.valueOf(str.charAt(i)); }
        tempStr =  new StringBuffer(input.getText());
        viewStr.append(tempStr.insert(k, "q0")).append("\n");
        z += tempStr + "\n";
        tempStr.delete(0,2);
        q1T(word);
    }

    private void q0T(String[] word){
        firstStr = new StringBuffer();
        right1 = left1 = 0;
        q1(word);
    }

    private void q1T(String[] word) throws FileNotFoundException {
        if (k == word.length-1){
            finishT(word);
        }else {
            switch (word[k]) {
                case "a" -> {
                    secondStr.append(word[k]);
                    viewStr.append(tempStr.insert(k, "q1")).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                    tempStr1.append(word[k]);
                    viewStr1.append(tempStr1.insert(k, "q1")).append("\n");
                    z += tempStr + "\n";
                    tempStr1.delete(k, k + 2);
                    k++;
                    q2T(word);
                }
                case "b", "c", "d" -> finishT(word);
            }
        }
    }

    private void q2T(String[] word) throws FileNotFoundException {
        if (k == word.length-1){
            finishT(word);
        }else {
            switch (word[k]) {
                case "d" -> {
                    secondStr.append(word[k]);
                    viewStr.append(tempStr.insert(k, "q2")).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                    tempStr1.append(word[k]);
                    viewStr1.append(tempStr1.insert(k, "q2")).append("\n");
                    z += tempStr + "\n";
                    tempStr1.delete(k, k + 2);
                    k++;
                    if(word[k].equals("a")) {
                        q1T(word);
                    } else {
                        right = left = k;
                        q3T(word);
                    }
                }
                case "b", "c", "a" -> finishT(word);
            }
        }
    }

    private void q3T(String[] word) throws FileNotFoundException {
        if (k == word.length-1){
            finishT(word);
        }else {
            switch (word[k]) {
                case "c" -> {
                    viewStr.append(tempStr.insert(right, "q3")).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(right, right+2);
                    viewStr1.append(tempStr1.insert(left, "q3")).append("\n");
                    z += tempStr + "\n";
                    tempStr1.delete(left, left + 2);
                    right--;
                    q4T(word);
                }
                case "b", "d", "a" -> finishT(word);
            }
        }
    }

    private void q4T(String[] word) throws FileNotFoundException {
        if (left == word.length - 1 && right == 0 && word[left].equals("b") && secondStr.charAt(right) == 'a') {
            finishG();
        } else if (left == word.length - 1 && right != 0) {
            finishT(word);
        } else if (left != word.length - 1 && right == 0) {
            finishT(word);
        } else {
            if (word[left].equals("c") && secondStr.charAt(right) == 'd') {
                right--;
                left++;
                viewStr.append(tempStr.insert(left, "q4")).append("\n");
                z += tempStr + "\n";
                tempStr.delete(left, left + 2);
                viewStr1.append(tempStr1.insert(right, "q4")).append("\n");
                z += tempStr + "\n";
                tempStr1.delete(right, right + 2);
                q4T(word);
            } else if (word[left].equals("b") && secondStr.charAt(right) == 'a') {
                    right--;
                    left++;
                    viewStr.append(tempStr.insert(left, "q4")).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(left, left + 2);
                    viewStr1.append(tempStr1.insert(right, "q4")).append("\n");
                    z += tempStr + "\n";
                    tempStr1.delete(right, right + 2);
                    q4T(word);
                } else {
                    finishT(word);
                }
            }
    }

    private void finishT(String[] word) throws FileNotFoundException {
        while (left != word.length) {
            left++;
            viewStr.append(tempStr.insert(left, "q5")).append("\n");
            z += tempStr + "\n";
            tempStr.delete(left, left + 2);
        }
        viewStr.append(tempStr.insert(left, "0")).append("\n");
        z += tempStr + "\n";
        tempStr.delete(left, left + 2);
        output_1.setText(String.valueOf(viewStr));
        output_2.setText(String.valueOf(viewStr1));
        PrintStream printStream = new PrintStream("TwoLenta.txt");
        printStream.print(z);
    }

    private void finishG() throws FileNotFoundException {
        viewStr.append(tempStr.insert(tempStr.length(), 1));
        z += tempStr + "\n";
        output_1.setText(String.valueOf(viewStr));
        output_2.setText(String.valueOf(viewStr1));
        PrintStream printStream = new PrintStream("TwoLenta.txt");
        printStream.print(z);
    }

    private void q1(String[] word){
        if (k1 == word.length-1){
            finish(word);
        }else {
            switch (word[k1]) {
                case "a" -> {
                    firstStr.append(word[k1]);
                    count++;
                    k1++;
                    q2(word);
                }
                case "b", "c", "d" -> finish(word);
            }
        }
    }

    private void q2(String[] word){
        if (k1 == word.length-1){
            finish(word);
        }else {
            switch (word[k1]) {
                case "d" -> {
                    firstStr.append(word[k1]);
                    k1++;
                    count++;
                    if(word[k1].equals("a")) {
                        q1(word);
                    } else {
                        right1 = left1 = k1;
                        q3(word);
                    }
                }
                case "b", "c", "a" -> finish(word);
            }
        }
    }

    private void q3(String[] word){
        if (k1 == word.length-1){
            finish(word);
        }else {
            switch (word[k1]) {
                case "c" -> {
                    right1--;
                    count++;
                    q4(word);
                }
                case "b", "d", "a" -> finish(word);
            }
        }
    }

    private void q4(String[] word){
            if (left1 == word.length - 1 && right1 == 0 && word[left1].equals("b") && firstStr.charAt(right1) == 'a') {
                finishN();
            } else if (left1 == word.length - 1 && right1 != 0) {
                finish(word);
            } else if (left1 != word.length - 1 && right1 == 0) {
                finish(word);
            } else {
                if (word[left1].equals("c") && firstStr.charAt(right1) == 'd') {
                    right1--;
                    left1++;
                    count++;
                    q4(word);
                } else if (word[left1].equals("b") && firstStr.charAt(right1) == 'a') {
                    right1--;
                    left1++;
                    count++;
                    q4(word);
                } else {
                    finish(word);
                }
            }
    }

    private void finish(String[] word){
        while (left1 != word.length) {
            left1++;
            count++;
        }
        count += 2;
    }

    private void finishN(){
        count++;
    }
}
