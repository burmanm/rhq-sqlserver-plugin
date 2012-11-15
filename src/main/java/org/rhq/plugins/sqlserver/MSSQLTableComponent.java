package org.rhq.plugins.sqlserver;

import java.sql.Connection;
import java.util.Set;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.plugins.database.DatabaseComponent;

public class MSSQLTableComponent implements DatabaseComponent<MSSQLDatabaseComponent<?>>, MeasurementFacet {

    private ResourceContext<MSSQLDatabaseComponent<?>> resourceContext;

	@Override
	public void start(ResourceContext<MSSQLDatabaseComponent<?>> resourceContext) {
		this.resourceContext = resourceContext;
	}

	public String getTableName() {
		return this.resourceContext.getPluginConfiguration().getSimple("tableName").getStringValue();
	}

	@Override
	public AvailabilityType getAvailability() {
		return AvailabilityType.UP;
	}

	@Override
	public void stop() { }

	@Override
	public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> metrics) throws Exception {
		// TODO Auto-generated method stub
		
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
