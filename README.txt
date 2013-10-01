
Building Glassfish Chess Sample
-------------------------------

 1) If running unit tests, you MUST start the database server for 'ChessServer' by typing:

   >> asadmin start-database

 2) Then execute:

   >> mvn clean install

 3) Finally, deploy your app and point your browser to http://localhost:8080/chess

   >> asadmin deploy ChessServer/target/ChessServer-1.0.war

