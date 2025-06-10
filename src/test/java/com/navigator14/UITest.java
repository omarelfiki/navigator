package com.navigator14;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import java.util.concurrent.TimeUnit;

import static org.testfx.assertions.api.Assertions.assertThat;

public class UITest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        new HomeUI().start(stage);
    }

    @Test
    public void test1() {
        TextField originField = lookup("#originField").query();
        clickOn(originField).write("Roma Termini");

        TextField destinationField = lookup("#destinationField").query();
        clickOn(destinationField).write("Vatican");

        TextField timeField = lookup("#timeField").query();
        clickOn(timeField).write("09:30");

        Button goButton = lookup("#goButton").query();
        clickOn(goButton);

        // Assertions to verify the fields are filled correctly
        assertThat(originField.getText()).isEqualTo("Roma Termini");
        assertThat(destinationField.getText()).isEqualTo("Vatican");
        assertThat(timeField.getText()).isEqualTo("09:30");
        assertThat(lookup(".text").queryText().getText()).doesNotContain("No route found");

        WaitForAsyncUtils.sleep(15, TimeUnit.SECONDS);


    }

    @Test
    public void test2(){
        TextField originField = lookup("#originField").query();
        clickOn(originField).write("Roma Termini");

        TextField destinationField = lookup("#destinationField").query();
        clickOn(destinationField).write("Colloseum");

        TextField timeField = lookup("#timeField").query();
        clickOn(timeField).write("09:30");

        Button goButton = lookup("#goButton").query();
        clickOn(goButton);

        // Assertions to verify the fields are filled correctly
        assertThat(originField.getText()).isEqualTo("Roma Termini");
        assertThat(destinationField.getText()).isEqualTo("Colloseum");
        assertThat(timeField.getText()).isEqualTo("09:30");

        WaitForAsyncUtils.sleep(15, TimeUnit.SECONDS);

        assertThat(lookup(".text").queryText().getText()).doesNotContain("No route found");
    }

    @Test
    public void test3(){
        TextField originField = lookup("#originField").query();
        clickOn(originField).write("41.904, 12.5004");

        TextField destinationField = lookup("#destinationField").query();
        clickOn(destinationField).write("41.8791, 12.5221");

        TextField timeField = lookup("#timeField").query();
        clickOn(timeField).write("09:30");

        Button goButton = lookup("#goButton").query();
        clickOn(goButton);

        // Assertions to verify the fields are filled correctly
        assertThat(originField.getText()).isEqualTo("41.904, 12.5004");
        assertThat(destinationField.getText()).isEqualTo("41.8791, 12.5221");
        assertThat(timeField.getText()).isEqualTo("09:30");

        WaitForAsyncUtils.sleep(20, TimeUnit.SECONDS);

        assertThat(lookup(".text").queryText().getText()).doesNotContain("No route found");
    }

    @Test
    public void HeatMapTest() {
        Pane togglePane = lookup("#togglePane").query();
        clickOn(togglePane);

        TextField originField = lookup("#originField").query();
        clickOn(originField).write("Rome");

        Button searchButton = lookup("#searchButton").query();
        clickOn(searchButton);

        WaitForAsyncUtils.sleep(60, TimeUnit.SECONDS);

    }
}