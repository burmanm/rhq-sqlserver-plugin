package org.rhq.plugins.sqlserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.plugins.database.DatabaseComponent;

public class MSSQLObjectComponent implements DatabaseComponent<MSSQLDatabaseComponent<?>>, MeasurementFacet {

    private ResourceContext<MSSQLDatabaseComponent<?>> resourceContext;
//    private static final String TABLE_STATISTICS_QUERY = "EXEC sp_spaceused ?";

	@Override
	public void start(ResourceContext<MSSQLDatabaseComponent<?>> resourceContext) {
		this.resourceContext = resourceContext;
	}

	@Override
	public AvailabilityType getAvailability() {
		return resourceContext.getAvailabilityContext().getLastReportedAvailability();
	}

	@Override
	public void stop() { }

	@Override
	public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> metrics) throws Exception {
//		Connection conn = getConnection();
//		PreparedStatement ps = conn.prepareStatement(TABLE_STATISTICS_QUERY);
//		ps.setString(1, getTableName());
//		ResultSet rs = ps.executeQuery();
//		if(rs.next()) {
//	        for (MeasurementScheduleRequest metric : metrics) {
//	        	String value = rs.getString(metric.getName());
//	        	if(value != null) {
//	        		value = value.replaceAll("KB", "").replaceAll("MB", "").trim(); // remove KB/MB definitions
//	        		Double dValue = new Double(value);
//	                MeasurementDataNumeric mdn = new MeasurementDataNumeric(metric, dValue);
//	                report.addData(mdn);
//	        	}
//	        }
//		}
	}
	
	@Override
    public Connection getConnection() {
        return this.resourceContext.getParentResourceComponent().getConnection();
    }

	@Override
    public void removeConnection() {
        this.resourceContext.getParentResourceComponent().removeConnection();
    }
	

}
