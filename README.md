rhq-sqlserver-plugin
====================

Work in progress, SQL Server RHQ Agent plugin. You'll need to manually add Microsoft's JDBC driver (jTDS should work, but it's not tested yet) to the maven repository, instructions here:

http://claude.betancourt.us/add-microsoft-sql-jdbc-driver-to-maven/

The user account that's used to poll the server requires public, VIEW SERVER STATE and VIEW ANY DEFINITION rights.

This plugin can't support the scenario of clustered (active-passive) SQL Server installation. This is a limitation of RHQ. For possible workarounds, see issue #20
