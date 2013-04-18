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
import groovy.xml.*
import org.codehaus.groovy.runtime.*;

class MigrationXMLWriter {    
	
    def out
	def outputForeignKeys = true

    def MigrationXMLWriter(out, opts) {
        this.out = out as OutputStream
		if(opts[DISABLE_FKS_FLAG].toString().toLowerCase() == "false") {
			outputForeignKeys = false
		}
    }

    /** Writes the Liquibase XML for the given List&lt;MigrationTableObject&gt;
     *
     * @param tableObjects a List&lt;MigrationTableObject&gt; of table objects to write the changeSets for
     * @return result a String containing the liquibase XML
     */
    def writeXMLOutput(def tableObjects) {
		def writer = new StringWriter()
		def previousTable
		
		def markupBuilder = new StreamingMarkupBuilder()
		markupBuilder.encoding = DEFAULT_OUTPUT_ENCODING		
		def xml = markupBuilder.bind { builder ->			
			mkp.xmlDeclaration(version:'1.0', encoding: 'UTF-8')
			databaseChangeLog( xmlns : "http://www.liquibase.org/xml/ns/dbchangelog"
				, "xmlns:xsi" : "http://www.w3.org/2001/XMLSchema-instance"
				, "xsi:schemaLocation" : "http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-2.0.xsd http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd") 
			{ 
				if(outputForeignKeys) {
					appendDisableForeignKeys(builder, tableObjects)
				}	
				changeSet(id:"DMT - insert migrated data - ${getCurrentDate()}", author:AUTHOR) {
					tableObjects.each { table ->
						if(previousTable != table.tableName) {
							mkp.yieldUnescaped(generateNewSectionComment(table.tableName))
							appendTableDelete(builder, table)
						}
						appendTableInsert(builder, table)
						appendTableRollBack(builder, table)
						previousTable = table.tableName
					}	
				}	
				if(outputForeignKeys) {
					appendEnableForeignKeys(builder, tableObjects)
				}
			}
		}
		
		out << XmlUtil.serialize(xml)		
    }
	
	def appendTableDelete(xml, table) {
		xml.delete(tableName:"${table.tableName}") {}
	}
	
    /** Appends a complete changeSets for a MigrationTableObject
     *
     * @param table a MigrationTableObject to write a changeSets for
     * @return nothing (appends to class variable)
     */
    def appendTableInsert(xml, table) {
		xml.insert(tableName:"${table.tableName}") {
			table.columns.each { columnName, columnValue ->
				appendColumn(xml, columnName, columnValue)
			}
		}
    }
	
	/** Appends a rollback that deletes a spcific record from a table. 
	 * 
	 * @param xml the xml builder
	 * @param table the table to perform the rollback on
	 * @return void
	 */
	def appendTableRollBack(xml, table) {
		xml.rollback {
			xml.delete(tableName:"${table.tableName}") {
				xml.where("id=${table.columns['id']}")
			}			
		}
	}

    /** Writes an individual column changeSet with appropriate formatting
     * for the data value/type
     *
     * @param columnName the name of the column to write
     * @param columnValue the value of the column to write
     * @return nothing (appends to class variable)
     */
    def appendColumn(xml, columnName, columnValue) {
		columnValue = UTFTransform(columnValue)	
			
        if (columnValue.toString().contains('<')) {
           xml.column(name:"${columnName}", "<![CDATA[ ${columnValue} ]]>")
        } else if(columnValue != null) {
            xml.column(name:"${columnName}", value:"${columnValue}")
        }
    }
	
	/** Appends a statement to disable foreign keys
	 * 
	 * @param xml the xml builder
	 * @param tables the tables to disable the FK constraints on (all tables)
	 * @return void
	 */
	def appendDisableForeignKeys(xml, tables) {
		xml.changeSet(id:"DMT - disable FK Constraints MSSQL - ${getCurrentDate()}", author:AUTHOR, dbms:'mssql') {
			xml.sql("execute sp_msforeachtable \"ALTER TABLE ? NOCHECK CONSTRAINT all\"")
		}
		def previousTable
		xml.changeSet(id:"DMT - disable FK Constraints Postgresql - ${getCurrentDate()}", author:AUTHOR, dbms:'postgresql') {
			tables.each { table ->
				if(previousTable != table.tableName) {
					xml.sql("ALTER TABLE ${table.tableName} DISABLE TRIGGER ALL")
				}
				previousTable = table.tableName
			}
		}
	}
	
	/** Appends a statement to enable foreign keys
	 *
	 * @param xml the xml builder
	 * @param tables the tables to enable the FK constraints on (all tables)
	 * @return void
	 */
	def appendEnableForeignKeys(xml, tables) {
		xml.changeSet(id:"DMT - enable FK Constraints MSSQL - ${getCurrentDate()}", author:AUTHOR, dbms:'mssql') {
			xml.sql("execute sp_msforeachtable \"ALTER TABLE ? WITH CHECK CHECK CONSTRAINT all\"")
		}
		def previousTable
		xml.changeSet(id:"DMT - enable FK Constraints Postgresql - ${getCurrentDate()}", author:AUTHOR, dbms:'postgresql') {
			tables.each { table ->	
				if(previousTable != table.tableName) {			
					xml.sql("ALTER TABLE ${table.tableName} ENABLE TRIGGER ALL")
				}
				previousTable = table.tableName
			}
		}
	}
	
	def generateNewSectionComment(tableName) {
		return """\n\n<!-- ################################### -->
				|<!-- Changeset section: ${tableName} -->
				|<!-- ################################### -->\n""".stripMargin()
	}
	
	/** Replaces non-UTF 8 characters with UTF-8 friendly characters
	 * 
	 * @param value the value to check and transform
	 * @return the transformed string (or the original string if it did not contain any non-UTF-8 characters)
	 */
	def UTFTransform(value) {
		
		if(value.getClass() != String &&
		   value.getClass() != GString ) {
			return value
		}
		
		NON_UTF_CHARS.each { nonUTFChar, UTFChar -> 
			if(value.contains(nonUTFChar)) {
				value = value.replace(nonUTFChar, UTFChar)
			}		
		}
		return value
	}
	
	private def getCurrentDate() {
		DateGroovyMethods.format(new Date(), 'MM/dd/yy HH:mm')
	}
}
