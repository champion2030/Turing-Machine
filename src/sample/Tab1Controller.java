package sample;

import java.io.FileWriter;
import java.io.IOException;
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

public class Tab1Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField textinput1;

    @FXML
    private Button start_graphic;

    @FXML
    private Button stopGraphics;

    @FXML
    private TextArea output_1;

    @FXML
    private GridPane gridpane;

    @FXML
    private Button start_1;

    private XYChart.Series<String, Number> lineSeries;
    static final List<Integer> numbers = Collections.synchronizedList(new ArrayList<Integer>());
    boolean isAlive = false, clicked = false;
    private int k, count, count1;
    private volatile int maxCount = 0;
    private int cycle = 0;
    Service<Void> service1, service;
    LineChart<String, Number> lineChart;
    private NumberAxis yAxis;
    private double lowerBound = Double.MAX_VALUE;
    private double upperBound = Double.MIN_VALUE;
    StringBuffer tempStr, viewStr = new StringBuffer();
    String z = "";

    public void initialize() {

        start_1.setOnAction(actionEvent -> {
            output_1.clear();
            k = 0;
            try {
                MainWork();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        start_graphic.setOnAction(actionEvent -> {
            if (isAlive){
                service.reset();
                service1.reset();
                lineSeries.getData().clear();
                gridpane.getChildren().remove(lineChart);
                numbers.clear();
                initSeries();
                lineChart = createLineChart();
                gridpane.add(lineChart, 1, 1);
                cycle = 0;
                service.start();
                service1.start();
                stopGraphics.setText("Остановить график");
            }else {
                initSeries();
                lineChart = createLineChart();
                gridpane.add(lineChart, 1, 1);
                secondThread();
                service.start();
                firstThread();
                service1.start();

            }
            stopGraphics.setDisable(false);
            start_graphic.setDisable(true);
        });

        stopGraphics.setOnAction(actionEvent -> {
            if(service1.isRunning()){
                isAlive = true;
                service.cancel();
                service1.cancel();
                stopGraphics.setText("Возобновить");
                start_graphic.setDisable(false);
            }
            else {
                service.restart();
                service1.restart();
                stopGraphics.setText("Остановить график");
                start_graphic.setDisable(true);
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
                                    k = 0;
                                    q1T(s);
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
        yAxis.setPrefWidth(35);
        yAxis.setMinorTickCount(15);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(20);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override public String toString(Number object) {
                return String.format("%7.2f", object.floatValue());
            }
        });
    }

    private void MainWork() throws IOException {
        String[] word = new String[textinput1.getText().length()];
        tempStr = new StringBuffer(textinput1.getText());
        viewStr.delete(0, viewStr.length());
        clicked = true;
        String str = textinput1.getText();
        for (int i = 0; i < str.length(); i++) {
            word[i] = String.valueOf(str.charAt(i));
        }
        count1 = 0;
        q1T(word);
    }

    private void q1T(String[] words) throws IOException {
        if (k == words.length-1){
            finishT(words);
        }else {
            switch (words[k]) {
                case "a" -> {
                    if (clicked) {
                        tempStr.insert(k, "q1");
                        viewStr.insert(0, tempStr + "\n");
                        z += tempStr + "\n";
                        tempStr.delete(k, k + 2);
                    }
                    k++;
                    count++;
                    count1++;
                    q2T(words);
                }
                case "b", "c", "d" -> finishT(words);
            }
        }
    }

    private void q2T(String[] words) throws IOException {
        if (k == words.length-1){
            finishT(words);
        }else {
            switch (words[k]) {
                case "d" -> {
                    if (clicked) {
                        tempStr.insert(k, "q2");
                        viewStr.append(tempStr).append("\n");
                        z += tempStr + "\n";
                        tempStr.delete(k, k + 2);
                    }
                    k++;
                    count1++;
                    count++;
                    if(words[k].equals("a")) {
                        q1T(words);
                    } else {
                        q3T(words);
                    }
                }
                case "b", "c", "a" -> finishT(words);
            }
        }
    }

    private void q3T(String[] words) throws IOException {
        if (k == words.length-1){
            finishT(words);
        }else {
            switch (words[k]) {
                case "c" -> {
                    if(clicked){
                        tempStr.insert(k, "q3");
                        viewStr.append(tempStr).append("\n");
                        z += tempStr + "\n";
                        tempStr.delete(k, k+2);
                    }
                    k++;
                    count1++;
                    count++;
                    q4T(words);
                }
                case "b", "d", "a" -> finishT(words);
            }
        }
    }

    private void q4T(String[] words) throws IOException {
        if (k == words.length-1){
            count++;
            count1++;
            q5T(words); // b - последний символ
        }else {
            switch (words[k]) {
                case "b" -> {
                    if (clicked) {
                        tempStr.insert(k, "q4");
                        viewStr.append(tempStr).append("\n");
                        z += tempStr + "\n";
                        tempStr.delete(k, k + 2);
                    }
                    count++;
                    count1++;
                    k++;
                    q3T(words);
                }
                case "d", "c", "a" -> finishT(words);
            }
        }
    }

    private void q5T(String[] words) throws IOException {
        switch (words[k]) {
            case "b" -> {
                if (clicked) {
                    tempStr.insert(k, "q5");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                }
                count1++;
                count++;
                q6T(words);
            }
            case "d", "c", "a" -> finishT(words);
        }
    }

    private void q6T(String[] words) throws IOException {
        switch (words[k]) {
            case "b", "c" -> {
                if (clicked) {
                    tempStr.insert(k, "q6");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                }
                count1++;
                count++;
                k--;
                q6T(words);
            }
            case "d" -> {
                if (clicked) {
                    tempStr.insert(k, "q6");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                }
                count1++;
                count++;
                q7T(words);
            }
        }
    }

    private void q7T(String[] words) throws IOException {
        if(k == -1){
            count++;
            count1++;
            k++;
            q10T(words);
        }
        switch (words[k]) {
            case "a", "d" -> {
                words[k] = "x";
                if (clicked) {
                    tempStr.replace(k, k + 1, "x");
                    tempStr.insert(k, "q7");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                }
                count++;
                count1++;
                k++;
                q8T(words);
            }
            case "x", "y" -> {
                if (clicked) {
                    tempStr.insert(k, "q7");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                }
                count++;
                count1++;
                k--;
                q7T(words);
            }
        }
    }

    private void q8T(String[] words) throws IOException {
        if (k == words.length-1){
            words[k] = "y";
            if (clicked) {
                tempStr.replace(k, k + 1, "y");
                tempStr.insert(k, "q8");
                viewStr.append(tempStr).append("\n");
                z += tempStr + "\n";
                tempStr.delete(k, k + 2);
            }
            count++;
            count1++;
            k--;
            q11T(words); // Встретили пробел надо заменить на y
        }else {
            switch (words[k]) {
                case "b", "c" -> q9T(words);
                case "x", "y" -> {
                    if (clicked) {
                        tempStr.insert(k, "q8");
                        viewStr.append(tempStr).append("\n");
                        z += tempStr + "\n";
                        tempStr.delete(k, k + 2);
                    }
                    count++;
                    count1++;
                    k++;
                    q8T(words);
                }
            }
        }
    }

    private void q9T(String[] words) throws IOException {
        switch (words[k]){
            case "b", "c" -> {
                if (clicked) {
                    tempStr.replace(k, k + 1, "y");
                    tempStr.insert(k, "q9");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                }
                count++;
                count1++;
                words[k] = "y";
                k--;
                q7T(words);
            }
        }
    }

    private void q10T(String[] words) throws IOException {
        if (k == words.length-1){
            count++;
            count1++;
            if (clicked) {
                z += 1 + "\n";
                viewStr.append(1 + "\n");
                clicked = false;
                output_1.appendText(String.valueOf(viewStr));
                PrintStream printStream = new PrintStream("OneLenta.txt");
                printStream.print(z);
                k--;
            }
        }else {
            switch (words[k]) {
                case "c", "b" -> finishT(words);
                case "x", "y" ->{
                    if (clicked) {
                        tempStr.insert(k, "q10");
                        viewStr.append(tempStr).append("\n");
                        z += tempStr + "\n";
                        tempStr.delete(k, k + 3);
                    }
                    count1++;
                    count++;
                    k++;
                    q10T(words);
                }
            }
        }
    }

    private void q11T(String[] words) throws IOException {
        if (k == 0){
            count1++;
            count++;
            if (clicked){
                tempStr.insert(0, "1");
                z += tempStr + "\n";
                viewStr.append(tempStr);
                clicked = false;
                output_1.appendText(String.valueOf(viewStr));
                PrintStream printStream = new PrintStream("OneLenta.txt");
                printStream.print(z);
            };
        }
        else {
            switch (words[k]) {
                case "x", "y" -> {
                    if (clicked) {
                        tempStr.insert(k, "q11");
                        viewStr.append(tempStr).append("\n");
                        z += tempStr + "\n";
                        tempStr.delete(k, k + 3);
                    }
                    count++;
                    count1++;
                    k--;
                    q11T(words);
                }
                case "a", "d" -> finishT(words);
            }
        }
    }

    private void finishT(String[] words) throws IOException {
        if (clicked){
            if (k == words.length-1) {
                count1++;
                tempStr.insert(tempStr.length(), "0");
                viewStr.append(tempStr);
                z += tempStr + "\n";
                tempStr.delete(tempStr.length(), tempStr.length()+1);
                clicked = false;
                k = 0;
                output_1.appendText(String.valueOf(viewStr));
                PrintStream printStream = new PrintStream("OneLenta.txt");
                printStream.print(z);
            }
            else {
                tempStr.insert(k, "q12");
                z += tempStr + "\n";
                viewStr.append(tempStr).append("\n");
                tempStr.delete(k, k + 3);
                k++;
                count1++;
                finishT(words);
            }
        }
        else {
            if(k != words.length-1) {
                k++;
                count++;
                finishT(words);
            }
        }
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
}

class PermutationsWithRepetition {
    private String[] source;
    private int variationLength;

    public PermutationsWithRepetition(String[] source, int variationLength) {
        this.source = source;
        this.variationLength = variationLength;
    }

    public String[][] getVariations() {
        int srcLength = source.length;
        int permutations = (int) Math.pow(srcLength, variationLength);

        String[][] table = new String[permutations][variationLength];

        for (int i = 0; i < variationLength; i++) {
            int t2 = (int) Math.pow(srcLength, i);
            for (int p1 = 0; p1 < permutations;) {
                for (Object o : source) {
                    for (int p2 = 0; p2 < t2; p2++) {
                        table[p1][i] = (String) o;
                        p1++;
                    }
                }
            }
        }
        return table;
    }
}

