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
import groovy.sql.Sql

class MigrationTableObjectDAO {

    def final postgresDriver = 'org.postgresql.Driver'
    def final jtdsDriver = 'net.sourceforge.jtds.jdbc.Driver'

    def password
    def user
    def url
    def driver

    public MigrationTableObjectDAO(opts) {
		handleOptions(opts)
        if (!(opts[DB_URL_FLAG])) {
            throw new IllegalArgumentException("Must specify a database URL")
        }
        url = opts[DB_URL_FLAG]

        if (url.contains('postgresql')) {
            driver = postgresDriver
        } else if (url.contains('jtds')) {
            driver = jtdsDriver
        } else {
            throw new IllegalArgumentException("Unable to detect target database type. Expect database url to contain either \"postgresql\" or \"jtds\"")
        }
    }

    def selectAllFromIteratedTables(def tables) {
        def tableObjects = []
        def sql = Sql.newInstance(url, user, password, driver)
        tables.each { table ->
            sql.eachRow("SELECT * FROM ${Sql.expand(table.tableName)}") { result ->
                def tableObject = new MigrationTableObject()
                tableObject.tableName = table.tableName
                tableObject.columns = result.toRowResult()
                tableObjects.add(tableObject);
            }
        }
        sql.close()
        tableObjects
    }
	
	def handleOptions(opts) {
		user = opts[DB_USERNAME_FLAG]?: 'sspadmin'
		password = opts[DB_PASSWORD_FLAG]?: 'sspadmin'
		if(opts[DB_PASSWORD_FLAG] == null && System.console() != null) {	
			password = System.console().readLine("Enter the source database password: ")
		}
	}	
}
