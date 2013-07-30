package org.rhq.plugins.sqlserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.core.util.jdbc.JDBCUtil;

public class MSSQLDatabaseDiscoveryComponent implements ResourceDiscoveryComponent<MSSQLServerComponent<?>> {

	@Override
	public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<MSSQLServerComponent<?>> context)
			throws InvalidPluginConfigurationException, SQLException {
        Set<DiscoveredResourceDetails> databases = new HashSet<DiscoveredResourceDetails>();

        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = context.getParentResourceComponent().getConnection().createStatement();
            resultSet = statement
                .executeQuery("SELECT name, database_id FROM sys.databases");
            while (resultSet.next()) {
                String databaseName = resultSet.getString("name");
                String databaseId = resultSet.getString("database_id");
                DiscoveredResourceDetails database = new DiscoveredResourceDetails(context.getResourceType(),
                    databaseId, databaseName, null, "The " + databaseName + " database", null, null);
                database.getPluginConfiguration().put(new PropertySimple("databaseName", databaseName));
                databases.add(database);
            }
        } finally {
            JDBCUtil.safeClose(statement, resultSet);
        }

        return databases;
	}

}
