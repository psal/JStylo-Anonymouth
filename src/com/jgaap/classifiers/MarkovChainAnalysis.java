/**
 * 
 */
package com.jgaap.classifiers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.jgaap.generics.AnalysisDriver;
import com.jgaap.generics.Event;
import com.jgaap.generics.EventSet;
import com.jgaap.generics.Pair;

/**
 * @author Darren Vescovi
 *
 */
public class MarkovChainAnalysis extends AnalysisDriver{

	
	@Override
	public String displayName() {
		
		return "Markov Chain Analysis";
	}

	@Override
	public boolean showInGUI() {
		
		return true;
	}

	@Override
	public String tooltipText() {
		
		return "First Order Markov Chain Analysis";
	}
	
	@Override
	public List<Pair<String, Double>> analyze(EventSet unknown, List<EventSet> known) {
		
		
		List<Pair<String, Double>> results = new ArrayList<Pair<String, Double>>();
		
		Iterator<EventSet> setIt = known.iterator();
		
		//loop throught the known and assign a probability to the unknown set using
		//a transition probability matrix built from each known event set.
		while(setIt.hasNext()){
			Hashtable<Event, Hashtable<Event, Double>> matrix = new Hashtable<Event, Hashtable<Event, Double>>();
			Hashtable<Event, Hashtable<Event, Double>> probMatrix = new Hashtable<Event, Hashtable<Event, Double>>();
			
			EventSet ev = setIt.next();
			
			//Iterate over events to create a matrix with 
			//counts of each time a event pair sequence 
			//appears.
			Iterator<Event> eventIt = ev.iterator();
			if(eventIt.hasNext()){
				//get the first event
				Event e1 = eventIt.next();
				while(eventIt.hasNext()){
					//get the next event
					Event e2 = eventIt.next();
					if(matrix.containsKey(e1)){
						if(matrix.get(e1).containsKey(e2)){
							//find out if the event sequence is already in the matrix
							//if so increment the count by 1;
							double tmp = matrix.get(e1).get(e2).doubleValue();
							matrix.get(e1).remove(e2);
							matrix.get(e1).put(e2, new Double(tmp+1));
							
						}
						else{
							//add the new sequence provided the first event is already
							//in the matrix
							matrix.get(e1).put(e2, new Double(1));
						}
					}else{
						//add the new sequence to matrix
						matrix.put(e1, new Hashtable<Event, Double>());
						matrix.get(e1).put(e2, new Double(1));
					}
					//reassign e1 to be e2				
					e1=e2;
					
				}	
			}
			
			//TODO calculate the probabilities of each event pair sequence to
			//create the Markov chain.
			
			//Get the row totals i.e. total of the double values from the second hashtable
			//then divide each row entry by its row total to obtain a valid transition probability matrix
			
			Set<Event> keys = matrix.keySet();
			Iterator<Event> keyIt = keys.iterator();
			
			while(keyIt.hasNext()){
				
				Event event = keyIt.next();
				
				probMatrix.put(event, new Hashtable<Event, Double>());
				Set<Event> keys2 = matrix.get(event).keySet();
				Iterator<Event> keys2It = keys2.iterator();
				double rowTotal =0;
				while(keys2It.hasNext()){
					//Get the row totals.
					Event event2 = keys2It.next();
					rowTotal += matrix.get(event).get(event2).doubleValue();
				}
				
				keys2It = keys2.iterator();
				
				while(keys2It.hasNext()){
					//divide each row entry by the row total to obtain a valid 
					//transition probability matrix.
					Event event2 = keys2It.next();
					
					double tmp = matrix.get(event).get(event2).doubleValue();
					//System.out.println(tmp.doubleValue());
					tmp = tmp /rowTotal;
					
					
					probMatrix.get(event).put(event2, new Double(tmp));
					 
					
				}
				
				
			}
			
			//now assign probability to the unknown event set using the newly
			//constructed transition probability matrix obtained above
			
			Iterator<Event> unknownIt = unknown.iterator();
			Event event1;
			double prob=0;
			if(unknownIt.hasNext()){
				event1 = unknownIt.next();
			
				while(unknownIt.hasNext()){
					Event event2 = unknownIt.next();
					//System.out.println(event1.toString()+event2.toString());
					if(probMatrix.containsKey(event1)){
						if(probMatrix.get(event1).containsKey(event2)){
							
							//use the negative log(base e) sum since multiplication will
							//almost always result in a probability of 0;\
							Double tmp = probMatrix.get(event1).get(event2);
							System.out.println(tmp);
							prob -= Math.log(tmp);
							//System.out.println(prob+"-----------");
						}
					}
					
					//set event1 equal to event2
					event1=event2;
									
				}
			
			
			}
			
			//assign the probability to the current known document author
			
			results.add(new Pair<String, Double>(ev.getAuthor(), prob));
			
			
		}
		
		
		//return the results
		Collections.sort(results);
		
		
		return results;
	}


}