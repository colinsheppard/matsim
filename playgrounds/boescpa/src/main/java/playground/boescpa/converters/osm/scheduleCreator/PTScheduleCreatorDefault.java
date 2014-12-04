/*
 * *********************************************************************** *
 * project: org.matsim.*                                                   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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
 * *********************************************************************** *
 */

package playground.boescpa.converters.osm.scheduleCreator;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.vehicles.Vehicle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The default implementation of PTStationCreator (using the Swiss-HAFAS-Schedule).
 *
 * @author boescpa
 */
public class PTScheduleCreatorDefault extends PTScheduleCreator {

	private CoordinateTransformation transformWGS84toCH1903_LV03 = TransformationFactory.getCoordinateTransformation("WGS84", "CH1903_LV03");
	private static final Set<Id<Vehicle>> vehicles = new HashSet<>();
	// TODO-boescpa Create a vehicles-file...

	public PTScheduleCreatorDefault(TransitSchedule schedule) {
		super(schedule);
	}

	@Override
	public final void createSchedule(String osmFile, String hafasFolder, Network network) {
		log.info("Creating the schedule...");
		createPTLines(hafasFolder);
		complementPTStations(osmFile);
		log.info("Creating the schedule... done.");
	}

	/**
	 * Create all pt-lines (stops, schedule, but no routes) of all types of public transport
	 * using the HAFAS-schedule.
	 *
	 * Writes the resulting schedule into this.schedule.
	 *
	 * @param hafasFolder
	 */
	private void createPTLines(String hafasFolder) {
		log.info("Creating pt lines from HAFAS file...");

		// TODO-boescpa Implement createPTLines...
		// work with this.schedule...

		// 1. Read all stops from HAFAS-BFKOORD_GEO
		readStops(hafasFolder + "/BFKOORD_GEO");
		// 2. Create all lines from HAFAS-Schedule
		//		1. Stops

		//		2. Schedule


		log.info("Creating pt lines from HAFAS file... done.");
	}

	/**
	 * Check and correct pt-Station-coordinates with osm-knowledge.
	 *
	 * Writes the resulting schedule into this.schedule.
	 *
	 * @param osmFile
	 */
	private void complementPTStations(String osmFile) {
		log.info("Correcting pt station coordinates based on OSM...");

		// TODO-boescpa Implement complementPTStations...
		// work with this.schedule...

		log.info("Correcting pt station coordinates based on OSM... done.");
	}

	////////////////// Local Helpers /////////////////////

	protected void readStops(String BFKOORD_GEOFile) {
		try {
			FileReader reader = new FileReader(BFKOORD_GEOFile);
			BufferedReader readsLines = new BufferedReader(reader);
			String newLine = readsLines.readLine();
			while (newLine != null) {
				/*Spalte Typ Bedeutung
				1−7 INT32 Nummer der Haltestelle
				9−18 FLOAT X-Koordinate
				20−29 FLOAT Y-Koordinate
				31−36 INT16 Z-Koordinate (optional)
				38ff CHAR Kommentarzeichen "%"gefolgt vom Klartext des Haltestellennamens (optional zur besseren Lesbarkeit)*/
				Id<TransitStopFacility> stopId = Id.create(newLine.substring(0, 7), TransitStopFacility.class);
				double xCoord = Double.parseDouble(newLine.substring(8, 18));
				double yCoord = Double.parseDouble(newLine.substring(19, 29));
				Coord coord = this.transformWGS84toCH1903_LV03.transform(new CoordImpl(xCoord, yCoord));
				String stopName = newLine.substring(39, newLine.length());
				createStop(stopId, coord, stopName);
				newLine = readsLines.readLine();
			}
			readsLines.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createStop(Id<TransitStopFacility> stopId, Coord coord, String stopName) {
		TransitStopFacility stopFacility = this.scheduleBuilder.createTransitStopFacility(stopId, coord, false);
		stopFacility.setName(stopName);
		this.schedule.addStopFacility(stopFacility);
	}

	protected void readLines(String FPLAN) {
		try {
			FileReader reader = new FileReader(FPLAN);
			BufferedReader readsLines = new BufferedReader(reader);
			String newLine = readsLines.readLine();
			while (newLine != null) {
				if (newLine.charAt(0) == '*') {
					switch (newLine.charAt(1)) {
						case 'Z': // Initialzeile neue Fahrt
							/*Spalte Typ Bedeutung
							1−2 CHAR *Z
							4−8 INT32 Fahrtnummer
							10−15 CHAR Verwaltung (6-stellig); Die Verwaltungsangabe darf
							keine Leerzeichen enthalten.
							17−21 INT16 leer
							23−25 INT16 Taktanzahl; gibt die Anzahl der noch folgenden Takte
							an.
							27−29 INT16 Taktzeit in Minuten (Abstand zwischen zwei Fahrten).*/

							break;
						case 'T': // Initialzeile neue Fahrt (Unterschied zu Z-Zeile: Flexible Abfahrtszeiten <-> AwaitDeparture = False)
							/*Spalte Typ Bedeutung
							1−2 CHAR *T
							4−8 INT32 Fahrtnummer
							10−15 CHAR Verwaltung (6-stellig); Die Verwaltungsangabe darf
							keine Leerzeichen enthalten.
							17−20 INT16 Fahrtzeitraum in Minuten
							22−25 INT16 Taktdichte in Sekunden (Abstand zweier Fahrten).*/

							break;
						case 'G': // Verkehrsmittelzeile
							/*Spalte Typ Bedeutung
							1−2 CHAR *G
							4−6 CHAR Verkehrsmittel bzw. Gattung
							8−14 [#]INT32 (optional) Laufwegsindex oder Haltestellennummer,
								ab der die Gattung gilt.
							16−22 [#]INT32 (optional) Laufwegsindex oder Haltestellennummer,
								bis zu der die Gattung gilt.
							24−29 [#]INT32 (optional) Index für das x. Auftreten oder
							Abfahrtszeitpunkt
							31−36 [#]INT32 (optional) Index für das x. Auftreten oder
							Ankunftszeitpunkt*/

							break;
					}
				} else if (newLine.charAt(0) == '+') { // Regionszeile (Bedarfsfahrten)
					// We don't have this transport mode in  MATSim (yet). => Delete Route and if Line now empty, delete Line.

				} else { // Laufwegzeile
					/*Spalte Typ Bedeutung
					1−7 INT32 Haltestellennummer
					9−29 CHAR (optional zur Lesbarkeit) Haltestellenname
					30−35 INT32 Ankunftszeit an der Haltestelle (lt. Ortszeit der
							Haltestelle)
					37−42 INT32 Abfahrtszeit an Haltestelle (lt. Ortszeit der
					Haltestelle)
					44−48 INT32 Ab dem Halt gültige Fahrtnummer (optional)
							50−55 CHAR Ab dem Halt gültige Verwaltung (optional)
							57−57 CHAR (optional) "X", falls diese Haltestelle auf dem
					Laufschild der Fahrt aufgeführt wird.*/

				}




				Id<TransitStopFacility> stopId = Id.create(newLine.substring(0, 7), TransitStopFacility.class);
				double xCoord = Double.parseDouble(newLine.substring(8, 18));
				double yCoord = Double.parseDouble(newLine.substring(19, 29));
				Coord coord = this.transformWGS84toCH1903_LV03.transform(new CoordImpl(xCoord, yCoord));
				String stopName = newLine.substring(39, newLine.length());
				createStop(stopId, coord, stopName);
				newLine = readsLines.readLine();
			}
			readsLines.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class PtLineFPLAN {
		public final Id<TransitLine> lineId;
		private final List<PtRouteFPLAN> routesFPLAN = new ArrayList<>();

		public PtLineFPLAN(String lineId) {
			this.lineId = Id.create(lineId, TransitLine.class);
		}

		public void createLine() {
			TransitLine line = scheduleBuilder.createTransitLine(lineId);
			for (PtRouteFPLAN route : this.routesFPLAN) {
				line.addRoute(route.getRoute());
			}
			schedule.addTransitLine(line);
		}

		public void addPtRouteFPLAN(PtRouteFPLAN route) {
			routesFPLAN.add(route);
		}
	}

	private class PtRouteFPLAN {
		public static final boolean TYPE_Z = true;
		public static final boolean TYPE_T = false;

		private final Id<TransitRoute> routeId;
		private final boolean type;
		private final int numberOfDepartures;
		private final int cycleTime; // [sec]
		private final List<TransitRouteStop> stops = new ArrayList<>();

		private int firstDepartureTime = -1; //[sec]

		public void setFirstDepartureTime(int hour, int minute) {
			if (firstDepartureTime < 0) {
				this.firstDepartureTime = (hour * 3600) + (minute * 60);
			}
		}

		private Id<Vehicle> usedVehicle = null;

		public void setUsedVehicle(String usedVehicle) {
			if (this.usedVehicle == null) {
				this.usedVehicle = Id.create(usedVehicle, Vehicle.class);
			}
			vehicles.add(Id.create(usedVehicle, Vehicle.class));
		}

		public PtRouteFPLAN(String lineId, boolean type, int numberOfDepartures, int cycleTime) {
			this.routeId = Id.create(lineId, TransitRoute.class);
			this.type = type;
			this.numberOfDepartures = numberOfDepartures;
			if (type) { // <=> If is TYPE_Z, then given cycleTime in [min] and we have to change it...
				this.cycleTime = cycleTime * 60;
			} else { // <=> If is TYPE_T, then given cycleTime already in [sec]...
				this.cycleTime = cycleTime;
			}
		}

		public TransitRoute getRoute() {
			TransitRoute transitRoute = scheduleBuilder.createTransitRoute(routeId, null, stops, "pt");
			for (Departure departure : this.getDepartures()) {
				transitRoute.addDeparture(departure);
			}
			return transitRoute;
		}

		/**
		 * @param stopId
		 * @param arrivalTime   Expected as seconds from midnight or zero if not available.
		 * @param departureTime Expected as seconds from midnight or zero if not available.
		 */
		public void addStop(String stopId, double arrivalTime, double departureTime) {
			TransitStopFacility stopFacility = schedule.getFacilities().get(Id.create(stopId, TransitStopFacility.class));
			double arrivalDelay = 0.0;
			if (arrivalTime > 0 && firstDepartureTime > 0) {
				arrivalDelay = arrivalTime - firstDepartureTime;
			}
			double departureDelay = 0.0;
			if (departureTime > 0 && firstDepartureTime > 0) {
				departureDelay = departureTime - firstDepartureTime;
			} else if (arrivalDelay > 0) {
				departureDelay = arrivalDelay + 1;
			}
			stops.add(createRouteStop(stopFacility, arrivalDelay, departureDelay, type));
		}

		/**
		 *
		 * @return A list of all departures of this route. If firstDepartureTime or usedVehicle are not set before this is called, null is returned.
		 */
		private List<Departure> getDepartures() {
			if (firstDepartureTime < 0 || usedVehicle == null) {
				return null;
			}

			List<Departure> departures = new ArrayList<>();
			for (int i = 0; i < numberOfDepartures; i++) {
				Id<Departure> departureId = Id.create(routeId.toString() + "_" + (i + 1), Departure.class);
				double departureTime = firstDepartureTime + (i*cycleTime);
				departures.add(createDeparture(departureId, departureTime, usedVehicle));
			}
			return departures;
		}

		private TransitRouteStop createRouteStop(TransitStopFacility stopFacility, double arrivalDelay, double departureDelay, boolean awaitDepartureTime) {
			TransitRouteStop routeStop = scheduleBuilder.createTransitRouteStop(stopFacility, arrivalDelay, departureDelay);
			routeStop.setAwaitDepartureTime(awaitDepartureTime);
			return routeStop;
		}

		private Departure createDeparture(Id<Departure> departureId, double departureTime, Id<Vehicle> vehicleId) {
			Departure departure = scheduleBuilder.createDeparture(departureId, departureTime);
			departure.setVehicleId(vehicleId);
			return departure;
		}
	}

}