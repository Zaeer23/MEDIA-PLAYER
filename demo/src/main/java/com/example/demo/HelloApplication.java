package com.example.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.scene.control.Alert;


import java.io.*;
import java.util.*;


public class HelloApplication extends Application {
    private final List<File> audioFiles = new ArrayList<>();
    private int currentFileIndex = 0;
    public MediaPlayer mediaPlayer;
    private final VBox fileButtonsContainer = new VBox();
    private Button lastSelectedButton = null;
    private final Map<String, Double> playbackPositions = new HashMap<>();
    private Label seek;
    private Slider seekSlider;
    private Timeline adTimeline;




    public void load(String filepath) {
        if (mediaPlayer != null) {
            playbackPositions.put(audioFiles.get(currentFileIndex).getAbsolutePath(), mediaPlayer.getCurrentTime().toMillis());
            mediaPlayer.dispose();
        }
        File audioFile = new File(filepath);
        Media media = new Media(audioFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        Double lastPosition = playbackPositions.get(filepath);
        if (lastPosition != null) {
            mediaPlayer.seek(Duration.seconds(lastPosition));
        }

        if (mediaPlayer != null) {
            mediaPlayer.currentTimeProperty().addListener((_, _, newValue) -> {
                if (newValue != null && !seekSlider.isValueChanging()) {
                    seek.setText(formatTime(newValue.toSeconds()));
                    seekSlider.setValue(newValue.toSeconds() / mediaPlayer.getTotalDuration().toSeconds());
                }
            });
        }

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(200), _ -> {
            if (mediaPlayer != null && mediaPlayer.getTotalDuration().toSeconds() > 0) {
                double progress = mediaPlayer.getCurrentTime().toSeconds() / mediaPlayer.getTotalDuration().toSeconds();
                seekSlider.setValue(progress);
                seek.setText(formatTime(mediaPlayer.getCurrentTime().toSeconds()));
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private String formatTime(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, null)));

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        Menu theme = new Menu("Themes");

        MenuItem openFiles = new MenuItem("Add Audiobooks to Library");
        MenuItem otheme = new MenuItem("Default");

        MenuItem firstTheme = new MenuItem("Black and White");

        MenuItem stheme = new MenuItem("Copyrighted Mode");

        fileMenu.getItems().add(openFiles);
        menuBar.getMenus().add(fileMenu);
        root.setTop(menuBar);
        menuBar.setStyle("-fx-background-color:#e246ab;");
        fileMenu.setStyle("-fx-text-fill: #ffffff;-fx-font-family: 'Impact';");
        theme.setStyle("-fx-text-fill: #ffffff;-fx-font-family: 'Impact';");
        menuBar.getMenus().add(theme);
        theme.getItems().add(otheme);
        theme.getItems().add(firstTheme);
        theme.getItems().add(stheme);




        ToolBar controls = new ToolBar();
        Button playButton = new Button("▶");
        Button pauseButton = new Button("❚❚");
        Button stopButton = new Button("◼");
        Button nextButton = new Button("⏭");
        Button prevButton = new Button("⏮");

        for (Button button : List.of(playButton, pauseButton, stopButton, nextButton, prevButton)) {
            button.setStyle("-fx-background-color: #000000; -fx-text-fill: white;");
            button.setFont(Font.font("Impact", 12));

            button.setOnMousePressed(_ -> button.setStyle("-fx-background-color: white; -fx-text-fill: black;"));
            button.setOnMouseReleased(_ -> button.setStyle("-fx-background-color: #000000; -fx-text-fill: white;"));
        }



        controls.getItems().addAll(playButton, pauseButton, stopButton, prevButton, nextButton);
        root.setBottom(controls);
        controls.setStyle("-fx-background-color:#e246ab;");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3"));

        root.setCenter(fileButtonsContainer);

        Scene scene = new Scene(root, 375, 400);
        stage.getIcons().add(new Image("file:cgm9HVe.png"));
        stage.setTitle("yfitopS");
        Label volume = new Label();
        volume.setFont(Font.font("Impact", 12));
        volume.setText("Volume");
        seek = new Label("00:00:00");
        seek.setFont(Font.font("Impact", 12));
        controls.getItems().add(seek);

        stage.setScene(scene);
        stage.show();

        try (BufferedReader br = new BufferedReader(new FileReader("Audios.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                File file = new File(line);
                if (file.exists()) {
                    audioFiles.add(file);
                    addAudioButton(file);
                } else {
                    System.out.println("Warning: File not found - " + line);
                }
            }
        } catch (IOException ex) {
            System.out.println("Error reading Audios.txt: " + ex.getMessage());
        }

        openFiles.setOnAction(_ -> {
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                audioFiles.addAll(selectedFiles);

                for (File file : selectedFiles) {
                    addAudioButton(file);
                    appendFileToAudiosTxt(file.getAbsolutePath());
                }
            }
        });

        playButton.setOnAction(_ -> {
            if (mediaPlayer != null) {
                mediaPlayer.play();

            }
        });

        pauseButton.setOnAction(_ -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });
        stopButton.setOnAction(_ -> {
            if (mediaPlayer != null) {
                playbackPositions.put(audioFiles.get(currentFileIndex).getAbsolutePath(), mediaPlayer.getCurrentTime().toMillis());
                mediaPlayer.stop();
                mediaPlayer.seek(Duration.ZERO);
            }
        });
        nextButton.setOnAction(_ -> {
            if (!audioFiles.isEmpty() && mediaPlayer != null) {
                playbackPositions.put(audioFiles.get(currentFileIndex).getAbsolutePath(), mediaPlayer.getCurrentTime().toMillis());
                currentFileIndex = (currentFileIndex + 1) % audioFiles.size();
                try {
                    load(audioFiles.get(currentFileIndex).getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        prevButton.setOnAction(_ -> {
            if (!audioFiles.isEmpty() && mediaPlayer != null) {
                playbackPositions.put(audioFiles.get(currentFileIndex).getAbsolutePath(), mediaPlayer.getCurrentTime().toMillis());
                currentFileIndex = (currentFileIndex - 1 + audioFiles.size()) % audioFiles.size();
                try {
                    load(audioFiles.get(currentFileIndex).getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        seekSlider = new Slider(0, 1, 0);
        seekSlider.setPrefWidth(200);
        controls.getItems().add(seekSlider);

        seekSlider.valueProperty().addListener((_, _, newValue) -> {
            if (mediaPlayer != null) {
                double newTime = newValue.doubleValue() * mediaPlayer.getTotalDuration().toSeconds();
                seek.setText(formatTime(newTime));
            }
        });

        seekSlider.valueProperty().addListener((_, _, newValue) -> {
            if (mediaPlayer != null) {
                double newTime = newValue.doubleValue() * mediaPlayer.getTotalDuration().toSeconds();
                seek.setText(formatTime(newTime));
            }
        });

        seekSlider.setOnMouseReleased(_ -> {
            if (mediaPlayer != null) {
                double newTime = seekSlider.getValue() * mediaPlayer.getTotalDuration().toSeconds();
                mediaPlayer.seek(Duration.seconds(newTime));
            }
        });

        if (mediaPlayer != null) {
            mediaPlayer.currentTimeProperty().addListener((_, _, newValue) -> {
                if (newValue != null) {
                    seek.setText(formatTime(newValue.toSeconds()));
                }
            });
        }

        TextField timeInput = new TextField();
        timeInput.setPromptText("Enter time (hh:mm:ss)");
        timeInput.setFont(Font.font("Impact", 12));

        Button go = new Button("Go!");
        go.setOnAction(_ -> {
            String input = timeInput.getText();
            try {
                String[] parts = input.split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);

                double totalSeconds = hours * 3600 + minutes * 60 + seconds;
                if (mediaPlayer != null) {
                    mediaPlayer.seek(Duration.seconds(totalSeconds));
                }
            } catch (Exception ex) {
                System.out.println("Invalid time format. Use hh:mm:ss");
            }
        });

        controls.getItems().addAll(timeInput, go);


        controls.getItems().add(volume);
        Slider volumeSlider = new Slider(0, 1, 0.1);
        volumeSlider.setPrefWidth(150);

        controls.getItems().add(volumeSlider);
        volumeSlider.valueProperty().addListener((_, _, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue());
                volume.setText("Volume: " + String.format("%.2f", newValue.doubleValue() * 100));
            }
        });
        System.out.println("Make sure to unzip your files");

        stheme.setOnAction(_ -> {
            try {
                stage.getIcons().clear();
                controls.setStyle("-fx-background-color:#1DB954;");
                stage.getIcons().add(new Image("file:OIP (1).jfif"));
                stage.setTitle("Spotify");
                menuBar.setStyle("-fx-background-color:#1DB954;");

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("Spotify Notified.");
                alert.setContentText("You are now using copyrighted material, therefore we are going to show you ads to pay our lawyers.");
                alert.showAndWait();

                if (adTimeline != null) {
                    adTimeline.stop();
                }


                File adAudioFile = new File("wanna-break-from-the-ads.mp3");
                Media adMedia = new Media(adAudioFile.toURI().toString());
                MediaPlayer adPlayer = new MediaPlayer(adMedia);

                adTimeline = new Timeline(new KeyFrame(Duration.seconds(15), _ -> {
                    Alert adAlert = new Alert(Alert.AlertType.INFORMATION);
                    adAlert.setTitle("Advertisement");
                    adAlert.setHeaderText("Sponsored Message");
                    adAlert.setContentText("This is an ad paying for legal battles. Thanks for your patience!");

                    adAlert.show();


                    adPlayer.seek(Duration.ZERO);
                    adPlayer.play();


                    adAlert.setOnHidden(_ -> adPlayer.stop());
                }));

                adTimeline.setCycleCount(Timeline.INDEFINITE);
                adTimeline.play();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        otheme.setOnAction(_ -> {
            try {
                stage.getIcons().clear();
                stage.getIcons().add(new Image("file:cgm9HVe.png"));
                controls.setStyle("-fx-background-color:#e246ab;");
                menuBar.setStyle("-fx-background-color:#e246ab;");
                stage.setTitle("yfitopS");


                if (adTimeline != null) {
                    adTimeline.stop();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        firstTheme.setOnAction(_ -> {
            try {
                stage.getIcons().clear();
                controls.setStyle("-fx-background-color:#ffffff;");
                stage.getIcons().add(new Image("file:spotify-logo-transparent-vector-6.png"));
                stage.setTitle("Back In My Day");
                menuBar.setStyle("-fx-background-color:#ffffff;");


                if (adTimeline != null) {
                    adTimeline.stop();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });




    }


    private void addAudioButton(File file) {
        Button fileButton = new Button(file.getName());
        fileButton.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
        fileButton.setFont(Font.font("Impact", 12));

        fileButton.setOnAction(_ -> {
            try {
                currentFileIndex = audioFiles.indexOf(file);
                load(file.getAbsolutePath());

                if (lastSelectedButton != null) {
                    lastSelectedButton.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
                }
                fileButton.setStyle("-fx-background-color: #ffffff;");
                lastSelectedButton = fileButton;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        fileButtonsContainer.getChildren().add(fileButton);
        fileButton.setPrefWidth(500);
    }

    private void appendFileToAudiosTxt(String filePath) {
        Set<String> existingPaths = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader("Audios.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                existingPaths.add(line);
            }
        } catch (IOException ex) {
            System.out.println("Error reading Audios.txt: " + ex.getMessage());
        }


        if (!existingPaths.contains(filePath)) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("Audios.txt", true))) {
                bw.write(filePath);
                bw.newLine();
            } catch (IOException ex) {
                System.out.println("Error writing to Audios.txt: " + ex.getMessage());
            }
        } else {
            System.out.println("Duplicate file ignored: " + filePath);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
