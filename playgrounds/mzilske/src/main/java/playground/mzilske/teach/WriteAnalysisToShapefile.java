/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.mzilske.teach;

import java.util.ArrayList;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.gis.PolylineFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;

public class WriteAnalysisToShapefile {

	public static void main(String[] args) {
		Config config = ConfigUtils.loadConfig("examples/equil/config.xml");
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		PolylineFeatureFactory factory = new PolylineFeatureFactory.Builder().
				addAttribute("linkId", String.class).
				addAttribute("laenge", Double.class).
				create();

		ArrayList<SimpleFeature> features = new ArrayList<SimpleFeature>();
		for (Link link : scenario.getNetwork().getLinks().values()) {
			System.out.println(link.getLength());
			SimpleFeature feature = factory.createPolyline(
					new Coordinate[] {
							new Coordinate(link.getFromNode().getCoord().getX(), link.getFromNode().getCoord().getY()),
							new Coordinate(link.getToNode().getCoord().getX(), link.getToNode().getCoord().getY())
					},
					new Object[] {
							link.getId(),
							link.getLength()
					}, 
					null);
			features.add(feature);
		}

		ShapeFileWriter.writeGeometries(features, "output/kanten");
	}

}
