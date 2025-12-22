package com.adxam.warehouse.source;

import com.adxam.warehouse.entity.Appliance;
import java.io.*;

public abstract class AbstractCsvSource<A extends Appliance<?>> implements Source<A> {
    private final String csvName;
    protected BufferedReader reader;
    private String nextLine;

    protected AbstractCsvSource(String csvName) { this.csvName = csvName; }

    @Override
    public void init() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(csvName);
            if (is == null) throw new FileNotFoundException("Resource not found: " + csvName);
            reader = new BufferedReader(new InputStreamReader(is));
            reader.readLine(); 
        } catch (IOException e) {
            System.err.println("Error loading " + csvName + ": " + e.getMessage());
        }
    }

    @Override
    public boolean hasNext() {
        try {
            if (reader == null) return false;
            nextLine = reader.readLine();
            return nextLine != null;
        } catch (IOException e) { return false; }
    }

    protected String getLine() { return nextLine; }

    @Override
    public void close() throws IOException { if (reader != null) reader.close(); }

    @Override
    public String csvName() { return csvName; }
    
    @Override
    public abstract Source<A> copy();
}
