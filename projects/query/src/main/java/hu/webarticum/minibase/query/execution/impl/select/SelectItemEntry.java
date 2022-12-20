package hu.webarticum.minibase.query.execution.impl.select;

import hu.webarticum.minibase.query.query.SelectQuery.SelectItem;
import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.miniconnect.record.translator.ValueTranslator;

class SelectItemEntry {

    final SelectItem selectItem;

    final ValueTranslator valueTranslator;

    final ColumnDefinition columnDefinition;
    
    
    SelectItemEntry(SelectItem selectItem, ValueTranslator valueTranslator, ColumnDefinition columnDefinition) {
        this.selectItem = selectItem;
        this.valueTranslator = valueTranslator;
        this.columnDefinition = columnDefinition;
    }

}