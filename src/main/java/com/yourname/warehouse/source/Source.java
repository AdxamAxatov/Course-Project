package com.yourname.warehouse.source;

import com.yourname.warehouse.entity.Appliance;
import java.io.Closeable;

public interface Source<A extends Appliance<?>> extends Closeable {
    void init();
    boolean hasNext();
    A next();
    String csvName();
    Source<A> copy();
}
