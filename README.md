ssp-data-migration
==================

Tool for moving SSP data from one data to another by representing that data as
Liquibase changsets.

This tool will not touch your target database at all and interactions with
the source database are strictly read-only.

There are no special provisions for locking the source database nor guaranteeing
that it is in a read-only state. So you are probably best served to run this
tool while the source application is not serving requests.

High-level workflow:

1. Configure a schema file describing the tables you want to export from the
  source database. The project includes a sample for SSP v1.2.0 at
  `src/main/groovy/resources/BaseConfigDBSchema` that would be suitable for
  migrating reference data types typically edited during a formal SSP
  training session.
2. Run the tool, configured with that schema file and coordinates
  for your source database, redirecting stdout to an XML file.
3. Take the resulting XML file and execute it against your target database using
  Liquibase tools. The simplest way to accomplish the latter is with the
  Liquibase [command line](http://www.liquibase.org/manual/command_line).

This is, of course, not the only way to move data from one database to another.
PostgresSQL in particular ships with standard export/import tools which
*should* be fully capable of migrating some or all SSP data between databases,
thereby obviating this tool. But the situation has historically been more
challenging with SqlServer, which has difficulty scripting the SSP database.
Direct database-to-database export/import jobs have worked better, but these
are often not allowed by network security policies.

Pre-requisites
==============

At a minimum you'll need a Java JDK install. This process can vary widely from
platform to platform, so you're on you're own for that one. This program will
theoretically work on JDK 1.5+, but you'll be best served by 1.6+.

Depending on how you plan to run the application you can either install Groovy
or Gradle. Gradle ships with an embedded Groovy, so you can technically avoid a
standalone Groovy install if you'd like. If you just want to clone this repo and
run the code with a minimum of fuss, Gradle is the way to go.

Gradle [download](http://www.gradle.org/downloads). The latest binary distro
should be fine. We've most recently tested with 1.4.

Gradle [install instructions](http://www.gradle.org/docs/current/userguide/installation.html).
The rest of this doc assumes you've added Gradle's `bin` directory to your PATH.

Groovy [download](http://groovy.codehaus.org/Download). The latest binary distro should be fine. The app has been
tested with 1.8.6 and that's the version Gradle will bundle with the binary
output.

Groovy [install instructions](http://groovy.codehaus.org/Installing+Groovy).
The rest of this doc assumes you've added Groovy's `bin` directory to your PATH.

Running the Program
===================

The app can be executed in at least three different ways:

1. Gradle run command. This is the easiest way to run the app if you are
tweaking the source code:

    `%> gradle -q run -PcliArgs="<opts>"`

  Note that `<opts>` parsing is naive so spaces in argument values will not work
  as expected.

2. Exploded Gradle-assembled application

    `%> gradle installApp`
    `%> ./build/install/ssp-data-migration/bin/ssp-data-migration <opts>`

  `<opts>` splitting is handled by your shell in this case, so whitespace in
  argument values should work as expected.

  Use this execution mechanism if you've received a zip or tar of the
  application.

3. Without Gradle. Don't know why you'd want to do this as it is significantly
more verbose and you'll need to do more legwork to make your JDBC driver
visible:

    ```
    %> groovy -cp "src/main/groovy/:src/main/resources:/path/to/your/jdbc/driver" \
    src/main/groovy/org/jasig/ssp/util/migration/Main.groovy \
    <opts>
    ```

Output is an XML document on stdout. Redirect as appropriate.

Options all have POSIX "long opt" format, i.e. `--option-name=option-value`.
The "=" is optional and can be replaced with whitespace. E.g.:

    %> gradle -q run -PcliArgs="--db-url=jdbc:postgresql://localhost:5432/ssp" > migration.xml

Options
=======

`--db-url`    [Required] The full URL to the source database

`--db-username`   [Optional] The username for the source database

`--db-password`   [Optional] The password for the source database

`--schema-file`   [Optional] Specify the location for the DB Schema file - the file that specifies what data to export (default: March 2013 tables from last export job)

`--usage`, `--help`    Prints the usage message and performs no processing