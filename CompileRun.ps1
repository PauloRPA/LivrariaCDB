$files = Get-ChildItem $PWD\src\*\*.java
javac -cp src/ -d 'target/' $files
java -cp 'target/' app.Application