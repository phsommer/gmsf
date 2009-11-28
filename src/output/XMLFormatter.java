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
package output;

import java.io.*;
import java.util.*;

import simulator.*;
import event.*;

/**
 * XMLFormatter generates a XML file with mobility traces.
 * @author psommer
 *
 */
public class XMLFormatter extends TraceFormatter {

	
	public void finish() {
		
		// sort all events by node identifier and start time
		Collections.sort(Simulator.events, new EventComparatorByNodeIdByStartTime());
		
		try {
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(Simulator.outputDirectory + "/trace.xml")));
			
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			writer.write("<traces xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"traces.xsd\">\n");
			
			// output paths of all nodes
			Iterator<Event> it = Simulator.events.iterator();
			int lastNodeId = -1;
			
			while (it.hasNext()) {
				
				Event event = it.next();
				
				if (event.node.id!=lastNodeId) {
					
					if (lastNodeId!=-1) {
						writer.write("    </events>\n");
						writer.write("  </node>\n");
					}
					
					// next node
					writer.write("  <node id=\"" + event.node.id + "\">\n");
					writer.write("    <events>\n");
	 			
					lastNodeId = event.node.id;
				}
				
				if (event.type==Event.MOVE) {
					Move temp = (Move) event;
					
					writer.write("      <move>\n");
					writer.write("        <start>\n");
					writer.write("          <time>" + String.format("%.2f",temp.time) + "</time>\n");
					writer.write("          <x>" + String.format("%.2f",temp.x) + "</x>\n");
					writer.write("          <y>" + String.format("%.2f",temp.y) + "</y>\n");
					writer.write("        </start>\n");
					writer.write("        <stop>\n");
					writer.write("          <time>" + String.format("%.2f", temp.time + temp.duration) + "</time>\n");
					writer.write("          <x>" + String.format("%.2f", temp.moveToX) + "</x>\n");
					writer.write("          <y>" + String.format("%.2f",temp.moveToY) + "</y>\n");
					writer.write("        </stop>\n");
					writer.write("      </move>\n");
					
					
				} else if (event.type==Event.PAUSE) {
					Pause temp = (Pause) event;
					
					writer.write("      <pause>\n");
					writer.write("        <time>" + String.format("%.2f", temp.time) + "</time>\n");
					writer.write("        <x>" + String.format("%.2f", temp.x) + "</x>\n");
					writer.write("        <y>" + String.format("%.2f", temp.y) + "</y>\n");
					writer.write("        <duration>" + String.format("%.2f", temp.duration)  + "</duration>\n");
					writer.write("      </pause>\n");	
					
					
				}  else if (event.type==Event.JOIN) {
					
					writer.write("      <join>\n");
					writer.write("        <time>" + String.format("%.2f", event.time) + "</time>\n");
					writer.write("        <x>" + String.format("%.2f", event.x) + "</x>\n");
					writer.write("        <y>" + String.format("%.2f", event.y) + "</y>\n");
					writer.write("      </join>\n");
	 				
					
				}  else if (event.type==Event.LEAVE) {
					
					writer.write("      <leave>\n");
					writer.write("        <time>" + String.format("%.2f", event.time) + "</time>\n");
					writer.write("        <x>" + String.format("%.2f", event.x) + "</x>\n");
					writer.write("        <y>" + String.format("%.2f", event.y) + "</y>\n");
					writer.write("      </leave>\n");
	 				
				}
				
			}
			
			if (lastNodeId!=-1) {
				writer.write("    </events>\n");
				writer.write("  </node>\n");
			}
				
			writer.write("</traces>\n");
			writer.close();
			
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
	}
	
}
