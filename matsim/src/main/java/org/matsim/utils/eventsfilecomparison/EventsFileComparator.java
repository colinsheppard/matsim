/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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

package org.matsim.utils.eventsfilecomparison;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * This class checks if two events files are semantic equivalent. The order of the events does not matter as long as
 * they are chronological sorted.
 * @author laemmel
 *
 */
public class EventsFileComparator {

	private static final Logger log = Logger.getLogger(EventsFileComparator.class);

	private final String eFile1;
	private final String eFile2;

	private boolean finishedWorkerTimeStep = false;
	private boolean finishedWorker = false;
	private Worker w1;
	private Worker w2;

	private int retCode = -1;

	private boolean run = true;

	private boolean abort = false;
	public EventsFileComparator(String eFile1, String eFile2) {
		this.eFile1 = eFile1;
		this.eFile2 = eFile2;
	}

	public void run() {

		this.w1 = new Worker(this.eFile1,this);
		this.w2 = new Worker(this.eFile2,this);
		this.w1.start();
		this.w2.start();

		while (this.run) {
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}


		if (this.retCode == 0) {
			log.info("Event files are semantic equivalent.");

		} else {
			log.warn("Event files differ.");
		}
	}

	public int compareEvents() {
		this.run();
		return this.retCode;
	}


	synchronized boolean timeStepFinished(boolean evFinished){
		if (this.abort) {
			return false;
		}


		if (evFinished) {
			eventsFinished();
			return true;
		}


		if (this.finishedWorker) {
			log.warn("Events files have different number of time steps! Aborting!");
			abort(-1);
			return false;
		}



		if (this.finishedWorkerTimeStep) {
			if (this.w1.getCurrentTime() != this.w2.getCurrentTime()) {
				log.warn("Differnt time steps in event files! Aborting!");
				abort(-2);
				return false;
			}
			compareTimeStep();
			this.finishedWorkerTimeStep = false;
			return false;
		} else {
			this.finishedWorkerTimeStep = true;
		}
		return true;
	}

	private void compareTimeStep() {

		//we do not abort here because we want to know which event is missing
		if (this.w1.getNumEvents() < this.w2.getNumEvents()) {
			compare(this.w2,this.w1);
		} else  {
			compare(this.w1,this.w2);
		}
	}

	private void compare(Worker w1, Worker w2) {
		Map<String, Counter> map1 = w1.getEventsMap();
		Map<String, Counter> map2 = w2.getEventsMap();
		for (Entry<String, Counter> e : map1.entrySet()) {

			Counter c = map2.get(e.getKey());
			if (c == null) {
				log.warn("Missing event:" + e.getKey() + "\nin events file:" + w2.getEFile());
				abort(-3);
				return;
			}
			if (c.getCount() != e.getValue().getCount()) {
				log.warn("Wrong event count for: " + e.getKey() + "\n" + e.getValue().getCount() + " times in file:" + w1.getEFile()
						+ "\n" + c.getCount() + " times in file:" + w2.getEFile());
				abort(-4);
				return;
			}
		}
		map1.clear();
		map2.clear();
		this.w1.cont();
		this.w2.cont();
	}



	synchronized void abort(int errCode) {
		this.abort = true;
		this.retCode = errCode;
		this.run = false;
		this.w1.abort();
		this.w2.abort();
	}
	private void eventsFinished(){
		if ( this.finishedWorkerTimeStep) {
			log.warn("Events files have different number of time steps! Aborting!");
			abort(-1);
			return;
		}

		if (this.finishedWorker) {
			if (this.w1.getCurrentTime() != this.w2.getCurrentTime()) {
				log.warn("Differnt time steps in event files! Aborting!");
				abort(-2);
				return;
			}
			compareTimeStep();
			this.retCode = 0;
			this.run = false;
		} else {
			this.finishedWorker = true;
		}
	}

}
