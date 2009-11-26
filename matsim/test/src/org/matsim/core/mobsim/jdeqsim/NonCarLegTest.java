package org.matsim.core.mobsim.jdeqsim;

import java.util.LinkedList;

import org.matsim.core.api.experimental.events.PersonEvent;
import org.matsim.core.events.ActivityEndEventImpl;
import org.matsim.core.events.ActivityStartEventImpl;
import org.matsim.core.events.AgentArrivalEventImpl;
import org.matsim.core.events.AgentDepartureEventImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.jdeqsim.util.TestHandlerDetailedEventChecker;
import org.matsim.core.population.PopulationImpl;
import org.matsim.testcases.MatsimTestCase;

public class NonCarLegTest extends TestHandlerDetailedEventChecker {

	public void test_EmptyCarRoute() {
		Gbl.reset();
		this.startTestDES("test/input/org/matsim/core/mobsim/jdeqsim/config2.xml", false, null, null);
	}
	
	public void checkAssertions(final PopulationImpl population) {
		// intentionally not executed, because this is checked in detail here
		// super.checkAssertions(population);
		boolean wasInLoop=false;
		
		for (LinkedList<PersonEvent> list : events.values()) {
			wasInLoop=true;
			assertTrue(list.get(0) instanceof ActivityEndEventImpl);
			assertTrue(list.get(1) instanceof AgentDepartureEventImpl);
			assertTrue(list.get(2) instanceof AgentArrivalEventImpl);
			assertTrue(list.get(3) instanceof ActivityStartEventImpl);
			
		}
		assertTrue(wasInLoop);
	}

}
