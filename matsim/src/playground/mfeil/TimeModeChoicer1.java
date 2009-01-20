/* *********************************************************************** *
 * project: org.matsim.*
 * TimeModeChoicer1.java
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
package playground.mfeil;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.matsim.basic.v01.BasicLeg;
import org.matsim.gbl.Gbl;
import org.matsim.planomat.costestimators.LegTravelTimeEstimator;
import org.matsim.population.Act;
import org.matsim.population.Leg;
import org.matsim.population.Plan;
import org.matsim.population.algorithms.PlanAnalyzeSubtours;
import org.matsim.scoring.PlanScorer;
import org.matsim.controler.Controler;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.FileNotFoundException;


/**
 * @author Matthias Feil
 * Like TimeOptimizer14 but first draft how to include also mode choice.
 */

public class TimeModeChoicer1 implements org.matsim.population.algorithms.PlanAlgorithm { 
	
	private final int						MAX_ITERATIONS, STOP_CRITERION, NEIGHBOURHOOD_SIZE;
	private int								OFFSET;
	private final double					minimumTime;
	private final PlanScorer 				scorer;
	private final LegTravelTimeEstimator	estimator;
	private static final Logger 			log = Logger.getLogger(TimeModeChoicer1.class);
	
	//////////////////////////////////////////////////////////////////////
	// Constructor
	//////////////////////////////////////////////////////////////////////
	
	public TimeModeChoicer1 (LegTravelTimeEstimator estimator, PlanScorer scorer){
		
		this.scorer 				= scorer;
		this.estimator				= estimator;
		this.OFFSET					= 1800;
		this.MAX_ITERATIONS 		= 30;
		this.STOP_CRITERION			= 5;
		this.minimumTime			= 3600;
		this.NEIGHBOURHOOD_SIZE		= 10;
		
		//TODO @MF: constants to be configured externally
	}
	
		
	//////////////////////////////////////////////////////////////////////
	// run() method
	//////////////////////////////////////////////////////////////////////
	
	public void run (Plan basePlan){
		// moved this piece of code to the very beginning
		if (basePlan.getActsLegs().size()==1) return;
		
		/* TODO: just as long as PlanomatXPlan exists. Needs then to be removed!!! */ 
		PlanomatXPlan plan = new PlanomatXPlan (basePlan.getPerson());
		plan.copyPlan(basePlan);
		
		// Initial clean-up of plan for the case actslegs is not sound.
		double move = this.cleanSchedule (((Act)(plan.getActsLegs().get(0))).getEndTime(), plan);
		int loops=1;
		while (move!=0.0){
			loops++;
			move = this.cleanSchedule(java.lang.Math.max(((Act)(plan.getActsLegs().get(0))).getEndTime()-move,this.minimumTime), plan);
			if (loops>3) {
				for (int i=2;i<plan.getActsLegs().size()-4;i+=2){
					((Act)plan.getActsLegs().get(i)).setDuration(this.minimumTime);
				}
				move = this.cleanSchedule(this.minimumTime, plan);
				if (move!=0.0){
					log.warn("No valid initial solution found for "+plan.getPerson().getId()+"!");
					plan.setScore(-10000);
					return;
				}
			}
		}
		// TODO Check whether allowed?
		plan.setScore(this.scorer.getScore(plan));	
		
		/* Analysis of subtours */
		// analyze plan: how many activities and subtours do we have?
		PlanAnalyzeSubtours planAnalyzeSubtours = new PlanAnalyzeSubtours();
		planAnalyzeSubtours.run(plan);
		
		// Initializing 
		int neighbourhood_size = 0;
		for (int i = plan.getActsLegs().size()-1;i>0;i=i-2){
			neighbourhood_size += i;
		}
		int [][] moves 									= new int [neighbourhood_size][2];
		int [] position									= new int [2];
		ArrayList<?> [] initialNeighbourhood 			= new ArrayList [neighbourhood_size];
		ArrayList<?> [] neighbourhood 					= new ArrayList [java.lang.Math.min(NEIGHBOURHOOD_SIZE, neighbourhood_size)];
		double []score					 				= new double [neighbourhood_size];
		ArrayList<?> bestSolution						= new ArrayList<Object>();
		int pointer;
		int currentIteration							= 1;
		int lastImprovement 							= 0;
		
		/*
		String outputfile = Controler.getOutputFilename("Timer_log"+Counter.timeOptCounter+"_"+plan.getPerson().getId()+".xls");
		Counter.timeOptCounter++;
		PrintStream stream;
		try {
			stream = new PrintStream (new File(outputfile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		stream.print(plan.getScore()+"\t");
		for (int z= 0;z<plan.getActsLegs().size();z=z+2){
		Act act = (Act)plan.getActsLegs().get(z);
			stream.print(act.getType()+"\t");
		}
		stream.println();
		stream.print("\t");
		for (int z= 0;z<plan.getActsLegs().size();z=z+2){
			stream.print(((Act)(plan.getActsLegs()).get(z)).getDuration()+"\t");
		}
		stream.println();
		*/
		
		// Copy the plan into all fields of the array neighbourhood
		for (int i = 0; i < initialNeighbourhood.length; i++){
			initialNeighbourhood[i] = this.copyActsLegs(plan.getActsLegs());
		}
		
		//Set the given plan as bestSolution
		bestSolution = this.copyActsLegs(plan.getActsLegs());
		double bestScore = plan.getScore();
		
		// Iteration 1
	//	stream.println("Iteration "+1);
		this.createInitialNeighbourhood((PlanomatXPlan)plan, initialNeighbourhood, score, moves, planAnalyzeSubtours);
		//for (int x=0;x<initialNeighbourhood.length;x++){
			//log.info("Oben "+x+" = "+((Leg)(initialNeighbourhood[x].get(1))).getDepartureTime());
		//}
		pointer = this.findBestSolution (initialNeighbourhood, score, moves, position);
				
		if (score[pointer]>bestScore){
			bestSolution = this.copyActsLegs((ArrayList<?>)initialNeighbourhood[pointer]);
			bestScore=score[pointer];
			lastImprovement = 0;
		}
		else {
			lastImprovement++;
		}
		for (int i = 0;i<neighbourhood.length; i++){
			neighbourhood[i] = this.copyActsLegs((ArrayList<?>)initialNeighbourhood[pointer]);
		}
		
		
		// Do Tabu Search iterations
		for (currentIteration = 2; currentIteration<=MAX_ITERATIONS;currentIteration++){
			
	//		stream.println("Iteration "+currentIteration);
			
			this.createNeighbourhood((PlanomatXPlan)plan, neighbourhood, score, moves, position, planAnalyzeSubtours);
			pointer = this.findBestSolution (neighbourhood, score, moves, position);
			
			if (pointer==-1) {
				log.info("No valid solutions found for person "+plan.getPerson().getId()+" at iteration "+currentIteration);
				break;
			}
		
			if (score[pointer]>bestScore){
				bestSolution = this.copyActsLegs((ArrayList<?>)neighbourhood[pointer]);
				bestScore=score[pointer];
				lastImprovement = 0;
			}
			else {
				lastImprovement++;
				if (lastImprovement > STOP_CRITERION) break;
				// NEW NEW NEW NEW NEW
				//if (changeInOffset)	break;
				//else {
				//	this.OFFSET /=2;
				//	changeInOffset = true;
				//}
			}
			
			if (this.MAX_ITERATIONS!=currentIteration){			
				for (int i = 0;i<neighbourhood.length; i++){
					neighbourhood[i] = this.copyActsLegs((ArrayList<?>)neighbourhood[pointer]);
				}
			}				
		}
	
		// Update the plan with the final solution 		
	//	stream.println("Selected solution\t"+bestScore);
		ArrayList<Object> al = basePlan.getActsLegs();
		basePlan.setScore(bestScore);
		
		/* TODO: remove this!! 
		ArrayList<Object> al = plan.getActsLegs();
		plan.setScore(bestScore);
		*/
		
		for (int i = 0; i<al.size();i++){
			if (i%2==0){
				((Act)al.get(i)).setDuration(((Act)(bestSolution.get(i))).getDuration());
				((Act)al.get(i)).setStartTime(((Act)(bestSolution.get(i))).getStartTime());
				((Act)al.get(i)).setEndTime(((Act)(bestSolution.get(i))).getEndTime());
			}
			else {
				((Leg)al.get(i)).setTravelTime(((Leg)(bestSolution.get(i))).getTravelTime());
				((Leg)al.get(i)).setDepartureTime(((Leg)(bestSolution.get(i))).getDepartureTime());
				((Leg)al.get(i)).setArrivalTime(((Leg)(bestSolution.get(i))).getArrivalTime());
				((Leg)al.get(i)).setMode(((Leg)(bestSolution.get(i))).getMode());
			}
		}
		
		// NEW NEW
		this.estimator.reset();
		
	}
	
	//////////////////////////////////////////////////////////////////////
	// Neighbourhood definition 
	//////////////////////////////////////////////////////////////////////
	
	public void createInitialNeighbourhood (PlanomatXPlan plan, ArrayList<?> [] neighbourhood, double[]score, int [][] moves,
			PlanAnalyzeSubtours planAnalyzeSubtours) {
		
		int pos = 0;
		for (int outer=0;outer<neighbourhood[0].size()-2;outer+=2){
			for (int inner=outer+2;inner<neighbourhood[0].size();inner+=2){
				
				score[pos]=this.increaseTime(plan, neighbourhood[pos], outer, inner, planAnalyzeSubtours);
				moves [pos][0]=outer;
				moves [pos][1]=inner;
				pos++;
			//	if (plan.getPerson().getId().toString().equals("10")) log.info("Oben: "+1+" = "+((Leg)(neighbourhood[pos-1].get(1))).getMode()+" und score = "+score[pos-1]+" und leg = "+((Leg)(neighbourhood[pos-1].get(1))).getDepartureTime());
				
				score[pos]=this.decreaseTime(plan, neighbourhood[pos], outer, inner);
				moves [pos][0]=inner;
				moves [pos][1]=outer;
				pos++;
				
			}
		}
	}
	
	
	public void createNeighbourhood (PlanomatXPlan plan, ArrayList<?> [] neighbourhood, double[]score, int[][] moves, int[]position,
			PlanAnalyzeSubtours planAnalyzeSubtours) {
		
		int pos = 0;
		int fieldLength = neighbourhood.length/3;
				
			for (int outer=java.lang.Math.max(position[0]-(fieldLength/2)*2,0);outer<position[0];outer+=2){
				score[pos]=this.decreaseTime(plan, neighbourhood[pos], outer, position[0]);
				moves [pos][0]=position[0];
				moves [pos][1]=outer;
				pos++;
			}
		
			OuterLoop1:
				for (int outer=position[0];outer<neighbourhood[0].size()-2;outer+=2){
					for (int inner=outer+2;inner<neighbourhood[0].size();inner+=2){
						score[pos]=this.increaseTime(plan, neighbourhood[pos], outer, inner, planAnalyzeSubtours);
						moves [pos][0]=outer;
						moves [pos][1]=inner;
						pos++;
						
						if (pos>=fieldLength) break OuterLoop1;
					}
				}
		
			for (int outer=java.lang.Math.max(position[1]-(fieldLength/2)*2,0);outer<position[1];outer+=2){
				
				if (outer!=position[0]){
					score[pos]=this.increaseTime(plan, neighbourhood[pos], outer, position[1], planAnalyzeSubtours);
					moves [pos][0]=outer;
					moves [pos][1]=position[1];
					pos++;
				}
			}
		
			OuterLoop2:
				for (int outer=position[1];outer<neighbourhood[0].size()-2;outer+=2){
					for (int inner=outer+2;inner<neighbourhood[0].size();inner+=2){
						score[pos]=this.decreaseTime(plan, neighbourhood[pos], outer, inner);
						moves [pos][0]=inner;
						moves [pos][1]=outer;
						pos++;
						
						if (pos>=fieldLength*2) break OuterLoop2;
					}
				}
		
		
			OuterLoop3:
				for (int outer=0;outer<neighbourhood[0].size()-2;outer=outer+2){
					for (int inner=outer+2;inner<neighbourhood[0].size();inner=inner+2){
						
						if (outer!=position[0]	&&	inner!=position[1]){
							if (position[0]<position[1]){
								score[pos]=this.increaseTime(plan, neighbourhood[pos], outer, inner, planAnalyzeSubtours);
								moves [pos][0]=outer;
								moves [pos][1]=inner;
								pos++;
								if (pos>neighbourhood.length-1) break OuterLoop3;
							}
							else if (inner!=position[0]	||	outer!=position[1]){
								score[pos]=this.increaseTime(plan, neighbourhood[pos], outer, inner, planAnalyzeSubtours);
								moves [pos][0]=outer;
								moves [pos][1]=inner;
								pos++;
								if (pos>neighbourhood.length-1) break OuterLoop3;
							}
						}
					
						if (inner!=position[0]	&&	outer!=position[1]){
							if (position[0]>position[1]){
								score[pos]=this.decreaseTime(plan, neighbourhood[pos], outer, inner);
								moves [pos][0]=inner;
								moves [pos][1]=outer;
								pos++;
								if (pos>neighbourhood.length-1) break OuterLoop3;
							}
							else if (outer!=position[0]	||	inner!=position[1]){
								score[pos]=this.decreaseTime(plan, neighbourhood[pos], outer, inner);
								moves [pos][0]=inner;
								moves [pos][1]=outer;
								pos++;
								if (pos>neighbourhood.length-1) break OuterLoop3;
							}
						}
					}
				}		
	}
	
	
	
	public double increaseTime(PlanomatXPlan plan, ArrayList<?> actslegs, int outer, int inner,
			PlanAnalyzeSubtours planAnalyzeSubtours){
		
		if ((((Act)(actslegs.get(inner))).getDuration()>=OFFSET+this.minimumTime)	||	
				(outer==0	&&	inner==actslegs.size()-1)	||
				(86400+((Act)(actslegs.get(0))).getEndTime()-((Act)(actslegs.get(actslegs.size()-1))).getStartTime())>OFFSET+this.minimumTime){
			
			// NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW
			ArrayList<?> actslegsResult = this.copyActsLegs(actslegs);
			if (Gbl.getConfig().planomat().getPossibleModes().length>0){
				double score=-100000;
				BasicLeg.Mode subtour1=Gbl.getConfig().planomat().getPossibleModes()[0];
				BasicLeg.Mode subtour2=Gbl.getConfig().planomat().getPossibleModes()[0];
				
				/* outer loop */
				for (int i=0;i<Gbl.getConfig().planomat().getPossibleModes().length;i++){
					boolean startFound = false;
					int start = -1;
					int stop1 = -1;
					for (int x=0;x<((int)(actslegs.size()/2));x++){
						if (planAnalyzeSubtours.getSubtourIndexation()[x]==planAnalyzeSubtours.getSubtourIndexation()[outer/2]){
							if (!startFound) {
								start = x*2;
								startFound = true;
							}
							stop1 = (x*2)+2;
							((Leg)(actslegs.get(x*2+1))).setMode(Gbl.getConfig().planomat().getPossibleModes()[i]);
						}
					}
					if (planAnalyzeSubtours.getSubtourIndexation()[outer/2]!=planAnalyzeSubtours.getSubtourIndexation()[(inner/2)-1]){
						/* inner loop */
						for (int j=0;j<Gbl.getConfig().planomat().getPossibleModes().length;j++){
							int stop2 = -1;
							for (int x=0;x<((int)(actslegs.size()/2));x++){
								if (planAnalyzeSubtours.getSubtourIndexation()[x]==planAnalyzeSubtours.getSubtourIndexation()[inner/2-1]){
									if ((x*2)<start) start = x*2;
									stop2 = (x*2)+2;
									((Leg)(actslegs.get(x*2+1))).setMode(Gbl.getConfig().planomat().getPossibleModes()[j]);
								}
							}
							ArrayList<?> actslegsInput = this.copyActsLegs(actslegs);
							double tmpscore = this.setTimes(plan, actslegsInput, this.OFFSET, outer, inner, start, java.lang.Math.max(stop1, stop2));
							//log.info(start+" "+outer+" "+inner+" "+stop2+" "+stop1);
							//if (plan.getPerson().getId().toString().equals("110")) log.info("Mitte1: "+1+" = "+((Leg)(actslegsInput.get(1))).getDepartureTime());
							if (tmpscore>score) {
								score = tmpscore;
								subtour1 = Gbl.getConfig().planomat().getPossibleModes()[i];
								subtour2 = Gbl.getConfig().planomat().getPossibleModes()[j];
								actslegsResult = this.copyActsLegs(actslegsInput);
								//if (plan.getPerson().getId().toString().equals("110")) log.info("Mitte2: "+1+" = "+((Leg)(actslegsResult.get(1))).getDepartureTime());
							}
						}
					}
					else {
						ArrayList<?> actslegsInput = this.copyActsLegs(actslegs);
						double tmpscore = this.setTimes(plan, actslegsInput, this.OFFSET, outer, inner, start, stop1);
						//log.info(start+" "+outer+" "+inner+" "+stop1);
						if (tmpscore>score) {
							score = tmpscore;
							subtour1 = Gbl.getConfig().planomat().getPossibleModes()[i];
							actslegsResult = this.copyActsLegs(actslegsInput);
							//if (plan.getPerson().getId().toString().equals("110")) log.info("Mitte3: "+1+" = "+((Leg)(actslegsResult.get(1))).getDepartureTime());
							
						}
					}
				}
				/*
				if (plan.getPerson().getId().toString().equals("10")){
					if (subtour1.toString()=="walk" || subtour2.toString()=="walk") log.info("Subtour walk!");
					if (subtour1.toString()=="pt" || subtour2.toString()=="pt") log.info("Subtour pt!");
				}
				*/
				for (int z=1;z<actslegs.size();z+=2){
					((Leg)(actslegs.get(z))).setDepartureTime(((Leg)(actslegsResult.get(z))).getDepartureTime());
					((Leg)(actslegs.get(z))).setTravelTime(((Leg)(actslegsResult.get(z))).getTravelTime());
					((Leg)(actslegs.get(z))).setArrivalTime(((Leg)(actslegsResult.get(z))).getArrivalTime());
				}
				for (int x=0;x<((int)(actslegs.size()/2));x++){
					if (planAnalyzeSubtours.getSubtourIndexation()[x]==planAnalyzeSubtours.getSubtourIndexation()[outer/2]){
						((Leg)(actslegs.get(x*2+1))).setMode(subtour1);
						continue;
					}
					if (planAnalyzeSubtours.getSubtourIndexation()[outer/2]!=planAnalyzeSubtours.getSubtourIndexation()[inner/2-1]){
						if (planAnalyzeSubtours.getSubtourIndexation()[x]==planAnalyzeSubtours.getSubtourIndexation()[inner/2-1]){
							((Leg)(actslegs.get(x*2+1))).setMode(subtour2);
						}
					}
				}
			//	if (plan.getPerson().getId().toString().equals("10")) log.info("Ende: "+1+" = "+((Leg)(actslegs.get(1))).getMode()+" und score = "+score+" und leg = "+((Leg)(actslegsResult.get(1))).getDepartureTime());
				return score;
			}
			else return this.setTimes(plan, actslegs, OFFSET, outer, inner, outer, inner);
		}
		else return this.swapDurations (plan, actslegs, outer, inner);
	}
	
	
	
	public double decreaseTime(PlanomatXPlan plan, ArrayList<?> actslegs, int outer, int inner){
		
		double time = OFFSET+this.minimumTime;
		if (outer==0) time = OFFSET+1;
		if (((Act)(actslegs.get(outer))).getDuration()>=time){
			return this.setTimes(plan, actslegs, (-1)*OFFSET, outer, inner, outer, inner);
		}
		// NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW
		else return this.swapDurations(plan, actslegs, outer, inner);
	}
	
	
	
	// NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW
	public double swapDurations (PlanomatXPlan plan, ArrayList<?> actslegs, int outer, int inner){
		
		double swaptime= java.lang.Math.max(((Act)(actslegs.get(inner))).getDuration(), this.minimumTime);
		return this.setTimes(plan, actslegs, swaptime, outer, inner, outer, inner);
	}
	
	//////////////////////////////////////////////////////////////////////
	// Help methods 
	//////////////////////////////////////////////////////////////////////
	
	
	public int findBestSolution (ArrayList<?> [] neighbourhood, double[] score, int [][] moves, int[]position){
				
		int pointer=-1;
		ArrayList<?> actslegs = new ArrayList<Object>();
		double firstScore =-100000;
		for (int i=0;i<neighbourhood.length;i++){					
			if (score[i]>firstScore){
				actslegs = neighbourhood[i];
				firstScore=score[i];
				pointer=i;
				position[0]=moves[i][0];
				position[1]=moves[i][1];
			}
			/*
			stream.print(score[i]+"\t"+((Leg)(neighbourhood[i].get(1))).getDepartureTime()+"\t");
			stream.print(((Leg)(neighbourhood[i].get(1))).getMode()+"\t");
			for (int z= 2;z<neighbourhood[i].size()-1;z=z+2){
				stream.print((((Leg)(neighbourhood[i].get(z+1))).getDepartureTime()-((Leg)(neighbourhood[i].get(z-1))).getArrivalTime())+"\t");
				stream.print(((Leg)(neighbourhood[i].get(z+1))).getMode()+"\t");
			}
			stream.print(86400-((Leg)(neighbourhood[i].get(neighbourhood[i].size()-2))).getArrivalTime()+"\t");
			stream.println();
			*/
		}
	//	stream.println("Iteration's best score\t"+firstScore);
		
		// clean-up of plan (=bestIterSolution)
		if (pointer!=-1) this.cleanActs(actslegs);
		
		return pointer;
	}
	
	
	public double cleanSchedule (double now, Plan plan){
		
		((Act)(plan.getActsLegs().get(0))).setEndTime(now);
		((Act)(plan.getActsLegs().get(0))).setDuration(now);
			
		double travelTime;
		for (int i=1;i<=plan.getActsLegs().size()-2;i=i+2){
			((Leg)(plan.getActsLegs().get(i))).setDepartureTime(now);
			travelTime = this.estimator.getLegTravelTimeEstimation(plan.getPerson().getId(), now, (Act)(plan.getActsLegs().get(i-1)), (Act)(plan.getActsLegs().get(i+1)), (Leg)(plan.getActsLegs().get(i)));
			((Leg)(plan.getActsLegs().get(i))).setArrivalTime(now+travelTime);
			((Leg)(plan.getActsLegs().get(i))).setTravelTime(travelTime);
			now+=travelTime;
			
			if (i!=plan.getActsLegs().size()-2){
				((Act)(plan.getActsLegs().get(i+1))).setStartTime(now);
				travelTime = java.lang.Math.max(((Act)(plan.getActsLegs().get(i+1))).getDuration()-travelTime, this.minimumTime);
				((Act)(plan.getActsLegs().get(i+1))).setDuration(travelTime);	
				((Act)(plan.getActsLegs().get(i+1))).setEndTime(now+travelTime);	
				now+=travelTime;
			}
			else {
				((Act)(plan.getActsLegs().get(i+1))).setStartTime(now);
				/* NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW*/
				if (86400>now+this.minimumTime){
					((Act)(plan.getActsLegs().get(i+1))).setDuration(86400-now);
					((Act)(plan.getActsLegs().get(i+1))).setEndTime(86400);
				}
				else if (86400+((Act)(plan.getActsLegs().get(0))).getDuration()>now+this.minimumTime){
					if (now<86400){
						((Act)(plan.getActsLegs().get(i+1))).setDuration(86400-now);
						((Act)(plan.getActsLegs().get(i+1))).setEndTime(86400);
					}
					else {
					((Act)(plan.getActsLegs().get(i+1))).setDuration(this.minimumTime);
					((Act)(plan.getActsLegs().get(i+1))).setEndTime(now+this.minimumTime);
					}
				}
				else {
					return (now+this.minimumTime-(86400+((Act)(plan.getActsLegs().get(0))).getDuration()));
				}
			}
		}
		return 0;
	}
		

	public void cleanActs (ArrayList<?> actslegs){
		
		((Act)(actslegs.get(0))).setEndTime(((Leg)(actslegs.get(1))).getDepartureTime());
		((Act)(actslegs.get(0))).setDuration(((Leg)(actslegs.get(1))).getDepartureTime());
		
		for (int i=2;i<=actslegs.size()-1;i=i+2){
			
			if (i!=actslegs.size()-1){
				((Act)(actslegs.get(i))).setStartTime(((Leg)(actslegs.get(i-1))).getArrivalTime());
				((Act)(actslegs.get(i))).setEndTime(((Leg)(actslegs.get(i+1))).getDepartureTime());
				((Act)(actslegs.get(i))).setDuration(((Leg)(actslegs.get(i+1))).getDepartureTime()-((Leg)(actslegs.get(i-1))).getArrivalTime());
				if (((Act)(actslegs.get(i))).getDuration()<this.minimumTime-2) log.warn("duration < minimumTime: "+((Act)(actslegs.get(i))).getDuration()+"; Pos = "+i+" von = "+(actslegs.size()-1));
			}
			else {
				((Act)(actslegs.get(i))).setStartTime(((Leg)(actslegs.get(i-1))).getArrivalTime());
				if (((Leg)(actslegs.get(i-1))).getArrivalTime()>86400){
					((Act)(actslegs.get(i))).setDuration(0);
					//((Act)(actslegs.get(i))).setStartTime(((Leg)(actslegs.get(i-1))).getArrivalTime());
					((Act)(actslegs.get(i))).setEndTime(((Act)(actslegs.get(i))).getStartTime()); // new
				}
				else {
					((Act)(actslegs.get(i))).setDuration(86400-((Leg)(actslegs.get(i-1))).getArrivalTime());
					((Act)(actslegs.get(i))).setEndTime(86400);
				}
			}
		}
	}

	
	public ArrayList<Object> copyActsLegs (ArrayList<?> in){
		
			ArrayList<Object> out = new ArrayList<Object>();
			
			for (int i= 0; i< in.size() ; i++) {
				try {
					if (i % 2 == 0) {
						// Activity
						Act a = new Act ((Act)in.get(i));
						out.add(a);
					} else {
						// Leg
						Leg inl = ((Leg) in.get(i));
						Leg l = new Leg (inl.getMode());
						l.setArrivalTime(inl.getArrivalTime());
						l.setDepartureTime(inl.getDepartureTime());
						l.setTravelTime(inl.getTravelTime());
						l.setRoute(inl.getRoute());
						out.add(l);
					}
				} catch (Exception e) {
					Gbl.errorMsg(e);
				}
			}
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private double setTimes (PlanomatXPlan plan, ArrayList<?> actslegs, double offset, int outer, int inner, int start, int stop){		
		double travelTime;
		double now = ((Leg)(actslegs.get(start+1))).getDepartureTime();
		int position = 0;	// indicates whether time setting has reached parameter "stop"
		
		/* if start < outer (mode choice) */
		for (int i=start+1;i<=outer-1;i+=2){
			((Leg)(actslegs.get(i))).setDepartureTime(now);
			travelTime = this.estimator.getLegTravelTimeEstimation(plan.getPerson().getId(), now, (Act)(actslegs.get(i-1)), (Act)(actslegs.get(i+1)), (Leg)(actslegs.get(i)));
			((Leg)(actslegs.get(i))).setArrivalTime(now+travelTime);
			((Leg)(actslegs.get(i))).setTravelTime(travelTime);
			//now+=travelTime+((Act)(actslegs.get(i+1))).getDuration();
			now = java.lang.Math.max(now+travelTime+this.minimumTime, ((Act)(actslegs.get(i+1))).getEndTime());
		}
		
		/* standard process */
		for (int i=outer+1;i<=inner-1;i+=2){
			if (i==outer+1) {
				now +=offset;
			}
			((Leg)(actslegs.get(i))).setDepartureTime(now);
			//if (plan.getPerson().getId().toString().equals("110")) log.info(i+" = "+((Leg)(actslegs.get(i))).getDepartureTime());
			travelTime = this.estimator.getLegTravelTimeEstimation(plan.getPerson().getId(), now, (Act)(actslegs.get(i-1)), (Act)(actslegs.get(i+1)), (Leg)(actslegs.get(i)));
			((Leg)(actslegs.get(i))).setArrivalTime(now+travelTime);
			((Leg)(actslegs.get(i))).setTravelTime(travelTime);
			now+=travelTime;
			
			if (i!=inner-1){
				//now+=((Act)(actslegs.get(i+1))).getDuration();
				now = java.lang.Math.max(now+this.minimumTime, (((Act)(actslegs.get(i+1))).getEndTime()+offset));
				if (((Act)(actslegs.get(i+1))).getDuration()<this.minimumTime-2) log.warn("Eingehende duration < minimumTime! "+((Act)(actslegs.get(i+1))).getDuration());
			}
			else {
				double time1 = ((Act)(actslegs.get(i+1))).getEndTime();
				if (inner==actslegs.size()-1) {
					time1=((Leg)(actslegs.get(1))).getDepartureTime()+86400;
				}
				position = inner;
				if (time1<now+this.minimumTime){	// check whether act "inner" has at least minimum time
					if (actslegs.size()>=i+3){
						now+=this.minimumTime;
						((Leg)(actslegs.get(i+2))).setDepartureTime(now);
						travelTime = this.estimator.getLegTravelTimeEstimation(plan.getPerson().getId(), now, (Act)(actslegs.get(i+1)), (Act)(actslegs.get(i+3)), (Leg)(actslegs.get(i+2)));
						((Leg)(actslegs.get(i+2))).setArrivalTime(now+travelTime);
						((Leg)(actslegs.get(i+2))).setTravelTime(travelTime);
						now+=travelTime;
						double time2 = ((Act)(actslegs.get(i+3))).getEndTime();
						if (i+3==actslegs.size()-1) {
							time2=((Leg)(actslegs.get(1))).getDepartureTime()+86400;
						}
						position = i+3;
						if (time2<now+this.minimumTime){
							return -100000;
						}
					}
					else return -100000;
				}
			}
		}
		
		/* if position < stop (mode choice) */
		if (position < stop){
			now = ((Leg)(actslegs.get(position+1))).getDepartureTime();
			for (int i=position+1;i<=stop-1;i+=2){
				((Leg)(actslegs.get(i))).setDepartureTime(now);
				travelTime = this.estimator.getLegTravelTimeEstimation(plan.getPerson().getId(), now, (Act)(actslegs.get(i-1)), (Act)(actslegs.get(i+1)), (Leg)(actslegs.get(i)));
				((Leg)(actslegs.get(i))).setArrivalTime(now+travelTime);
				((Leg)(actslegs.get(i))).setTravelTime(travelTime);
				//now+=travelTime+((Act)(actslegs.get(i+1))).getDuration();
				now+=travelTime;
				now = java.lang.Math.max(now+this.minimumTime, ((Act)(actslegs.get(i+1))).getEndTime());
				if (i+1==actslegs.size()-1){
					double time=((Leg)(actslegs.get(1))).getDepartureTime()+86400;
					if (time<now){
						return -100000;
					}
				}
				else {
					if (now>((Act)(actslegs.get(i+1))).getEndTime()){
						((Leg)(actslegs.get(i+2))).setDepartureTime(now);
						travelTime = this.estimator.getLegTravelTimeEstimation(plan.getPerson().getId(), now, (Act)(actslegs.get(i+1)), (Act)(actslegs.get(i+3)), (Leg)(actslegs.get(i+2)));
						((Leg)(actslegs.get(i+2))).setArrivalTime(now+travelTime);
						((Leg)(actslegs.get(i+2))).setTravelTime(travelTime);
						now+=travelTime;
						double time3 = ((Act)(actslegs.get(i+3))).getEndTime();
						if ((i+3)==actslegs.size()-1) {
							time3=((Leg)(actslegs.get(1))).getDepartureTime()+86400;
						}
						if (time3<now+this.minimumTime){
							return -100000;
						}
					}
				}
			}
		}
		
		
		/* Scoring */
		plan.setActsLegs((ArrayList<Object>)actslegs);
		return scorer.getScore(plan);
	}
	
	
	/*
	public double getOffset (){
		return this.OFFSET;
	}
	 */
}
	

	
