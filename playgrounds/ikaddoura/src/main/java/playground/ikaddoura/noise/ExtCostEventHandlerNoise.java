/* *********************************************************************** *
 * project: org.matsim.*
 * MoneyEventHandler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package playground.ikaddoura.noise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.TransitDriverStartsEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonMoneyEventHandler;
import org.matsim.api.core.v01.events.handler.TransitDriverStartsEventHandler;

/**
 * 
 * @author ikaddoura , lkroeger
 *
 */
public class ExtCostEventHandlerNoise implements NoiseEventAffectedHandler , NoiseEventHandler , PersonMoneyEventHandler, TransitDriverStartsEventHandler , ActivityEndEventHandler , PersonDepartureEventHandler , LinkEnterEventHandler, PersonEntersVehicleEventHandler , PersonLeavesVehicleEventHandler {
	private final static Logger log = Logger.getLogger(ExtCostEventHandlerNoise.class);
	private final double vtts_car;
	
	// This analysis uses either money events or congestion events.
	private final boolean useMoneyEvents;
	
	private Map<Id,Integer> personId2actualTripNumber = new HashMap<Id, Integer>();
	private Map<Id,Map<Integer,String>> personId2tripNumber2legMode = new HashMap<Id,Map<Integer,String>>();
	
	private Map<Id,Map<Integer,Double>> personId2tripNumber2departureTime = new HashMap<Id, Map<Integer,Double>>();
	private Map<Id,Map<Integer,Double>> personId2tripNumber2tripDistance = new HashMap<Id, Map<Integer,Double>>();
	private Map<Id,Map<Integer,Double>> personId2tripNumber2amount = new HashMap<Id, Map<Integer,Double>>();
	private Map<Id,Double> driverId2totalDistance = new HashMap<Id,Double>();
	
	private Map<Id, Double> personId2amountSum = new HashMap <Id, Double>();
	private Map<Id, Double> personId2amountSumAffected = new HashMap <Id, Double>();
	private List<Id> persons = new ArrayList<Id>();
	
	// for pt-distance calculation
	private Map<Id,Double> personId2distanceEnterValue = new HashMap<Id,Double>();
	
	private List<Id> ptDrivers = new ArrayList<Id>();
	private Scenario scenario;
	
	private double distance = 500.;
	private double maxDistance = 40 * distance;
	private double timeBinSize = 300.0;
	
	public ExtCostEventHandlerNoise(Scenario scenario, boolean useMoneyEvents) {
		this.scenario = scenario;
		this.useMoneyEvents = useMoneyEvents;
		log.info("UseMoneyEvents : " + useMoneyEvents);
		this.vtts_car = (this.scenario.getConfig().planCalcScore().getTraveling_utils_hr() - this.scenario.getConfig().planCalcScore().getPerforming_utils_hr()) / this.scenario.getConfig().planCalcScore().getMarginalUtilityOfMoney();
		log.info("VTTS_car: " + vtts_car);
	}

	@Override
	public void reset(int iteration) {
		personId2actualTripNumber.clear();
		personId2tripNumber2departureTime.clear();
		personId2tripNumber2tripDistance.clear();
		personId2tripNumber2amount.clear();
		personId2tripNumber2legMode.clear();
		driverId2totalDistance.clear();
		personId2distanceEnterValue.clear();
		ptDrivers.clear(); // not really necessary
		personId2amountSum.clear();
		personId2amountSumAffected.clear();
	}
	
	@Override
	public void handleEvent(NoiseEvent event) {
//		log.info("NOISEEVENT: "+event.getAmount());
		if (!(useMoneyEvents == true)) {
			
//			log.info("###################");
			
			// trip-based analysis
			double amount = event.getAmount();
			double eventTime = event.getTime();
			int tripNumber = 0;
			double maxDepTime = 0.;
			Map<Integer,Double> tripNumber2departureTime = personId2tripNumber2departureTime.get(event.getAgentId());
			
			for(int tripNr : tripNumber2departureTime.keySet()) {
				if(eventTime >= tripNumber2departureTime.get(tripNr)) {
					if (tripNumber2departureTime.get(tripNr) >= maxDepTime) {
						tripNumber = tripNr;
					}
				}
			}
				
			double amountBefore = personId2tripNumber2amount.get(event.getAgentId()).get(tripNumber);
			double updatedAmount = amountBefore + amount;
			Map<Integer,Double> tripNumber2amount = personId2tripNumber2amount.get(event.getAgentId());
			tripNumber2amount.put(tripNumber, updatedAmount);
			personId2tripNumber2amount.put(event.getAgentId(), tripNumber2amount);
			
			// person-based analysis
			if (this.personId2amountSum.get(event.getAgentId()) == null) {
				this.personId2amountSum.put(event.getAgentId(), amount);
			} else {
				double amountSoFar = this.personId2amountSum.get(event.getAgentId());
				double amountNew = amountSoFar + amount;
				this.personId2amountSum.put(event.getAgentId(), amountNew);
			}
		}
	}
	
	@Override
	public void handleEvent(NoiseEventAffected event) {
//		log.info("NOISEEVENT: "+event.getAmount());
		if (!(useMoneyEvents == true)) {
			
			double amount = event.getAmount();
			
			// person-based analysis
			if (!(this.personId2amountSumAffected.containsKey(event.getAffectedAgentId()))) {
				this.personId2amountSumAffected.put(event.getAffectedAgentId(), amount);
			} else {
				double amountSoFar = this.personId2amountSumAffected.get(event.getAffectedAgentId());
				double amountNew = amountSoFar + amount;
				this.personId2amountSumAffected.put(event.getAffectedAgentId(), amountNew);
			}
		}
	}
	
	@Override
	public void handleEvent(PersonMoneyEvent event) {
//		log.info("MONEYEVENT: "+event.getAmount());
		if (useMoneyEvents == true) {
			
			log.info("###################");
			
			// trip-based analysis
			double amount = event.getAmount();
			double eventTime = event.getTime();
			int tripNumber = 0;
			double maxDepTime = 0.;
			Map<Integer,Double> tripNumber2departureTime = personId2tripNumber2departureTime.get(event.getPersonId());
			
			for(int tripNr : tripNumber2departureTime.keySet()) {
				if(eventTime >= tripNumber2departureTime.get(tripNr)) {
					if (tripNumber2departureTime.get(tripNr) >= maxDepTime) {
						tripNumber = tripNr;
					}
				}
			}
				
			double amountBefore = personId2tripNumber2amount.get(event.getPersonId()).get(tripNumber);
			double updatedAmount = amountBefore + amount;
			Map<Integer,Double> tripNumber2amount = personId2tripNumber2amount.get(event.getPersonId());
			tripNumber2amount.put(tripNumber, updatedAmount);
			personId2tripNumber2amount.put(event.getPersonId(), tripNumber2amount);
			
			// person-based analysis
			if (this.personId2amountSum.get(event.getPersonId()) == null) {
				this.personId2amountSum.put(event.getPersonId(), amount);
			} else {
				double amountSoFar = this.personId2amountSum.get(event.getPersonId());
				double amountNew = amountSoFar + amount;
				this.personId2amountSum.put(event.getPersonId(), amountNew);
			}
		}
	}
	
	@Override
	public void handleEvent(LinkEnterEvent event) {
		double linkLength = this.scenario.getNetwork().getLinks().get(event.getLinkId()).getLength();
		if(ptDrivers.contains(event.getVehicleId())){
			if(driverId2totalDistance.containsKey(event.getVehicleId())){
				driverId2totalDistance.put(event.getVehicleId(),driverId2totalDistance.get(event.getVehicleId()) + linkLength);
			} else {
				driverId2totalDistance.put(event.getVehicleId(),linkLength);
			}
		}else{
			// updating the trip Length
			int tripNumber = personId2actualTripNumber.get(event.getVehicleId());
			double distanceBefore = personId2tripNumber2tripDistance.get(event.getVehicleId()).get(tripNumber);
			double updatedDistance = distanceBefore + linkLength;
			Map<Integer,Double> tripNumber2tripDistance = personId2tripNumber2tripDistance.get(event.getVehicleId());
			tripNumber2tripDistance.put(tripNumber, updatedDistance);
			personId2tripNumber2tripDistance.put(event.getVehicleId(), tripNumber2tripDistance);
		}
	}
	
//	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	public Map<Id,List<Double>> getPersonId2listOfDepartureTimes(String mode) {
		Map<Id,List<Double>> personId2listOfDepartureTimes = new HashMap<Id, List<Double>>();
		for(Id personId: personId2tripNumber2departureTime.keySet()){
			List<Double> times = new ArrayList<Double>();
			for(int i : personId2tripNumber2departureTime.get(personId).keySet()){
				if(personId2tripNumber2legMode.get(personId).get(i).toString().equals(mode)){
					double time = personId2tripNumber2departureTime.get(personId).get(i);
					times.add(time);
				}else{
				}
			}
			personId2listOfDepartureTimes.put(personId, times);
		}
		return personId2listOfDepartureTimes;
	}
	
	public Map<Id,List<Double>> getPersonId2listOfDistances(String mode) {
		Map<Id,List<Double>> personId2listOfDistances = new HashMap<Id, List<Double>>();
		for(Id personId: personId2tripNumber2tripDistance.keySet()){
			List<Double> distances = new ArrayList<Double>();
			for(int i : personId2tripNumber2tripDistance.get(personId).keySet()){
				if(personId2tripNumber2legMode.get(personId).get(i).toString().equals(mode)){
					double distance = personId2tripNumber2tripDistance.get(personId).get(i);
					distances.add(distance);
				}else{
				}
			}
			personId2listOfDistances.put(personId, distances);
		}
		return personId2listOfDistances;
	}
	
	public Map<Id,List<Double>> getPersonId2listOfAmounts(String mode) {
		Map<Id,List<Double>> personId2listOfAmounts = new HashMap<Id, List<Double>>();
		for(Id personId: personId2tripNumber2amount.keySet()){
			List<Double> amounts = new ArrayList<Double>();
			for(int i : personId2tripNumber2amount.get(personId).keySet()){
				if(personId2tripNumber2legMode.get(personId).get(i).toString().equals(mode)){
					double amount = personId2tripNumber2amount.get(personId).get(i);
					
					amounts.add(amount);
				}else{
				}
			}
			personId2listOfAmounts.put(personId, amounts);
		}
		return personId2listOfAmounts;
	}
	
	public Map<Double, Double> getAvgAmountPerTripDepartureTime(String mode) {
		Map<Double, Double> tripDepTime2avgFare = new HashMap<Double, Double>();

		Map<Double, List<Double>> tripDepTime2fares = new HashMap<Double, List<Double>>();
		double startTime = this.timeBinSize;
		double periodLength = this.timeBinSize;
		double endTime = 30. * 3600;
		
		for (double time = startTime; time <= endTime; time = time + periodLength){
			List<Double> fares = new ArrayList<Double>();
			tripDepTime2fares.put(time, fares);
		}
		
		Map<Integer, double[]> counter2allDepartureTimesAndAmounts = new HashMap<Integer, double[]>();
		int i = 0;
		
		for(Id personId : personId2tripNumber2departureTime.keySet()){
			for(int tripNumber : personId2tripNumber2departureTime.get(personId).keySet()){
				if(personId2tripNumber2legMode.get(personId).get(tripNumber).toString().equals(mode)){
					double departureTime = personId2tripNumber2departureTime.get(personId).get(tripNumber);
					double belongingAmount = personId2tripNumber2amount.get(personId).get(tripNumber);
					double[] departureTimeAndAmount = new double[2];
					departureTimeAndAmount[0] = departureTime;
					departureTimeAndAmount[1] = belongingAmount;				
					counter2allDepartureTimesAndAmounts.put(i, departureTimeAndAmount);
					i++;
				}else{
				}
			}
		}
		
		for (Double time : tripDepTime2fares.keySet()){
			for (int counter : counter2allDepartureTimesAndAmounts.keySet()){
				if (counter2allDepartureTimesAndAmounts.get(counter)[0] < time && counter2allDepartureTimesAndAmounts.get(counter)[0] >= (time - periodLength)) {
					if (tripDepTime2fares.containsKey(time)){
						tripDepTime2fares.get(time).add(counter2allDepartureTimesAndAmounts.get(counter)[1]);
					}
				}
			}
		}
		
		for (Double time : tripDepTime2fares.keySet()){
			double amountSum = 0.;
			double counter = 0.;
			for (Double amount : tripDepTime2fares.get(time)){
				if (amount == null){
					
				} else {
					amountSum = amountSum + amount;
					counter++;
				}
			}
			
			double avgFare = 0.;
			if (counter!=0.){
				avgFare = (-1) * amountSum / counter;
			}
			tripDepTime2avgFare.put(time, avgFare);
		}
		return tripDepTime2avgFare;
	}
	
	public Map<Double, Double> getAvgAmountPerTripDistance(String mode) {
		Map<Double, Double> tripDistance2avgAmount = new HashMap<Double, Double>();

		Map<Double, List<Double>> tripDistance2amount = new HashMap<Double, List<Double>>();
		double startDistance = this.distance;
		double groupsize = this.distance;
		double endDistance = this.maxDistance;
		
		for (double distance = startDistance; distance <= endDistance; distance = distance + groupsize){
			List<Double> amounts = new ArrayList<Double>();
			tripDistance2amount.put(distance, amounts);
		}
		
		Map<Integer, double[]> counter2allDistancesAndAmounts = new HashMap<Integer, double[]>();
		int i = 0;
		
		for(Id personId : personId2tripNumber2tripDistance.keySet()){
			for(int tripNumber : personId2tripNumber2tripDistance.get(personId).keySet()){
				if(personId2tripNumber2legMode.get(personId).get(tripNumber).toString().equals(mode)){
					double tripDistance = personId2tripNumber2tripDistance.get(personId).get(tripNumber);
					double belongingAmount = personId2tripNumber2amount.get(personId).get(tripNumber);
					double[] tripDistanceAndAmount = new double[2];
					tripDistanceAndAmount[0] = tripDistance;
					tripDistanceAndAmount[1] = belongingAmount;				
					counter2allDistancesAndAmounts.put(i, tripDistanceAndAmount);
					i++;
				}
			}
		}
		
		for (Double dist : tripDistance2amount.keySet()){
			for (int counter : counter2allDistancesAndAmounts.keySet()){
				if (counter2allDistancesAndAmounts.get(counter)[0] < dist && counter2allDistancesAndAmounts.get(counter)[0] >= (dist - groupsize)) {
					if (tripDistance2amount.containsKey(dist)){
						tripDistance2amount.get(dist).add(counter2allDistancesAndAmounts.get(counter)[1]);
					}
				}
			}
		}
		
		for (Double dist : tripDistance2amount.keySet()){
			double amountSum = 0.;
			double counter = 0.;
			for (Double amount : tripDistance2amount.get(dist)){
				if (amount == null){
					
				} else {
					amountSum = amountSum + amount;
					counter++;
				}
			}
			
			double avgAmount = 0.;
			if (counter!=0.){
				avgAmount = (-1) * amountSum / counter;
			}
			tripDistance2avgAmount.put(dist, avgAmount);
		}
		return tripDistance2avgAmount;
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		
		if (this.persons.contains(event.getPersonId())){
			// do nothing
		} else {
			this.persons.add(event.getPersonId());
		}
		
		// A transit driver should not practice any activity,
		// otherwise the code has to be adapted here.
		if(ptDrivers.contains(event.getPersonId())){
			throw new RuntimeException("ActivityEndEvent by a transit-driver! The code has to be adapted.");
		}
		if(event.getActType().toString().equals("pt interaction")){
			// pt_interactions are not considered
		} else {
			if(personId2actualTripNumber.containsKey(event.getPersonId())){
				// The trip which starts immediately is at least the second trip of the person
				personId2actualTripNumber.put(event.getPersonId(), personId2actualTripNumber.get(event.getPersonId())+1);
				Map<Integer,Double> tripNumber2departureTime = personId2tripNumber2departureTime.get(event.getPersonId());
				tripNumber2departureTime.put(personId2actualTripNumber.get(event.getPersonId()), event.getTime());
				personId2tripNumber2departureTime.put(event.getPersonId(), tripNumber2departureTime);
				Map<Integer,Double> tripNumber2tripDistance = personId2tripNumber2tripDistance.get(event.getPersonId());
				tripNumber2tripDistance.put(personId2actualTripNumber.get(event.getPersonId()), 0.0);
				personId2tripNumber2tripDistance.put(event.getPersonId(), tripNumber2tripDistance);
					
				Map<Integer,Double> tripNumber2amount = personId2tripNumber2amount.get(event.getPersonId());
				tripNumber2amount.put(personId2actualTripNumber.get(event.getPersonId()), 0.0);
				personId2tripNumber2amount.put(event.getPersonId(), tripNumber2amount);
		
			} else {
				// The trip which starts immediately is the first trip of the person
				personId2actualTripNumber.put(event.getPersonId(), 1);
				Map<Integer,Double> tripNumber2departureTime = new HashMap<Integer, Double>();
				tripNumber2departureTime.put(1, event.getTime());
				personId2tripNumber2departureTime.put(event.getPersonId(), tripNumber2departureTime);
				Map<Integer,Double> tripNumber2tripDistance = new HashMap<Integer, Double>();
				tripNumber2tripDistance.put(1, 0.0);
				personId2tripNumber2tripDistance.put(event.getPersonId(), tripNumber2tripDistance);
				
				Map<Integer,Double> tripNumber2amount = new HashMap<Integer, Double>();
				tripNumber2amount.put(1, 0.0);
				personId2tripNumber2amount.put(event.getPersonId(), tripNumber2amount);
			}
		}	
	}
	
	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if(ptDrivers.contains(event.getPersonId())){
			// ptDrivers are not considered
		}else{
			// The leg mode has to be saved here.
			// The actual trip number has just been adapted before
			if(personId2tripNumber2legMode.containsKey(event.getPersonId())){
				// This is at least the second trip.
				int tripNumber = personId2actualTripNumber.get(event.getPersonId());
				Map<Integer,String> tripNumber2legMode = personId2tripNumber2legMode.get(event.getPersonId());
				if(tripNumber2legMode.containsKey(tripNumber)){
					// legMode already listed, possible for pt trips
					if(tripNumber2legMode.get(tripNumber).toString().equals("pt")){	
					} else{
						throw new RuntimeException("A leg mode has already been listed.");
					}
				} else {
					// the leg mode has to be saved.
					String legMode = event.getLegMode();
					if((event.getLegMode().toString().equals(TransportMode.transit_walk))){
						legMode = "pt";
					} else {
					}
					tripNumber2legMode.put(personId2actualTripNumber.get(event.getPersonId()), legMode);
					personId2tripNumber2legMode.put(event.getPersonId(), tripNumber2legMode);
				}
			} else {
				// This is the first trip of the person
				Map<Integer,String> tripNumber2legMode = new HashMap<Integer,String>();
				String legMode = event.getLegMode();
				if((event.getLegMode().toString().equals(TransportMode.transit_walk))){
					legMode = "pt";
				} else {
				}
				tripNumber2legMode.put(1, legMode);
				personId2tripNumber2legMode.put(event.getPersonId(), tripNumber2legMode);
			}
		}
	}
	
	@Override
	public void handleEvent(TransitDriverStartsEvent event) {
		if (ptDrivers.contains(event.getDriverId())) {
			// already listed
		} else {
			ptDrivers.add(event.getDriverId());
			driverId2totalDistance.put(event.getVehicleId(),0.0);
		}
	}
	
	@Override
	public void handleEvent(PersonLeavesVehicleEvent event) {
		if(ptDrivers.contains(event.getPersonId())){
			// ptDrivers are not considered
		} else {
			int tripNumber = personId2actualTripNumber.get(event.getPersonId());
			Map<Integer,String> tripNumber2legMode = personId2tripNumber2legMode.get(event.getPersonId());
			if((tripNumber2legMode.get(tripNumber)).equals(TransportMode.car)){
			// car drivers not considered here
			} else {
				double distanceTravelled = (driverId2totalDistance.get(event.getVehicleId()) - personId2distanceEnterValue.get(event.getPersonId())); 
				
				Map<Integer,Double> tripNumber2distance = personId2tripNumber2tripDistance.get(event.getPersonId());
				tripNumber2distance.put(tripNumber, tripNumber2distance.get(tripNumber) + distanceTravelled);
				
				personId2distanceEnterValue.remove(event.getPersonId());
			}
		}
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		if(ptDrivers.contains(event.getPersonId())){
			// ptDrivers are not considered
		} else {
			int tripNumber = personId2actualTripNumber.get(event.getPersonId());
			Map<Integer,String> tripNumber2legMode = personId2tripNumber2legMode.get(event.getPersonId());
			if((tripNumber2legMode.get(tripNumber)).equals(TransportMode.car)){
			// car drivers not considered here
			} else {
				personId2distanceEnterValue.put(event.getPersonId(), driverId2totalDistance.get(event.getVehicleId()));
			}
		}
	}

	public Map<Id, Double> getPersonId2amountSum() {
		return personId2amountSum;
	}
	
	public Map<Id, Double> getPersonId2amountSumAffected() {
		return personId2amountSumAffected;
	}
	
	public Map<Id, Double> getPersonId2amountSumAllAgents() {
		Map<Id, Double> personId2amountSumAllAgents = new HashMap<Id, Double>();
		
		List<Id> personIds = new ArrayList<Id>();
		if (this.scenario.getPopulation().getPersons().isEmpty()) {
			log.warn("Scenario does not contain a Population. Using the person IDs from the events file for the person-based analysis.");
			personIds.addAll(this.persons);
		} else {
			log.info("Scenario contains a Population. Using the person IDs from the population for the person-based analysis.");
			personIds.addAll(this.scenario.getPopulation().getPersons().keySet());
		}
		
		for (Id id : personIds) {
			double amountSum = 0.;
			if (this.personId2amountSum.get(id) == null) {
				// no monetary payments
			} else {
				amountSum = -1.0 * this.personId2amountSum.get(id);
			}
			personId2amountSumAllAgents.put(id, amountSum);
		}
		return personId2amountSumAllAgents;
	}
	
	public Map<Id, Double> getPersonId2amountSumAffectedAllAgents() {
		Map<Id, Double> personId2amountSumAffectedAllAgents = new HashMap<Id, Double>();
		
		List<Id> personIds = new ArrayList<Id>();
		if (this.scenario.getPopulation().getPersons().isEmpty()) {
			log.warn("Scenario does not contain a Population. Using the person IDs from the events file for the person-based analysis.");
			personIds.addAll(this.persons);
		} else {
			log.info("Scenario contains a Population. Using the person IDs from the population for the person-based analysis.");
			personIds.addAll(this.scenario.getPopulation().getPersons().keySet());
		}
		
		for (Id id : personIds) {
			double amountSum = 0.;
			if (!(this.personId2amountSumAffected.containsKey(id))) {
				// no monetary payments
			} else {
				amountSum = -1.0 * this.personId2amountSumAffected.get(id);
			}
			personId2amountSumAffectedAllAgents.put(id, amountSum);
		}
		return personId2amountSumAffectedAllAgents;
	}

}