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

class MigrationMeta {
	
	public static def AUTHOR = 'data.migration.tool'

    public static final def DB_URL_FLAG = 'db-url'
    public static final def DB_USERNAME_FLAG = 'db-username'
    public static final def DB_PASSWORD_FLAG = 'db-password'
    public static final def USAGE_FLAG = 'usage'
    public static final def HELP_FLAG = 'help'
    public static final def SCHEMA_FILE_FLAG = 'schema-file'

    public static def cliOption(name) {
        "--${name}"
    }

    public static final def DEFAULT_SCHEMA_FILE_NAME = '/BaseConfigDBSchema.xml'
    public static final def DEFAULT_DESTINATION_FILE_NAME = 'SSP_Export_Liquibase_changeset.xml'
	
	public static final def DEFAULT_OUTPUT_ENCODING = "UTF-8"
	
	public static final NON_UTF_CHARS = [
		"\u2018":"'",	// ‘
		"\u2019":"'",	// ’
		"\u201c":"\"",	// “
		"\u201d":"\"",  // ”
		"\u2013":"-"	// – non UTF8 dash
		]
		
    public static final USAGE = """
----------------------------------
- Running the program
----------------------------------

    The app can be executed in at least three different ways:

        1) Gradle run command. This is the easiest way to run the app if you
           are tweaking the source code:

             %> gradle -q run -PcliArgs="<opts>"

           Note that <opts> parsing is naive so spaces in argument values will
           not work as expected.

        2) Exploded Gradle-assembled application

             %> gradle installApp
             %> ./build/install/ssp-data-migration/bin/ssp-data-migration <opts>

           Opts splitting is handled by your shell in this case, so whitespace
           in argument values should work as expected.

           Use this execution mechanism if you've received a zip or tar of the
           application.

        3) Without Gradle. Don't know why you'd want to do this as it is
           significantly more verbose and you'll need to do more legwork to
           make your JDBC driver visible:

            %>  groovy -cp \"src/main/groovy/:src/main/resources:/path/to/your/jdbc/driver\" \\
                src/main/groovy/org/jasig/ssp/util/migration/Main.groovy \\
                <opts>

    Output is an XML document on stdout. Redirect as appropriate.

    Options all have POSIX "long opt" format, i.e. --option-name=option-value.
    The "=" is optional and can be replaced with whitespace. E.g.:

    %> gradle -q run -PcliArgs="${cliOption(DB_URL_FLAG)}=jdbc:postgresql://localhost:5432/ssp" > migration.xml


----------------------------------
- Options
----------------------------------

    ${cliOption(DB_URL_FLAG)}    [Required] The full URL to the source database
    ${cliOption(DB_USERNAME_FLAG)}   [Optional] The username for the source database
    ${cliOption(DB_PASSWORD_FLAG)}   [Optional] The password for the source database
    
	${cliOption(SCHEMA_FILE_FLAG)}   [Optional] Specify the location for the DB Schema file - the file that specifies what data to export (default: March 2013 tables from last export job)
    ${cliOption(USAGE_FLAG)}, ${HELP_FLAG}    Prints this usage message and performs no processing
"""

    public static final def UNIX_TIMESTAMP = /[0-9]{1,4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}.[0-9]{1,2}/

}
