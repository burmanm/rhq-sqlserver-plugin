/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.plugins.sqlserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.util.jdbc.JDBCUtil;
import org.rhq.plugins.database.DatabaseComponent;

public class MSSQLServerComponent<T extends ResourceComponent<?>> implements DatabaseComponent<T>, MeasurementFacet {
    private static final Log LOG = LogFactory.getLog(MSSQLServerComponent.class);

    private Connection connection;

    private ResourceContext resourceContext;

    private boolean started;

    public void start(ResourceContext resourceContext) throws InvalidPluginConfigurationException, Exception {
        this.resourceContext = resourceContext;
        this.connection = buildConnection(resourceContext.getPluginConfiguration());
        this.started = true;
    }

    public void stop() {
        removeConnection();
        this.started = false;
    }

    public AvailabilityType getAvailability() {
        if (started && getConnection() != null) {
            return AvailabilityType.UP;
        } else {
            return AvailabilityType.DOWN;
        }
    }

    /*
     * Implement later.
     * 
     * (non-Javadoc)
     * @see org.rhq.core.pluginapi.measurement.MeasurementFacet#getValues(org.rhq.core.domain.measurement.MeasurementReport, java.util.Set)
     */
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> metrics) {
    	
    }

    public Connection getConnection() {
        try {
            if (this.connection == null || connection.isClosed()) {
                this.connection = buildConnection(this.resourceContext.getPluginConfiguration());
            }
        } catch (SQLException e) {
            LOG.info("Unable to create oracle connection", e);
        }
        return this.connection;
    }

    public void removeConnection() {
        JDBCUtil.safeClose(connection);
        this.connection = null;
    }

    public static Connection buildConnection(Configuration configuration) throws SQLException {
        String driverClass = configuration.getSimple("driverClass").getStringValue();
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new InvalidPluginConfigurationException("Specified JDBC driver class (" + driverClass
                + ") not found.");
        }

        String url = buildUrl(configuration);
        LOG.debug("Attempting JDBC connection to [" + url + "]");

        String principal = configuration.getSimple("principal").getStringValue();
        String credentials = configuration.getSimple("credentials").getStringValue();
        String instance = configuration.getSimple("instanceName").getStringValue();

        Properties props = new Properties();
        props.put("user", principal);
        props.put("password", credentials);
        if(instance != null && !instance.equals("MSSQLSERVER")) {
        	props.put("instanceName", instance);
        }

        return DriverManager.getConnection(url, props);
    }

    private static String buildUrl(Configuration configuration) {
    	return "jdbc:sqlserver://" + configuration.getSimpleValue("host", "localhost") + ":"
    			+ configuration.getSimpleValue("port", "1433");
    }
}