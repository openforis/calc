package org.openforis.calc.rdb;

import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public final class KeyFactory extends AbstractKeys {
    /**
     * Factory method for unique keys
     */
    public static UniqueKey<Record> newUniqueKey(Table<Record> table, TableField<Record, ?>... fields) {
        return AbstractKeys.createUniqueKey(table, fields);
    }
}
