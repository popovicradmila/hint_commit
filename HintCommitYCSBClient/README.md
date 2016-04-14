Compile:

    # get the ycsb adapter:
    wget https://github.com/brianfrankcooper/YCSB/archive/0.8.0.zip
    unzip 0.8.0.zip
    cd YCSB-0.8.0/core
    mvn package

    # now we have the .jar
    cp target/core-0.8.0.jar ~/HintCommitYCSBClient/target/dependency/
