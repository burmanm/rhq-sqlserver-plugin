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
import java.sql.DatabaseMetaData;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.Nullable;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ManualAddFacet;
import org.rhq.core.pluginapi.inventory.ProcessScanResult;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.core.system.ProcessInfo;
import org.rhq.core.util.jdbc.JDBCUtil;

public class MSSQLDiscoveryComponent implements ResourceDiscoveryComponent, ManualAddFacet {

	private static final Log log = LogFactory.getLog(MSSQLDiscoveryComponent.class);

	public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext resourceDiscoveryContext)
			throws InvalidPluginConfigurationException, Exception {
		Set<DiscoveredResourceDetails> found = new HashSet<DiscoveredResourceDetails>();
		List<ProcessScanResult> autoDiscoveryResults = resourceDiscoveryContext.getAutoDiscoveredProcesses();
		for (ProcessScanResult process : autoDiscoveryResults) {

			String pwd = process.getProcessInfo().getExecutable().getCwd();

			String version = pwd;

			Configuration pluginConfig = resourceDiscoveryContext.getDefaultPluginConfiguration().deepCopy();
			
			DiscoveredResourceDetails details = createResourceDetails(resourceDiscoveryContext, pluginConfig, version,
					process.getProcessInfo());
			found.add(details);
		}

		return found;
	}

	public DiscoveredResourceDetails discoverResource(Configuration pluginConfig, ResourceDiscoveryContext resourceDiscoveryContext)
			throws InvalidPluginConfigurationException {

		Connection connection = null;
		String version = null;
		try {
			connection = MSSQLServerComponent.buildConnection(pluginConfig);
			DatabaseMetaData dbmd = connection.getMetaData();
			version = dbmd.getDatabaseMajorVersion() + "." + dbmd.getDatabaseMinorVersion();
		} catch (Exception e) {
			log.warn("Could not connect to SQL Server with supplied configuration", e);
//			throw new InvalidPluginConfigurationException("Unable to connect to SQL Server", e);
		} finally {
			JDBCUtil.safeClose(connection);
		}

		DiscoveredResourceDetails details = createResourceDetails(resourceDiscoveryContext, pluginConfig, version, null);
		return details;
	
	}

	private static DiscoveredResourceDetails createResourceDetails(ResourceDiscoveryContext discoveryContext,
			Configuration pluginConfig, String version, @Nullable ProcessInfo processInfo) {
		String description = "SQL Server " + version;
		String key = "sqlserver";
		String value = "2008R2";
		return new DiscoveredResourceDetails(discoveryContext.getResourceType(), key, value, version, description, pluginConfig,
				processInfo);
	}
}
