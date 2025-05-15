package com.navigator14;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.testfx.assertions.api.Assertions.assertThat;

public class HomeUITest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        new homeUI().start(stage);
    }

    @Test
    public void testFillFields() {
        TextField originField = lookup("#originField").query();
        clickOn(originField).write("Roma Termini");

        TextField destinationField = lookup("#destinationField").query();
        clickOn(destinationField).write("Vatican");

        TextField timeField = lookup("#timeField").query();
        clickOn(timeField).write("09:30");

        DatePicker dateField = lookup("#dateField").query();
        interact(() -> dateField.setValue(LocalDate.of(2024, 5, 12)));

        // Assertions to verify the fields are filled correctly
        assertThat(originField.getText()).isEqualTo("Roma Termini");
        assertThat(destinationField.getText()).isEqualTo("Vatican");
        assertThat(timeField.getText()).isEqualTo("09:30");
        assertThat(dateField.getValue()).isEqualTo(LocalDate.of(2024, 5, 12));

        WaitForAsyncUtils.sleep(20, TimeUnit.SECONDS);

        assertThat(lookup(".text").queryText().getText()).doesNotContain("No route found");
    }
}