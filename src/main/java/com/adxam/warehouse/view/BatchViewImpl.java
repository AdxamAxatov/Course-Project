package com.adxam.warehouse.view;

import com.adxam.warehouse.controller.Controller;
import com.adxam.warehouse.controller.RequestImpl;
import com.adxam.warehouse.controller.Response;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class BatchViewImpl extends AbstractView {

    private static final String COMMENT_PREFIX = "#";

    private final String scriptName;

    public BatchViewImpl(String scriptName) {
        this.scriptName = scriptName;
    }

    @Override
    public void start() {
        Controller controller = controller();

        try (BufferedReader reader = openScript()) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith(COMMENT_PREFIX)) {
                    continue;
                }

                System.out.println("> " + line);

                Response response = controller.execute(new RequestImpl(line));
                System.out.println(response.responseString());

                if (response.isOut()) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read batch script: " + scriptName, e);
        }
    }

    private BufferedReader openScript() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(scriptName);
        if (is == null) {
            throw new FileNotFoundException("Batch script not found: " + scriptName);
        }
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }
}
