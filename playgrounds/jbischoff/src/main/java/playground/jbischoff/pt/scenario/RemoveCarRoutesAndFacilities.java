/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

/**
 * 
 */
package playground.jbischoff.pt.scenario;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * @author  jbischoff
 *
 */
/**
 *
 */
public class RemoveCarRoutesAndFacilities {
public static void main(String[] args) {
	String inputFile ="C:/Users/Joschka/Documents/shared-svn/studies/jbischoff/multimodal/berlin/input/10pct/bvg.run189.10pct.100.plans.filtered.selected.xml.gz_removedTransitActs.xml.gz";
	String outputFile ="C:/Users/Joschka/Documents/shared-svn/studies/jbischoff/multimodal/berlin/input/10pct/bvg.run189.10pct.100.plans.filtered.selected.noRoutes.xml.gz";
	
	Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
	new PopulationReader(scenario).readFile(inputFile);
	for (Person p : scenario.getPopulation().getPersons().values()){
		for (Plan plan : p.getPlans()){
			for (PlanElement pe : plan.getPlanElements()){
				if (pe instanceof Leg){
					Leg l = (Leg) pe;
					if (l.getMode().equals(TransportMode.car)){
						l.setRoute(null);
					}
				}
				else if (pe instanceof Activity){
					((Activity) pe).setFacilityId(null);
					((Activity) pe).setLinkId(null);
				}
			}
		}
	}
	new PopulationWriter(scenario.getPopulation()).write(outputFile);
}
}
