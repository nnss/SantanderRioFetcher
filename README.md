# SantanderRioFetcher

## Ussage

    <jarname> [options]
  
    -d <dni used in the login> *
    -p <password in numbers>   *
    -u <the username>          *
  
    -f <list invests>         
    -m <list last moves>     **
    
    * obligatory
    ** default

## JAR generation

    mvn package

## JAR with dependencies generation

    mvn package  -Pbundle

The generated jar is left at target/SantanderRioFetcher-jar-with-dependencies.jar


# TODO

implement YML to keep de configuration (using CMD arguments is quite disgusting)

