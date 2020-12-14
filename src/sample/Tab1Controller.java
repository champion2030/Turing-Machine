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
    boolean isAlive = false;
    private int k, count, k1;
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
                                    k1 = 0;
                                    q1S(s);
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


    private void MainWork() throws IOException {
        String[] word = new String[textinput1.getText().length()];
        tempStr = new StringBuffer(textinput1.getText());
        viewStr.delete(0, viewStr.length());
        String str = textinput1.getText();
        for (int i = 0; i < str.length(); i++) {
            word[i] = String.valueOf(str.charAt(i));
        }
        q1T(word);
    }

    private void q1T(String[] words) throws IOException {
        if (k == words.length){
            tempStr.insert(k, "q1");
            viewStr.append(tempStr).append("\n");
            z += tempStr + "\n";
            tempStr.delete(k, k + 2);
            finishT(words);
        }else {
            switch (words[k]) {
                case "a" -> {
                    tempStr.insert(k, "q1");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                    k++;
                    q2T(words);
                }
                case "b", "c", "d" -> finishT(words);
            }
        }
    }

    private void q2T(String[] words) throws IOException {
        if (k == words.length){
            tempStr.insert(k, "q2");
            viewStr.append(tempStr).append("\n");
            z += tempStr + "\n";
            tempStr.delete(k, k + 2);
            finishT(words);
        }else {
            switch (words[k]) {
                case "d" -> {
                    tempStr.insert(k, "q2");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                    k++;
                    if (k == words.length){
                        tempStr.insert(k, "q2");
                        viewStr.append(tempStr).append("\n");
                        z += tempStr + "\n";
                        tempStr.delete(k, k + 2);
                        finishT(words);
                    }
                    else if(words[k].equals("a")){
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
            tempStr.insert(k, "q3");
            viewStr.append(tempStr).append("\n");
            z += tempStr + "\n";
            tempStr.delete(k, k + 2);
            finishT(words);
        }else {
            switch (words[k]) {
                case "c" -> {
                    tempStr.insert(k, "q3");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k+2);
                    k++;
                    q4T(words);
                }
                case "b", "d", "a" -> finishT(words);
            }
        }
    }

    private void q4T(String[] words) throws IOException {
        if (k == words.length-1){
            q5T(words); // b - последний символ
        }else {
            switch (words[k]) {
                case "b" -> {
                    tempStr.insert(k, "q4");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
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
                tempStr.insert(k, "q5");
                viewStr.append(tempStr).append("\n");
                z += tempStr + "\n";
                tempStr.delete(k, k + 2);
                q6T(words);
            }
            case "d", "c", "a" -> finishT(words);
        }
    }

    private void q6T(String[] words) throws IOException {
        switch (words[k]) {
            case "b", "c" -> {
                tempStr.insert(k, "q6");
                viewStr.append(tempStr).append("\n");
                z += tempStr + "\n";
                tempStr.delete(k, k + 2);
                k--;
                q6T(words);
            }
            case "d" -> {
                tempStr.insert(k, "q6");
                viewStr.append(tempStr).append("\n");
                z += tempStr + "\n";
                tempStr.delete(k, k + 2);
                q7T(words);
            }
        }
    }

    private void q7T(String[] words) throws IOException {
        if(k == -1){
            k++;
            q10T(words);
        }else {
            switch (words[k]) {
                case "a", "d" -> {
                    words[k] = "x";
                    tempStr.replace(k, k + 1, "x");
                    tempStr.insert(k, "q7");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                    k++;
                    q8T(words);
                }
                case "x", "y" -> {
                    tempStr.insert(k, "q7");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                    k--;
                    q7T(words);
                }
            }
        }
    }

    private void q8T(String[] words) throws IOException {
        if (k == words.length-1){
            words[k] = "y";
            tempStr.replace(k, k + 1, "y");
            tempStr.insert(k, "q8");
            viewStr.append(tempStr).append("\n");
            z += tempStr + "\n";
            tempStr.delete(k, k + 2);
            k--;
            q11T(words); // Встретили пробел надо заменить на y
        }else {
            switch (words[k]) {
                case "b", "c" -> q9T(words);
                case "x", "y" -> {
                    tempStr.insert(k, "q8");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 2);
                    k++;
                    q8T(words);
                }
            }
        }
    }

    private void q9T(String[] words) throws IOException {
        switch (words[k]){
            case "b", "c" -> {
                tempStr.replace(k, k + 1, "y");
                tempStr.insert(k, "q9");
                viewStr.append(tempStr).append("\n");
                z += tempStr + "\n";
                tempStr.delete(k, k + 2);
                words[k] = "y";
                k--;
                q7T(words);
            }
        }
    }

    private void q10T(String[] words) throws IOException {
        if (k == words.length-1){
            z += 1 + "\n";
            viewStr.append(1 + "\n");
            output_1.appendText(String.valueOf(viewStr));
            PrintStream printStream = new PrintStream("OneLenta.txt");
            printStream.print(z);
        }else {
            switch (words[k]) {
                case "c", "b" -> finishT(words);
                case "x", "y" ->{
                    tempStr.insert(k, "q10");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 3);
                    k++;
                    q10T(words);
                }
            }
        }
    }

    private void q11T(String[] words) throws IOException {
        if (k == 0){
            tempStr.insert(0, "1");
            z += tempStr + "\n";
            viewStr.append(tempStr);
            output_1.appendText(String.valueOf(viewStr));
            PrintStream printStream = new PrintStream("OneLenta.txt");
            printStream.print(z);
        }
        else {
            switch (words[k]) {
                case "x", "y" -> {
                    tempStr.insert(k, "q11");
                    viewStr.append(tempStr).append("\n");
                    z += tempStr + "\n";
                    tempStr.delete(k, k + 3);
                    k--;
                    q11T(words);
                }
                case "a", "d" -> finishT(words);
            }
        }
    }

    private void finishT(String[] words) throws IOException {
        if (k == words.length) {
            tempStr.insert(k, "q12");
            z += tempStr + "\n";
            viewStr.append(tempStr).append("\n");
            tempStr.delete(k, k + 3);
            tempStr.insert(tempStr.length(), "0");
            viewStr.append(tempStr);
            z += tempStr + "\n";
            tempStr.delete(tempStr.length(), tempStr.length()+1);
            k = 0;
            output_1.appendText(String.valueOf(viewStr));
            PrintStream printStream = new PrintStream("OneLenta.txt");
            printStream.print(z);
        }
        else {
            while (k != words.length){
                tempStr.insert(k, "q12");
                z += tempStr + "\n";
                viewStr.append(tempStr).append("\n");
                tempStr.delete(k, k + 3);
                k++;
            }
            finishT(words);
        }

    }



    private void q1S(String[] words) {
        if (k1 == words.length){
            count++;
            finishS(words);
        }else {
            switch (words[k]) {
                case "a" -> {
                    k1++;
                    count++;
                    q2S(words);
                }
                case "b", "c", "d" -> finishS(words);
            }
        }
    }

    private void q2S(String[] words) {
        if (k1 == words.length){
            count++;
            finishS(words);
        }else {
            switch (words[k1]) {
                case "d" -> {
                    k1++;
                    count++;
                    if (k1 == words.length){
                        count++;
                        finishS(words);
                    }
                    else if(words[k1].equals("a")){
                        q1S(words);
                    } else {
                        q3S(words);
                    }
                }
                case "b", "c", "a" -> finishS(words);
            }
        }
    }

    private void q3S(String[] words) {
        if (k1 == words.length-1){
            count++;
            finishS(words);
        }else {
            switch (words[k1]) {
                case "c" -> {
                    k1++;
                    count++;
                    q4S(words);
                }
                case "b", "d", "a" -> finishS(words);
            }
        }
    }

    private void q4S(String[] words) {
        if (k1 == words.length-1){
            q5S(words); // b - последний символ
        }else {
            switch (words[k1]) {
                case "b" -> {
                    count++;
                    k1++;
                    q3S(words);
                }
                case "d", "c", "a" -> finishS(words);
            }
        }
    }

    private void q5S(String[] words) {
        switch (words[k1]) {
            case "b" -> {
                count++;
                q6S(words);
            }
            case "d", "c", "a" -> finishS(words);
        }
    }

    private void q6S(String[] words) {
        switch (words[k1]) {
            case "b", "c" -> {
                count++;
                k1--;
                q6S(words);
            }
            case "d" -> {
                count++;
                q7S(words);
            }
        }
    }

    private void q7S(String[] words){
        if(k1 == -1){
            count++;
            k1++;
            q10S(words);
        }else {
            switch (words[k]) {
                case "a", "d" -> {
                    words[k1] = "x";
                    count++;
                    k1++;
                    q8S(words);
                }
                case "x", "y" -> {
                    count++;
                    k1--;
                    q7S(words);
                }
            }
        }
    }

    private void q8S(String[] words) {
        if (k1 == words.length-1){
            words[k1] = "y";
            count++;
            k1--;
            q11S(words); // Встретили пробел надо заменить на y
        }else {
            switch (words[k1]) {
                case "b", "c" -> q9S(words);
                case "x", "y" -> {
                    count++;
                    k1++;
                    q8S(words);
                }
            }
        }
    }

    private void q9S(String[] words){
        switch (words[k1]){
            case "b", "c" -> {
                count++;
                words[k1] = "y";
                k1--;
                q7S(words);
            }
        }
    }

    private void q10S(String[] words){
        if (k1 == words.length-1){
            count++;
        }else {
            switch (words[k1]) {
                case "c", "b" -> finishS(words);
                case "x", "y" ->{
                    count++;
                    k1++;
                    q10S(words);
                }
            }
        }
    }

    private void q11S(String[] words) {
        if (k1 == 0){
            count++;
            //Закончили
        }
        else {
            switch (words[k1]) {
                case "x", "y" -> {
                    count++;
                    k1--;
                    q11S(words);
                }
                case "a", "d" -> finishS(words);
            }
        }
    }

    private void finishS(String[] words){
        if (k1 == words.length){
            count += 2;
        }
        else {
            while (k1 != words.length) {
                k1++;
                count++;
            }
            finishS(words);
        }
    }

}


