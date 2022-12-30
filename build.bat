vcvars64 && ^
mvn clean package && ^
java -agentlib:native-image-agent=config-output-dir=./src/main/resources/META-INF/native-image -jar ./target/xktz-examiner-1.0-SNAPSHOT-jar-with-dependencies.jar && ^
mvn clean package && ^
native-image --verbose -jar ./target/xktz-examiner-1.0-SNAPSHOT-jar-with-dependencies.jar