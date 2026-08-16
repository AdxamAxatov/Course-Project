package com.adxam.warehouse.source;

import com.adxam.warehouse.entity.Appliance;
import java.io.Closeable;
import java.io.IOException;

public interface Source<A extends Appliance<?>> extends Closeable {
    void init() throws IOException;
    boolean hasNext() throws IOException;
    A next();
    String csvName();
    Source<A> copy();
}
