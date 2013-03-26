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

class Main {
    public static void main(String[] args) {

        def processedArgs = handleOptions(args)

        def dao = new MigrationTableObjectDAO(processedArgs)
        def xmlWriter = new MigrationXMLWriter(System.out, processedArgs)
        def xmlParser = new MigrationXMLParser(processedArgs)

        xmlWriter.writeXMLOutput(dao.selectAllFromIteratedTables(xmlParser.parseDBSchemaFile()))
    }

    /** Handles the options passed from the command line, prints warnings for
     *  missing elements or exits the program if required options are missing.
     *
     * @param args the args passed from the command line
     * @return nothing
     */
    private static def handleOptions(args) {

        if (args.length > 0 && (
        args[0].contains(cliOption(HELP_FLAG)) ||
                args[0].contains(cliOption(USAGE_FLAG)))) {
            usage()
            System.exit(1)
        }

        def processedArgs = [:]
        def expecting = null
        args.each { it ->
            if (expecting && !(isCliOption)) {
                processedArgs[expecting] = it
                return
            }

            if (it.startsWith(cliOption(DB_URL_FLAG))) {
                if (hasValue(it, DB_URL_FLAG)) {
                    processedArgs[DB_URL_FLAG] = valueOf(it, DB_URL_FLAG)
                } else {
                    expecting = DB_URL_FLAG
                }
            } else if (it.startsWith(cliOption(SCHEMA_FILE_FLAG))) {
                if (hasValue(it)) {
                    processedArgs[SCHEMA_FILE_FLAG] = valueOf(it, SCHEMA_FILE_FLAG)
                } else {
                    expecting = SCHEMA_FILE_FLAG
                }
            } else if (it.startsWith(cliOption(DB_USERNAME_FLAG))) {
                if (hasValue(it)) {
                    processedArgs[DB_USERNAME_FLAG] = valueOf(it, DB_USERNAME_FLAG)
                } else {
                    expecting = DB_USERNAME_FLAG
                }
            } else if (it.startsWith(cliOption(DB_PASSWORD_FLAG))) {
                if (hasValue(it)) {
                    processedArgs[DB_PASSWORD_FLAG] = valueOf(it, DB_PASSWORD_FLAG)
                } else {
                    expecting = DB_PASSWORD_FLAG
                }
            }
        }

        if (!(processedArgs[DB_URL_FLAG])) {
            sayErr "\nError: Database URL not set, please set this parameter using the ${cliOption(DB_URL_FLAG)} option"
            sayErr "(e.g.: ${cliOption(DB_URL_FLAG)}=jdbc:postgresql://localhost:5432/ssp)"
            usage()
            System.exit(1)
        }

        if (!(processedArgs[SCHEMA_FILE_FLAG])) {
            sayErr "\nWarning: Database schema file (${cliOption(SCHEMA_FILE_FLAG)}) not set, defaulting to included db schema file (${DEFAULT_SCHEMA_FILE_NAME})."
            processedArgs[SCHEMA_FILE_FLAG] = defaultSchema();
        }

        processedArgs
    }

    private static def hasValue(cliArg, cliOpt) {
        cliArg ==~ "${cliOption(cliOpt)}=.*"
    }

    private static def valueOf(cliArg, cliOpt) {
        (cliArg =~ "${cliOption(cliOpt)}=(.*)")[0][1]
    }

    private static def defaultSchema() {
        MigrationXMLParser.class.getResourceAsStream(DEFAULT_SCHEMA_FILE_NAME)
//        println MigrationXMLParser.class.getResource(DEFAULT_SCHEMA_FILE_NAME)
//        new File(MigrationXMLParser.class.getResource(DEFAULT_SCHEMA_FILE_NAME).toURI()).getAbsolutePath();
    }

    private static def sayErr(msg) {
        System.err.println(msg)
    }

    private static def usage() {
        sayErr(USAGE)
    }

}
