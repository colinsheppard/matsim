package playground.anhorni.locationchoice.preprocess.plans.modifications;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.PopulationReader;

public class AddBorderCrossingTraffic {
	
	PopulationImpl oldPlans = new PopulationImpl();
	private final static Logger log = Logger.getLogger(AddBorderCrossingTraffic.class);
	
	public void run(Population plans, NetworkLayer network) {
		this.init(network);
		this.assignTTA(plans);
	}

	private void init(NetworkLayer network) {	
		final PopulationReader plansReader = new MatsimPopulationReader(this.oldPlans, network);
		plansReader.readFile("input/plans/bordercrossing/plans.xml.gz");
	}
	
	private void assignTTA(Population plans) {
		
		int cnt = 0;
		
		Iterator<PersonImpl> person_it = this.oldPlans.getPersons().values().iterator();
		while (person_it.hasNext()) {
			PersonImpl person = person_it.next();
			Plan plan = person.getSelectedPlan();
			
			List<? extends PlanElement> actslegs = plan.getPlanElements();			
			for (int j = 0; j < actslegs.size(); j=j+2) {			
				final Activity act = (Activity)actslegs.get(j);
				
				
				//if (act.getType().startsWith("tta") || act.getType().startsWith("shop")) {
				if (act.getType().startsWith("tta")) {
					String id = "20" + person.getId().toString();
					person.setId(new IdImpl(id));
					person.createDesires("tta");
					person.getDesires().putActivityDuration("tta", 8 * 3600);
					((PopulationImpl)plans).addPerson(person);
					cnt++;
					
					break;
				}
			}
		}
		log.info("Added " + cnt + " persons crossing the border");
	}
}
