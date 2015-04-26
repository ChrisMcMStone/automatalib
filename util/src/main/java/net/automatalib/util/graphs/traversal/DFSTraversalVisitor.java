/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.util.graphs.traversal;

import net.automatalib.commons.util.Holder;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;

final class DFSTraversalVisitor<N, E, D> implements GraphTraversalVisitor<N, E, DFSData<D>> {
	private final DFSVisitor<? super N, ? super E, D> visitor;
	private int dfsNum;
	private final MutableMapping<N, DFSData<D>> records;
	
	public DFSTraversalVisitor(IndefiniteGraph<N,E> graph, DFSVisitor<? super N, ? super E, D> visitor) {
		this.visitor = visitor;
		this.records = graph.createStaticNodeMapping();
	}

	@Override
	public GraphTraversalAction processInitial(N initialNode, Holder<DFSData<D>> outData) {
		D data = visitor.initialize(initialNode);
		DFSData<D> rec = new DFSData<D>(data, dfsNum++);
		records.put(initialNode, rec);
		
		outData.value = rec;
		return GraphTraversalAction.EXPLORE;
	}

	@Override
	public boolean startExploration(N node, DFSData<D> data) {
		visitor.explore(node, data.data);
		return true;
	}

	@Override
	public void finishExploration(N node, DFSData<D> data) {
		visitor.finish(node, data.data);
		data.finished = true;
	}

	@Override
	public GraphTraversalAction processEdge(N srcNode,
			DFSData<D> srcData, E edge, N tgtNode, Holder<DFSData<D>> outData) {
		DFSData<D> tgtRec = records.get(tgtNode);
		if(tgtRec == null) {
			D data = visitor.treeEdge(srcNode, srcData.data, edge, tgtNode);
			tgtRec = new DFSData<D>(data, dfsNum++);
			records.put(tgtNode, tgtRec);
			
			outData.value = tgtRec;
			return GraphTraversalAction.EXPLORE;
		}
		if(!tgtRec.finished)
			visitor.backEdge(srcNode, srcData.data, edge, tgtNode, tgtRec.data);
		else if(tgtRec.dfsNumber > srcData.dfsNumber)
			visitor.forwardEdge(srcNode, srcData.data, edge, tgtNode, tgtRec.data);
		else
			visitor.crossEdge(srcNode, srcData.data, edge, tgtNode, tgtRec.data);
		
		return GraphTraversalAction.IGNORE;
	}

	@Override
	public void backtrackEdge(N srcNode, DFSData<D> srcData, E edge, N tgtNode,
			DFSData<D> tgtData) {
		visitor.backtrackEdge(srcNode, srcData.data, edge, tgtNode, tgtData.data);
	}
	
	
}