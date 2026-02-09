package hu.webarticum.minibase.test.storage;

import hu.webarticum.minibase.storage.impl.simple.SimpleTable;
import hu.webarticum.minibase.storage.impl.simple.SimpleTable.SimpleTableBuilder;
import hu.webarticum.miniconnect.lang.ImmutableList;

class XXXXXXX {

    public SimpleTable xxxxxx(Iterable<? extends Iterable<?>> rawRows) {
        SimpleTableBuilder builder = SimpleTable.builder();
        //builder.columnDefinitions();
        for (Iterable<?> rawRow : rawRows) {
            builder.addRow(ImmutableList.fromIterable(rawRow));
        }
        return builder.build();
    }

}
