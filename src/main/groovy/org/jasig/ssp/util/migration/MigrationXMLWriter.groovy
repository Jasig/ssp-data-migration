/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.ssp.util.migration

import static MigrationMeta.*

class MigrationXMLWriter {

    def previousTable
    def tab = '    '
    def changesetCounter = 1
    def result

    def MigrationXMLWriter(writer, opts) {
        result = writer
    }

    /** Writes the Liquibase XML for the given List&lt;MigrationTableObject&gt;
     *
     * @param tableObjects a List&lt;MigrationTableObject&gt; of table objects to write the changesets for
     * @return result a String containing the liquibase XML
     */
    def writeXMLOutput(def tableObjects) {
        result << XML_CHANGESET_HEADER
        appendDisableMSSQLForeignKey()

        tableObjects.each { table ->
            previousTable == table.tableName ? changesetCounter++ : (newTableSection(table))
            appendTableChangeset(table)
            previousTable = table.tableName
        }
        appendEnablePGForeignKey(previousTable)

        result << '\n<!-- ----------------------------------------- -->\n'
        result << "<!-- Footer -->\n"
        result << '<!-- ----------------------------------------- -->\n\n'

        appendEnableMSSQLForeignKey()
        result << XML_CHANGESET_FOOTER
    }

    /** Appends a complete changesets for a MigrationTableObject
     *
     * @param table a MigrationTableObject to write a changesets for
     * @return nothing (appends to class variable)
     */
    def appendTableChangeset(def table) {
        appendOpenChangeset(table, 'insert')
        appendOpenInsert(table)
        appendColumns(table)
        appendCloseInsert()
        appendCloseChangeset()
    }

    /** Iterates through the columns of a MigrationTableObject and
     * 	calls the write method to generate the liquibase logic to
     *  inject that data.
     *
     * @param table the MigrationTableObject that contains the column data
     * @return nothing (appends to class variable)
     */
    def appendColumns(def table) {
        table.columns.each { columnName, columnValue ->
            appendColumn(columnName, columnValue)
        }
    }

    /** Writes an individual column changeset with appropriate formatting
     * for the data value/type
     *
     * @param columnName the name of the column to write
     * @param columnValue the value of the column to write
     * @return nothing (appends to class variable)
     */
    def appendColumn(def columnName, def columnValue) {
        if (columnValue.toString().contains('<')) {
            result << "${tab}<column name=\"${columnName}\"><![CDATA[ \n${columnValue}\n]]>\n${tab}</column>\n"
        } else if (columnValue.toString() =~ UNIX_TIMESTAMP) {
            result << "${tab}<column name=\"${columnName}\" valueDate=\"${columnValue}\" />\n"
        } else {
            result << "${tab}<column name=\"${columnName}\" value=\"${columnValue}\" />\n"
        }
    }

    /** Opens a changeset tag for a table insertion
     *
     * @param table the table the changeset is for
     * @param action the action to take with the changeset (e.g. insert, delete, etc.)
     * @return
     */
    def appendOpenChangeset(def table, def action) {
        def change = "${tab}<changeSet id=\"transfer reference data - ${action ?: action} ${table?.tableName} ${changesetCounter}\" author=\"configuration_data_exporter\">\n"
        indentTab()
        result << change
    }

    /** Closes a changeset tag, decrements the tab
     *
     */
    def appendCloseChangeset() {
        decrementTab()
        result << "${tab}</changeSet>\n\n"
    }

    /** Opens a liquibase insertion tag
     *
     * @param table the table object that will be inserted into
     * @return
     */
    def appendOpenInsert(def table) {
        def change = "${tab}<insert tableName=\"${table.tableName}\">\n"
        indentTab()
        result << change
    }

    /** Closes an insert tag
     *
     * @return
     */
    def appendCloseInsert() {
        decrementTab()
        result << "${tab}</insert>\n"
    }

    /** performs the logic for the liquibase at the point where the table
     * 	being worked on is changed.
     *
     * @param table
     * @return
     */
    def newTableSection(def table) {
        changesetCounter = 1
        appendEnablePGForeignKey(previousTable)
        appendTableComment(table)
        appendDisablePGForeignKey(table)
        appendOpenChangeset(table, 'delete')
        appendDeleteTable(table)
        appendCloseChangeset()
    }

    def appendDeleteTable(def table) {
        result << "${tab}<sql> DELETE FROM ${table.tableName} </sql>\n"
    }

    def appendTableComment(def table) {
        result << '\n<!-- ----------------------------------------- -->\n'
        result << "<!-- Changeset section: ${table.tableName} -->\n"
        result << '<!-- ----------------------------------------- -->\n\n'
    }

    def appendDisableMSSQLForeignKey() {
        result << "${tab}<changeSet id=\"transfer reference data - disable foreign key MSSQL\" author=\"configuration_data_exporter\" dbms=\"mssql\">\n"
        indentTab()
        result << tab + '<sql>execute sp_msforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT all"</sql>\n'
        appendCloseChangeset()
    }

    def appendEnableMSSQLForeignKey() {
        result << "${tab}<changeSet id=\"transfer reference data - enable foreign key MSSQL\" author=\"configuration_data_exporter\" dbms=\"mssql\">\n"
        indentTab()
        result << tab + '<sql>execute sp_msforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all"</sql>\n'
        appendCloseChangeset()
    }

    def appendDisablePGForeignKey(table) {
        result << "${tab}<changeSet id=\"transfer reference data - disable ${table.tableName} foreign key Postgres\" author=\"configuration_data_exporter\" dbms=\"postgresql\">\n"
        indentTab()
        result << "${tab}<sql>ALTER TABLE ${table.tableName} DISABLE TRIGGER ALL</sql>\n"
        appendCloseChangeset()
    }

    def appendEnablePGForeignKey(tableName) {
        if (tableName != null) {
            result << "${tab}<changeSet id=\"transfer reference data - enable ${tableName} foreign key Postgres\" author=\"configuration_data_exporter\" dbms=\"postgresql\">\n"
            indentTab()
            result << "${tab}<sql>ALTER TABLE ${tableName} ENABLE TRIGGER ALL</sql>\n"
            appendCloseChangeset()
        }
    }

    def indentTab() {
        tab += tab
    }

    def decrementTab() {
        tab = tab[0..(tab.length()*-1)]
    }
}
