/*
 * JGAAP -- a graphical program for stylometric authorship attribution
 * Copyright (C) 2009,2011 by Patrick Juola
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 **/
package com.jgaap.classifiers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.jgaap.JGAAPConstants;
import com.jgaap.generics.AnalyzeException;
import com.jgaap.generics.DistanceCalculationException;
import com.jgaap.generics.Event;
import com.jgaap.generics.EventHistogram;
import com.jgaap.generics.EventSet;
import com.jgaap.generics.NeighborAnalysisDriver;
import com.jgaap.generics.Pair;

/**
 * Assigns authorship labels by using a nearest-neighbor approach on a given
 * distance/divergence function.
 * 
 *
 * @author Michael Ryan
 * @since 5.0.0
 */

public class AuthorCentroidDriver extends NeighborAnalysisDriver {

	static Logger logger = Logger.getLogger(AuthorCentroidDriver.class);
	
	public String displayName() {
		return "Author Centroid Driver" + getDistanceName();
	}

	public String tooltipText() {
		return " ";
	}

	public boolean showInGUI() {
		return false;
	}

	@Override
	public List<Pair<String, Double>> analyze(EventSet unknown, List<EventSet> known) throws AnalyzeException {
		List<Pair<String, Double>> results = new ArrayList<Pair<String, Double>>();
		List<EventSet> knownCentroids = new ArrayList<EventSet>();
		Map<String, List<EventSet>> knownAuthors = new HashMap<String, List<EventSet>>();
		for (EventSet eventSet : known) {
			if (knownAuthors.containsKey(eventSet.getAuthor())) {
				knownAuthors.get(eventSet.getAuthor()).add(eventSet);
			} else {
				List<EventSet> tmp = new ArrayList<EventSet>();
				tmp.add(eventSet);
				knownAuthors.put(eventSet.getAuthor(), tmp);
			}
		}
		Set<Event> events = new HashSet<Event>();
		List<EventHistogram> histograms = new ArrayList<EventHistogram>();
		List<String> authors = new ArrayList<String>(knownAuthors.keySet());
		for (String author : authors) {
			EventSet centroid = new EventSet(author);
			// Writer writer = new BufferedWriter(new FileWriter(new
			// File(jgaapConstants.tmpDir()+ author + ".centroid")));
			double count = 0;
			EventHistogram hist = new EventHistogram();
			for (EventSet eventSet : knownAuthors.get(author)) {
				for (Event event : eventSet) {
					hist.add(event);
					events.add(event);
				}
				count++;
			}
			histograms.add(hist);
			for (Event event : hist) {
				// writer.write(event.getEvent()+"\t"+hist.getAbsoluteFrequency(event)/count+"\n");
				for (int i = 0; i < Math.round(hist.getAbsoluteFrequency(event) / count); i++) {
					centroid.addEvent(event);
				}
			}
			knownCentroids.add(centroid);
		}
		List<Event> orderedEvents = new ArrayList<Event>(events);

		try {
			Writer writer = new BufferedWriter(new FileWriter(new File(JGAAPConstants.JGAAP_TMPDIR+ "key.centroid")));
			for (Event event : orderedEvents) {
				writer.write(event.getEvent() + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int j = 0;
		for (EventHistogram hist : histograms) {
			try {
				Writer writer = new BufferedWriter(new FileWriter(new File(JGAAPConstants.JGAAP_TMPDIR+ authors.get(j)+".centroid")));
				for (Event event : orderedEvents) {
					writer.write(hist.getRelativeFrequency(event)+"\n");
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			j++;
		}
		EventHistogram unknownHist = new EventHistogram();
		for(Event event : unknown){
			unknownHist.add(event);
		}


		for (int i = 0; i < knownCentroids.size(); i++) {
			double current;
			try {
				current = distance.distance(unknown, knownCentroids.get(i));
			} catch (DistanceCalculationException e) {
				e.printStackTrace();
				throw new AnalyzeException("Distance "+distance.displayName()+" failed");
			}
			results.add(new Pair<String, Double>(knownCentroids.get(i).getAuthor(), current, 2));
			logger.debug(unknown.getDocumentName()+"(Unknown):"+knownCentroids.get(i).getDocumentName()+"("+knownCentroids.get(i).getAuthor()+") Distance:"+current);
		}
		Collections.sort(results);
		return results;
	}

}
