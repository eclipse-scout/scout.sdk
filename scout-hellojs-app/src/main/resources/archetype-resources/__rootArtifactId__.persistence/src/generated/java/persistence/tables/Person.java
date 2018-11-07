#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
/*
 * This file is generated by jOOQ.
 */
package ${package}.persistence.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import ${package}.persistence.Keys;
import ${package}.persistence.tables.records.PersonRecord;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Person extends TableImpl<PersonRecord> {

    private static final long serialVersionUID = -956693580;

    /**
     * The reference instance of <code>Schema.person</code>
     */
    public static final Person PERSON = new Person();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PersonRecord> getRecordType() {
        return PersonRecord.class;
    }

    /**
     * The column <code>Schema.person.person_id</code>.
     */
    public final TableField<PersonRecord, String> PERSON_ID = createField("person_id", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>Schema.person.first_name</code>.
     */
    public final TableField<PersonRecord, String> FIRST_NAME = createField("first_name", org.jooq.impl.SQLDataType.VARCHAR(200), this, "");

    /**
     * The column <code>Schema.person.last_name</code>.
     */
    public final TableField<PersonRecord, String> LAST_NAME = createField("last_name", org.jooq.impl.SQLDataType.VARCHAR(200).nullable(false), this, "");

    /**
     * The column <code>Schema.person.salary</code>.
     */
    public final TableField<PersonRecord, Integer> SALARY = createField("salary", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>Schema.person.external</code>.
     */
    public final TableField<PersonRecord, Boolean> EXTERNAL = createField("external", org.jooq.impl.SQLDataType.BOOLEAN, this, "");

    /**
     * Create a <code>Schema.person</code> table reference
     */
    public Person() {
        this(DSL.name("person"), null);
    }

    /**
     * Create an aliased <code>Schema.person</code> table reference
     */
    public Person(String alias) {
        this(DSL.name(alias), PERSON);
    }

    /**
     * Create an aliased <code>Schema.person</code> table reference
     */
    public Person(Name alias) {
        this(alias, PERSON);
    }

    private Person(Name alias, Table<PersonRecord> aliased) {
        this(alias, aliased, null);
    }

    private Person(Name alias, Table<PersonRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Person(Table<O> child, ForeignKey<O, PersonRecord> key) {
        super(child, key, PERSON);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
      return ${package}.persistence.Schema.SCHEMA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<PersonRecord> getPrimaryKey() {
        return Keys.PERSON_PK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<PersonRecord>> getKeys() {
        return Arrays.<UniqueKey<PersonRecord>>asList(Keys.PERSON_PK);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person as(String alias) {
        return new Person(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Person as(Name alias) {
        return new Person(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Person rename(String name) {
        return new Person(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Person rename(Name name) {
        return new Person(name, null);
    }
}
