package org.rhq.plugins.sqlserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.measurement.*;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.plugins.database.DatabaseComponent;
import org.rhq.plugins.database.DatabaseQueryUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: michael
 * Date: 7/24/13
 * Time: 12:59 AM
 */
public class MSSQLDataFileComponent implements DatabaseComponent<MSSQLDatabaseComponent<?>>, MeasurementFacet {

    private ResourceContext<MSSQLDatabaseComponent<?>> resourceContext;
    private Log log = LogFactory.getLog(MSSQLDataFileDiscoveryComponent.class);

    private static String AVAILABLE_STATUS = "ONLINE";
    private static String STATUS_COLUMN = "state_desc";
    private static String AVAILABILITY_QUERY = "SELECT state_desc FROM sys.master_files WHERE file_guid = ?";

    private static String TRAIT_QUERY = "SELECT m.type_desc, m.name, (CASE WHEN ds.name IS NULL THEN 'ROWS' ELSE ds.name END) AS filegroup, m.physical_name, m.state_desc, " +
            "(CASE max_size WHEN '-1' THEN 'Unlimited' " +
            "ELSE CONVERT(varchar(30), CAST(max_size/128.0 AS decimal(30,2))) END) AS max_size, " +
            "(CASE WHEN m.is_percent_growth = 1 THEN CONVERT(varchar(30), m.growth) + '%' ELSE CONVERT(varchar(30), m.growth) END) AS growth FROM sys.master_files AS m " +
            "LEFT OUTER JOIN sys.data_spaces AS ds ON m.data_space_id = ds.data_space_id " +
            "WHERE m.file_guid = ?";

    private static String METRIC_QUERY = "SELECT CAST(size/128.0 AS decimal(30,2)) AS allocated_space, CAST((size/128.0 - CAST(FILEPROPERTY(name, 'SpaceUsed') AS int)/128.0) AS decimal(30,2)) AS available_space " +
            "FROM sys.database_files WHERE file_guid = ?";

    @Override
    public Connection getConnection() {
        return this.resourceContext.getParentResourceComponent().getConnection();
    }

    @Override
    public void removeConnection() {
        this.resourceContext.getParentResourceComponent().removeConnection();
    }

    @Override
    public AvailabilityType getAvailability() {

        // TODO: This is identical to the database's Availability, only query is different. Refactor to use same code..
        AvailabilityType result = AvailabilityType.DOWN;

        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            Connection conn = getConnection();
            statement = conn.prepareStatement(AVAILABILITY_QUERY);
            statement.setString(1, this.resourceContext.getResourceKey());
            resultSet = statement.executeQuery();

            if(resultSet.next()) {
                String status = resultSet.getString(STATUS_COLUMN);
                if(status.equals(AVAILABLE_STATUS)) {
                    result = AvailabilityType.UP;
                }
            }
        } catch (SQLException e) {
            log.error("Received SQLException while executing status, ", e);
        } finally {
            DatabaseQueryUtility.close(statement, resultSet);
        }

        return result;
    }

    @Override
    public void getValues(MeasurementReport measurementReport, Set<MeasurementScheduleRequest> measurementScheduleRequests) throws Exception {
        Map<String, Object> traitValues = null;
        Map<String, Double> metricValues = null;

        for (MeasurementScheduleRequest request : measurementScheduleRequests) {

            String property = request.getName();

            switch(request.getDataType()) {
                case TRAIT:
                    if(traitValues == null) {
                        traitValues = DatabaseQueryUtility.getGridValues(this.resourceContext.getParentResourceComponent(), TRAIT_QUERY, this.resourceContext.getResourceKey()).get(0);
                    }
                    measurementReport.addData(new MeasurementDataTrait(request, (String) traitValues.get(property)));
                    break;
                case MEASUREMENT:
                    if(metricValues == null) {
                        metricValues = DatabaseQueryUtility.getNumericQueryValues(this, METRIC_QUERY);
                    }
                    measurementReport.addData(new MeasurementDataNumeric(request, metricValues.get(property)));
                    break;
                default:
                    // Not supported here
                    break;
            }
        }
    }

    @Override
    public void start(ResourceContext<MSSQLDatabaseComponent<?>> mssqlDatabaseComponentResourceContext) throws InvalidPluginConfigurationException, Exception {
        this.resourceContext = mssqlDatabaseComponentResourceContext;
    }

    @Override
    public void stop() { }
}
