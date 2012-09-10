package org.rhq.plugins.sqlserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.pluginapi.availability.AvailabilityFacet;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.plugins.database.DatabaseComponent;
import org.rhq.plugins.database.DatabaseQueryUtility;

public class MSSQLDatabaseComponent implements DatabaseComponent<MSSQLServerComponent<?>>, AvailabilityFacet {

    private static Log log = LogFactory.getLog(MSSQLDatabaseComponent.class);
	
	private MSSQLServerComponent<?> serverComponent;
	private String databaseName;
	private static String STATUS_COLUMN = "state_desc";
	private static String AVAILABLE_STATUS = "ONLINE";
	
	
	@Override
	public void start(ResourceContext<MSSQLServerComponent<?>> context) throws InvalidPluginConfigurationException, Exception {
		serverComponent = context.getParentResourceComponent();
		databaseName = context.getResourceKey();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AvailabilityType getAvailability() {

		AvailabilityType result = AvailabilityType.DOWN;
		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			Connection conn = getConnection();
			statement = conn.prepareStatement("SELECT state_desc FROM sys.databases WHERE name = ?");
			statement.setString(1, databaseName);
			resultSet = statement.executeQuery();
			
			while(resultSet.next()) {
				String status = resultSet.getString(STATUS_COLUMN);				
				if(status.equals(AVAILABLE_STATUS)) {
					result = AvailabilityType.UP;
				}
				break;
			}
		} catch (SQLException e) {
			log.error("Received SQLException while executing status, ", e);
		} finally {
			DatabaseQueryUtility.close(statement, resultSet);
		}
		
		return result;
	}

    @Override
    public Connection getConnection() {
        return serverComponent.getConnection();
    }

    @Override
    public void removeConnection() {
    	serverComponent.removeConnection();
    }

}
