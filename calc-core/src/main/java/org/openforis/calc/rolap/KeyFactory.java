package org.openforis.calc.rolap;

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
class KeyFactory extends AbstractKeys {
    /**
     * Factory method for unique keys
     */
    public static UniqueKey<Record> createUniqueKey(Table<Record> table, TableField<Record, ?>... fields) {
        return AbstractKeys.createUniqueKey(table, fields);
    }
}
