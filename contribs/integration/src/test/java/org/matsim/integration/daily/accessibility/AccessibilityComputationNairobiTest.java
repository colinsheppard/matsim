/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.integration.daily.accessibility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.accessibility.AccessibilityConfigGroup;
import org.matsim.contrib.accessibility.utils.AccessibilityUtils;
import org.matsim.contrib.accessibility.utils.VisualizationUtils;
import org.matsim.contrib.matrixbasedptrouter.MatrixBasedPtRouterConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup.VspDefaultsCheckingLevel;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.testcases.MatsimTestUtils;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author dziemke
 */
public class AccessibilityComputationNairobiTest {
	public static final Logger log = Logger.getLogger(AccessibilityComputationNairobiTest.class);

	private static final Double cellSize = 5000.;

	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;

	@Test
	public void doAccessibilityTest() throws IOException {
		// Input and output
		String folderStructure = "../../";
		String networkFile = "matsimExamples/countries/ke/nairobi/2015-10-15_network.xml";
//		String networkFile = "../shared-svn/projects/maxess/data/nairobi/network/2015-10-15_network_modified_policy.xml";
//		String networkFile = "../shared-svn/projects/maxess/data/kenya/network/2016-10-19_network_detailed.xml";
		// Adapt folder structure that may be different on different machines, in particular on server
		folderStructure = PathUtils.tryANumberOfFolderStructures(folderStructure, networkFile);
		networkFile = folderStructure + networkFile;
//		final String facilitiesFile = folderStructure + "matsimExamples/countries/ke/nairobi/2015-10-15_facilities.xml";
//		final String facilitiesFile = "../../../shared-svn/projects/maxess/data/nairobi/land_use/nairobi_LU_2010/facilities.xml";
//		final String facilitiesFile = "../../../shared-svn/projects/maxess/data/nairobi/kodi/schools/primary_public/facilities.xml";
//		final String facilitiesFile = "../../../shared-svn/projects/maxess/data/nairobi/kodi/health/hospitals/facilities.xml";
		final String facilitiesFile = folderStructure + "matsimExamples/countries/ke/nairobi/2016-07-09_facilities_airports.xml"; //airports
//		final String facilitiesFile = "../../../shared-svn/projects/maxess/data/nairobi/facilities/04/facilities.xml"; //airports
		final String outputDirectory = utils.getOutputDirectory();
//		final String outputDirectory = "../../../shared-svn/projects/maxess/data/nairobi/output/27/";
//		String travelTimeMatrix = "../../../shared-svn/projects/maxess/data/nairobi/digital_matatus/gtfs/matrix/temp/tt.csv";
//		String travelDistanceMatrix = "../../../shared-svn/projects/maxess/data/nairobi/digital_matatus/gtfs/matrix/temp/td.csv";
//		String ptStops = "../../../shared-svn/projects/maxess/data/nairobi/digital_matatus/gtfs/matrix/temp/IDs.csv";
		
		// Parameters
		final String crs = "EPSG:21037"; // = Arc 1960 / UTM zone 37S, for Nairobi, Kenya
		final Envelope envelope = new Envelope(240000, 280000, 9844000, 9874000); // whole Nairobi
//		final Envelope envelope = new Envelope(246000, 271000, 9853000, 9863000); // whole Nairobi // central part
//		final Envelope envelope = new Envelope(249000, 255000, 9854000, 9858000); // western Nairobi with Kibera
//		final Envelope envelope = new Envelope(-70000, 800000, 9450000, 10500000); // whole Kenya
//		final Envelope envelope = new Envelope(-70000, 420000, 9750000, 10100000); // Southwestern half of Kenya
		final String runId = "ke_nairobi_" + AccessibilityUtils.getDate() + "_" + cellSize.toString().split("\\.")[0] + "_kodi_sec_";
		final boolean push2Geoserver = true;
		
		// QGis parameters
		boolean createQGisOutput = false;
		final boolean includeDensityLayer = true;
		final Double lowerBound = -7.; // (upperBound - lowerBound) ideally nicely divisible by (range - 2)
		final Double upperBound = 0.0;
		final Integer range = 9; // In the current implementation, this must always be 9
		final int symbolSize = 500; // Usually chosen a little bit larger than cellSize
		final int populationThreshold = (int) (1 / (1000/cellSize * 1000/cellSize));
		
		// Storage objects
		final List<String> modes = new ArrayList<>();
		
		// Config and scenario
		final Config config = ConfigUtils.createConfig(new AccessibilityConfigGroup(), new MatrixBasedPtRouterConfigGroup());
		config.network().setInputFile(networkFile);
		config.facilities().setInputFile(facilitiesFile);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory(outputDirectory);
		config.controler().setLastIteration(0);
//		config.planCalcScore().setBrainExpBeta(200); // Set to high value to base computation on time to nearest facility only
		final Scenario scenario = ScenarioUtils.loadScenario(config);

		// Collect activity types
//		final List<String> activityTypes = AccessibilityRunUtils.collectAllFacilityOptionTypes(scenario);
//		log.info("Found activity types: " + activityTypes);
		final List<String> activityTypes = new ArrayList<>();
		activityTypes.add("airport"); // airport anaylis version
//		activityTypes.add("Educational"); // land-use file version
//		activityTypes.add("Commercial"); // land-use file version
//		activityTypes.add("Industrial"); // land-use file version
//		activityTypes.add("Public Purpose"); // land-use file version
//		activityTypes.add("Recreational"); // land-use file version
//		activityTypes.add("Hospital"); // kodi file version
		
		// Settings for VSP check
		config.timeAllocationMutator().setMutationRange(7200.);
		config.timeAllocationMutator().setAffectingDuration(false);
		config.plans().setRemovingUnneccessaryPlanAttributes(true);
		config.plans().setActivityDurationInterpretation(PlansConfigGroup.ActivityDurationInterpretation.tryEndTimeThenDuration);
		config.vspExperimental().setVspDefaultsCheckingLevel(VspDefaultsCheckingLevel.warn);
		
		// Network density points
		ActivityFacilities measuringPoints = AccessibilityUtils.createMeasuringPointsFromNetworkBounds(scenario.getNetwork(), cellSize);
		double maximumAllowedDistance = 0.5 * cellSize;
		final ActivityFacilities densityFacilities = AccessibilityUtils.createNetworkDensityFacilities(scenario.getNetwork(), measuringPoints, maximumAllowedDistance);

		// Controller
		final Controler controler = new Controler(scenario);
//		controler.addControlerListener(new AccessibilityStartupListener(activityTypes, densityFacilities, crs, runId, envelope, cellSize, push2Geoserver));
		if ( true ) {
			throw new RuntimeException("AccessibilityStartupListener is no longer supported; please switch to GridBasedAccessibilityModule. kai, dec'16") ;
		}

		if ( true ) {
			throw new RuntimeException("The now following execution path is no longer supported; please set the modes in the config (as it was earlier). kai, dec'16" ) ;
		}
//		// Add calculators
//		controler.addOverridingModule(new AbstractModule() {
//			@Override
//			public void install() {
//				MapBinder<String,AccessibilityContributionCalculator> accBinder = MapBinder.newMapBinder(this.binder(), String.class, AccessibilityContributionCalculator.class);
//				{
//					String mode = "freeSpeed";
//					this.binder().bind(AccessibilityContributionCalculator.class).annotatedWith(Names.named(mode)).toProvider(new FreeSpeedNetworkModeProvider(TransportMode.car));
//					accBinder.addBinding(mode).to(Key.get(AccessibilityContributionCalculator.class, Names.named(mode)));
//					if (!modes.contains(mode)) modes.add(mode); // This install method is called four times, but each new mode should only be added once
//				}
//				{
//					String mode = TransportMode.car;
//					this.binder().bind(AccessibilityContributionCalculator.class).annotatedWith(Names.named(mode)).toProvider(new NetworkModeProvider(mode));
//					accBinder.addBinding(mode).to(Key.get(AccessibilityContributionCalculator.class, Names.named(mode)));
//					if (!modes.contains(mode)) modes.add(mode); // This install method is called four times, but each new mode should only be added once
//				}
//				{ 
//					String mode = TransportMode.bike;
//					this.binder().bind(AccessibilityContributionCalculator.class).annotatedWith(Names.named(mode)).toProvider(new ConstantSpeedModeProvider(mode));
//					accBinder.addBinding(mode).to(Key.get(AccessibilityContributionCalculator.class, Names.named(mode)));
//					if (!modes.contains(mode)) modes.add(mode); // This install method is called four times, but each new mode should only be added once
//				}
//				{
//					final String mode = TransportMode.walk;
//					this.binder().bind(AccessibilityContributionCalculator.class).annotatedWith(Names.named(mode)).toProvider(new ConstantSpeedModeProvider(mode));
//					accBinder.addBinding(mode).to(Key.get(AccessibilityContributionCalculator.class, Names.named(mode)));
//					if (!modes.contains(mode)) modes.add(mode); // This install method is called four times, but each new mode should only be added once
//				}
//			}
//		});
		controler.run();

		// QGis
		if (createQGisOutput == true) {
			String osName = System.getProperty("os.name");
			String workingDirectory = config.controler().getOutputDirectory();
			for (String actType : activityTypes) {
				String actSpecificWorkingDirectory = workingDirectory + actType + "/";
				for (String mode : modes) {
					VisualizationUtils.createQGisOutput(actType, mode, envelope, workingDirectory, crs, includeDensityLayer,
							lowerBound, upperBound, range, symbolSize, populationThreshold);
					VisualizationUtils.createSnapshot(actSpecificWorkingDirectory, mode, osName);
				}
			}  
		}
	}
}