rhq-sqlserver-plugin
====================

Work in progress, SQL Server RHQ Agent plugin. You'll need to manually add Microsoft's JDBC driver (jTDS should work, but it's not tested yet) to the maven repository, instructions here:

http://claude.betancourt.us/add-microsoft-sql-jdbc-driver-to-maven/

The user account that's used to poll the server requires public, VIEW SERVER STATE and VIEW ANY DEFINITION rights.