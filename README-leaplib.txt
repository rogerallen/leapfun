Over in ../leaplib, this is what I did...

======================================================================
* leaplib 0.8.1

cp ~/Downloads/LeapSDK+Examples_hotfix_public_mac_x64_0.8.1+6221/LeapSDK/lib/LeapJava.jar .

cat - > LeapJavaPom.xml << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>rogerallen</groupId>
  <artifactId>leaplib</artifactId>
  <version>0.8.1</version>
  <name>leaplib</name>
  <description>leap motion java lib</description>
</project>
EOF

mvn install:install-file -Dfile=LeapJava.jar -DpomFile=LeapJavaPom.xml

======================================================================
* leaplib-natives 0.8.1

mkdir -p native/macosx/x86_64

cp ~/Downloads/LeapSDK+Examples_hotfix_public_mac_x64_0.8.1+6221/LeapSDK/lib/libLeap.dylib native/macosx/x86_64
cp ~/Downloads/LeapSDK+Examples_hotfix_public_mac_x64_0.8.1+6221/LeapSDK/lib/libLeapJava.dylib native/macosx/x86_64

cat - > LeapJavaNativePom.xml << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>rogerallen</groupId>
  <artifactId>leaplib-natives</artifactId>
  <version>0.8.1</version>
  <name>leaplib-natives</name>
  <description>leap motion java native lib</description>
</project>
EOF

jar -cMf leap-java-natives-0.8.1.jar native

mvn install:install-file -Dfile=leap-java-natives-0.8.1.jar -DpomFile=LeapJavaNativePom.xml

======================================================================
* Test locally

Use lein -o run

======================================================================
* Uploading

cd /Users/rallen/.m2/repository/rogerallen/leaplib/0.8.1
scp leaplib-0.8.1.jar leaplib-0.8.1.pom clojars@clojars.org:
cd /Users/rallen/.m2/repository/rogerallen/leaplib-natives/0.8.1
scp leaplib-natives-0.8.1.jar leaplib-natives-0.8.1.pom clojars@clojars.org:

======================================================================
* Cleaning

rm -rf ~/.m2/repository/rogerallen/leaplib-natives
rm -rf ~/.m2/repository/rogerallen/leaplib
