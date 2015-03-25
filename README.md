# SantanderRioFetcher

## Usage

    <jar name> [options]
  
    -d <dni used in the login>
    -p <password in numbers>
    -u <the username>

    -w <phantom js with full path>

    -c <config file>
    -C              # checks the configuration and exits with a message
  
    -f              # list invests>
    -m              # list last moves>     **
    
    ** default

    if <-d|-p|-u> is used, the program takes this argument instead of the configuration.
    if nothing is specified, the program aborts.

    If java couldn't find the path to phantomjs, this should be specified with the -w

## JAR generation

    mvn package

## JAR with dependencies generation

    mvn package  -Pbundle

The generated jar is left at target/SantanderRioFetcher-jar-with-dependencies.jar


# TODO

implement YML to keep de configuration (using CMD arguments is quite disgusting)

