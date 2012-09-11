package org.rhq.plugins.sqlserver;

import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.plugins.database.CustomTableComponent;

public class MSSQLTableComponent extends CustomTableComponent {

	@Override
	public AvailabilityType getAvailability() {
		return AvailabilityType.UP;
	}
	
}
