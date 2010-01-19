/* Copyright (c) 2007-2009, Computer Engineering and Networks Laboratory (TIK), ETH Zurich.
*  All rights reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions
*  are met:
*
*  1. Redistributions of source code must retain the above copyright
*     notice, this list of conditions and the following disclaimer.
*  2. Redistributions in binary form must reproduce the above copyright
*     notice, this list of conditions and the following disclaimer in the
*     documentation and/or other materials provided with the distribution.
*  3. Neither the name of the copyright holders nor the names of
*     contributors may be used to endorse or promote products derived
*     from this software without specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS `AS IS'
*  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
*  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
*  ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS
*  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
*  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, LOSS OF USE, DATA,
*  OR PROFITS) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
*  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
*  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
*  THE POSSIBILITY OF SUCH DAMAGE.
*
*  @author Philipp Sommer <phsommer@users.sourceforge.net>
* 
*/

package mobility;


import java.util.*;


import simulator.*;
import event.*;
import model.*;


/**
 * This abstract class represents a mobile node in an ad-hoc network.
 * The mobility of a node is generated by an implementation of a mobility model.
 * @author psommer
 *
 */
public abstract class MobileNode extends GraphNode{

	/** unique id for this node */
	public int id = 0;
	/** indicates if the node is currently participating in the simulation */
	public boolean participating = false;
	/** position on road */
	public double position = 0;
	/** current road */
	public RoadEdge road = null;
	/** current speed */
	public double speed = 0;
	/** current direction (if moving) */
	public double direction = 0;
	/** list with generated events for this node */
	protected LinkedList<Event> events = new LinkedList<Event>();
	/** currently active event */
	protected Event currentEvent = null;
	/** last event in the event queue */
	protected Event lastEvent = null;
	/** end time of the last event in the queue */
	protected double lastEventEndTime = 0;
	
	/** table of contacts for this node */
	public Hashtable<Integer, Double> contacts = new Hashtable<Integer, Double>();
	/** set with current neighbors of this node */
	public HashSet<MobileNode> currentNeighbors = new HashSet<MobileNode>();
	/** number of new neighbors since the last sample time */
	public int neighborsNew = 0;
	/** number of neighbors lost since the last sample time */
	public int neighborsLost = 0;
	
	/** time when this node joined the simulation */
	public double joinTime = Double.NEGATIVE_INFINITY;
	/** time when this node left the simulation */
	public double leaveTime = Double.POSITIVE_INFINITY;
	
	/** members used for the Dijkstra shortest path algorithm */
	public int dijkstraCost = Integer.MAX_VALUE;
	public boolean dijkstraVisited = false;
	
	/**
	 * Constructs a new Node
	 * @param id unique identifier for this node. This identifier has to be unique in the simulation.
	 */
	public MobileNode(int id) {
		this.id = id;
		leaveTime = Simulator.duration;
		lastEventEndTime = 0;
	}
	
	/**
	 * Returns the unique identifier for this node.
	 * @return Unique node identifier
	 */
	public int getId() {
		return id;
	}
	
	
	/**
	 * Initializes the node
	 */
	public abstract void init();
	
	
	/**
	 * Inserts a new event into the node's list of events.
	 * @param event
	 */
	public void addEvent(Event event) {
		
		/*
		if (event.time<lastEventEndTime) {
			System.err.println("Event starts before the previous event is finished!");
			System.err.println("previousEvent finish: " + lastEventEndTime + " this event starts: " + event.time);
		}
		*/
		// add event to the internal event queue
		events.addLast(event);
		// update the reference to the last event
		lastEvent = event;
		// update the end time of the current event
		lastEventEndTime = event.time + event.duration;
		// inform the simulation about this event
		Simulator.addEvent(event);
		
	}
	
	/**
	 * Prepares the node for the next simulation step.
	 */
	public abstract void prepare();
	
	
	/**
	 * Updates the current node position for the next sample time based on the node's list of events.
	 * @return Returns true if everything went fine
	 */
	public boolean next() {
		
		
		
		// get the first event in the list
		Event firstEvent = events.peek();
		
		while (firstEvent!=null && firstEvent.time<=Simulator.time) {
		
			// proceed with the next event 
			currentEvent = events.poll();
			
			if (currentEvent.type==Event.JOIN) {
				// add node to the simulation
				Simulator.addNode(currentEvent.time, this);
				participating = true;
				joinTime = currentEvent.time;
			} else if (currentEvent.type==Event.LEAVE) {
				// remove node from the simulation
				leaveTime = currentEvent.time;
				Simulator.removeNode(currentEvent.time, this);
				participating = false;
				currentEvent = null;
				
			} else {
				// other events need not to be handled specially here
			}
			// check if there is another event in the queue
			firstEvent = events.peek();
		}
		
		
		// return false if there is no current ongoing event for this node or the node is not participating
		if (currentEvent==null || !participating) return false;
		
		
		// process current event
		if (currentEvent.type==Event.MOVE) {
			// node is moving
			
			Move movement = (Move) currentEvent;
				
			if (Simulator.time<currentEvent.time + movement.duration) {
				// node is moving
				double fraction = (Simulator.time - movement.time)/movement.duration;
					
				// update the current node position, velocity and direction
				x = fraction*(movement.moveToX-movement.x) + movement.x;
				y = fraction*(movement.moveToY-movement.y) + movement.y;
				speed = movement.velocity;
				direction = movement.direction;
			} else {
				// node has reached the destination
				x = movement.moveToX;
				y = movement.moveToY;
				speed = 0;
				direction = 0;
			}
				
				
		} else {
				// node is not moving
				x = currentEvent.x;
				y = currentEvent.y;
				speed = 0;
				direction = 0;
		}
		
		
		if ((currentEvent.time + currentEvent.duration)<Simulator.time) currentEvent = null;
		
		return true;
	};
	
	
	/**
	 * Called to clean up.
	 */
	public void finish() {
		// clear all events
		events.clear();
		if (leaveTime>Simulator.duration) leaveTime = Simulator.duration;
	}
	
	/**
	 * Returns true if both nodes have the same unique identifier
	 */
	public boolean equals(MobileNode other) {
		return id == other.id;
	}
	
}
