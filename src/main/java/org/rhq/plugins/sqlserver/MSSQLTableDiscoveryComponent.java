package org.rhq.plugins.sqlserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.plugins.database.DatabaseQueryUtility;

public class MSSQLTableDiscoveryComponent implements ResourceDiscoveryComponent<MSSQLDatabaseComponent<?>> {
	
    private Log log = LogFactory.getLog(MSSQLTableDiscoveryComponent.class);
    private static final String TABLE_NAME_QUERY = "select TABLE_SCHEMA + '.' + TABLE_NAME AS table_name from INFORMATION_SCHEMA.TABLES"; 

	@Override
	public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<MSSQLDatabaseComponent<?>> context)
			throws InvalidPluginConfigurationException, Exception {
		
		String dbName = context.getParentResourceComponent().getDatabaseName();
		Connection conn = context.getParentResourceComponent().getConnection();
		Set<DiscoveredResourceDetails> discoveredTables = new HashSet<DiscoveredResourceDetails>();

		if(conn != null) {
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			try {
				statement = conn.prepareStatement(TABLE_NAME_QUERY);
				resultSet = statement.executeQuery();
				
				while(resultSet.next()) {
		            String tableName = resultSet.getString("table_name");
		            DiscoveredResourceDetails service = new DiscoveredResourceDetails(context.getResourceType(), tableName,
		                    tableName, null, null, null, null);
		                service.getPluginConfiguration().put(new PropertySimple("tableName", tableName));
		                discoveredTables.add(service);
				}
				
			} catch (SQLException e) {
				log.error("Received SQLException while executing status, ", e);
			} finally {
				DatabaseQueryUtility.close(statement, resultSet);
			}			
		}		
		
		return discoveredTables;
	}
}
