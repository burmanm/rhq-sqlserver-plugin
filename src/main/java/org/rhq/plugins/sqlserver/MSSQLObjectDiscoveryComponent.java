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

public class MSSQLObjectDiscoveryComponent implements ResourceDiscoveryComponent<MSSQLDatabaseComponent<?>> {
	
    private Log log = LogFactory.getLog(MSSQLObjectDiscoveryComponent.class);
    private static final String RESOURCE_TYPE_SIMPLE_VALUE = "objectType";
    private static final String TABLE_TYPE = "U";
//    private static final String TABLE_NAME_QUERY = "select TABLE_SCHEMA + '.' + TABLE_NAME AS table_name from INFORMATION_SCHEMA.TABLES";

    private static final String OBJECT_QUERY = "SELECT o.name AS object_name, s.name AS schema_name, o.object_id FROM sys.objects AS o " +
            "INNER JOIN sys.schemas AS s ON s.schema_id = o.schema_id " +
            "WHERE type = ?";

	@Override
	public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<MSSQLDatabaseComponent<?>> context)
			throws InvalidPluginConfigurationException, Exception {

        String objectType = context.getDefaultPluginConfiguration().getSimpleValue(RESOURCE_TYPE_SIMPLE_VALUE);

		String dbName = context.getParentResourceComponent().getDatabaseName();
		Set<DiscoveredResourceDetails> discoveredObjects = new HashSet<DiscoveredResourceDetails>();
        if(dbName != null && dbName.equals("tempdb")) {
            return discoveredObjects; // For tempdb, we don't want to see them (they are too temporary)
        }
        Connection conn = context.getParentResourceComponent().getConnection();

		if(conn != null) {
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			try {
				conn.setCatalog(dbName);
				statement = conn.prepareStatement(OBJECT_QUERY);
                statement.setString(1, objectType);
				resultSet = statement.executeQuery();
				
				while(resultSet.next()) {
		            String objectName = resultSet.getString("object_name");
                    String objectId = resultSet.getString("object_id");
                    if(objectType.equals(TABLE_TYPE)) {
                        objectName = resultSet.getString("schema_name") + "." + objectName;
                    }
		            DiscoveredResourceDetails service = new DiscoveredResourceDetails(context.getResourceType(), objectId,
		                    objectName, null, null, null, null);
		                service.getPluginConfiguration().put(new PropertySimple("objectName", objectName));
		                discoveredObjects.add(service);
				}
				
			} catch (SQLException e) {
				log.error("Received SQLException while executing status, ", e);
			} finally {
				DatabaseQueryUtility.close(statement, resultSet);
			}			
		}		
		
		return discoveredObjects;
	}
}
