import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.OutputStream;
import java.io.PrintStream;

public class ConsolePopup {
    private final Stage popupStage;
    private final TextArea textArea;

    public ConsolePopup() {
        popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Database Initialization Log");

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefWidth(400);
        textArea.setPrefHeight(350);

        VBox layout = new VBox(textArea);
        Scene scene = new Scene(layout, 600, 400);
        popupStage.setScene(scene);

        redirectSystemStreams();
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                updateTextArea(new String(b, off, len));
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void updateTextArea(String text) {
        Platform.runLater(() -> textArea.appendText(text));
    }

    public void show() {
        popupStage.show();
    }

    public void close() {
        popupStage.close();
    }
}