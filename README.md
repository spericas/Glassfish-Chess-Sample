javaone2013-chess
=================

Chess Demo shown at JavaOne 2013 Technical Keynote

1. If running unit tests, you MUST start the database server for 'ChessServer' by typing:

   <pre>asadmin start-database</pre>

2. Then execute:

   <pre>mvn clean install</pre>

3. Finally, deploy your app and point your browser to http://localhost:8080/chess

   <pre>asadmin deploy ChessServer/target/ChessServer-1.0.war</pre>
