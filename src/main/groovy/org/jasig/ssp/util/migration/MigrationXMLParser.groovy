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

import static org.jasig.ssp.util.migration.MigrationMeta.*

class MigrationXMLParser {

    def filePath

    public MigrationXMLParser(opts) {
        if (opts[SCHEMA_FILE_FLAG]) {
            filePath = opts[SCHEMA_FILE_FLAG]
        } else {
            throw new IllegalArgumentException("Must specify a schema file");
        }
    }

    /** Parses the schema DB Schema XML so the program knows which
     * 	tables to pull data from.
     *
     * @return List&lt;MigrationTableObject&gt;
     */
    public def parseDBSchemaFile() {
        def tableObjects = []

        def tables = new XmlParser().parse(filePath)
        tables.each { table ->
            def tableObject = new MigrationTableObject()
            tableObject.tableName = table.@tableName
            tableObjects.add(tableObject)
        }
        tableObjects
    }
}
