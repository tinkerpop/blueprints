Blueprints RexsterGraph 
Performance improvements
Pierre De Wilde, May 2011.

SUMMARY
=======
The general design of HTTP remote Tinkerpop stack is: Gremlin => Pipes => Blueprints => Rexster => GraphDB.

To improve the performance of this stack:

1. reduce the HTTP traffic between Blueprints and Rexster
2. directly send Gremlin commands to Rexster

DETAILS
=======

1. reduce the HTTP traffic between Blueprints and Rexster
---------------------------------------------------------
RexsterGraph() translates Blueprints calls into Rexster REST API calls.

To minimize the number of calls to Rexster REST API:
- remove duplicated calls (e.g. via g.toString())
- remove unnecessary calls (e.g v.outE)
- store raw vertex/edge at object level (e.g. v.keys())
- optimize the use of buffer (e.g. g.V)

//
// GRAPH
//
gremlin> g = new RexsterGraph('http://localhost:8182/tinkergraph')
GET http://localhost:8182/tinkergraph (new RexsterGraph())
old: 3 identical calls
GET http://localhost:8182/tinkergraph (new RexsterGraph())
GET http://localhost:8182/tinkergraph (toString())
GET http://localhost:8182/tinkergraph (toString())

gremlin> g.toString()
-
old: GET http://localhost:8182/tinkergraph

gremlin> g.getRawGraph()
GET http://localhost:8182/tinkergraph
old: same

gremlin> g.clear()
DELETE http://localhost:8182/tinkergraph
old: same

gremlin> g.shutdown()
-
old: same

//
// INDEX
//
gremlin> g.getIndices()
GET http://localhost:8182/tinkergraph/indices
GET http://localhost:8182/tinkergraph/indices/edges/keys
GET http://localhost:8182/tinkergraph/indices/vertices/keys
old: 5 calls
GET http://localhost:8182/tinkergraph/indices
GET http://localhost:8182/tinkergraph/indices/edges/keys
GET http://localhost:8182/tinkergraph/indices/vertices/keys
GET http://localhost:8182/tinkergraph/indices/edges/keys
GET http://localhost:8182/tinkergraph/indices/vertices/keys

gremlin> g.dropIndex(Index.VERTICES)
DELETE http://localhost:8182/tinkergraph/indices/vertices
old: same

gremlin> idx = g.createAutomaticIndex(Index.VERTICES, Vertex.class, null)
POST http://localhost:8182/tinkergraph/indices/vertices (type=automatic&class=vertex)
GET http://localhost:8182/tinkergraph/indices/vertices/keys
old: 3 calls
POST http://localhost:8182/tinkergraph/indices/vertices?type=automatic&class=vertex
GET http://localhost:8182/tinkergraph/indices/vertices/keys
GET http://localhost:8182/tinkergraph/indices/vertices/keys

gremlin> idx.getAutoIndexKeys()
-
old: GET http://localhost:8182/tinkergraph/indices/vertices/keys

gremlin> g.dropIndex(Index.VERTICES)
gremlin> idx = g.createAutomaticIndex(Index.VERTICES, Vertex.class, new HashSet(['name']))
POST http://localhost:8182/tinkergraph/indices/vertices (type=automatic&class=vertex&keys=[name])
GET http://localhost:8182/tinkergraph/indices/vertices/keys
old: 3 calls
POST http://localhost:8182/tinkergraph/indices/vertices?type=automatic&class=vertex&keys=[name]
GET http://localhost:8182/tinkergraph/indices/vertices/keys
GET http://localhost:8182/tinkergraph/indices/vertices/keys

gremlin> idx.getAutoIndexKeys()
-
old: GET http://localhost:8182/tinkergraph/indices/vertices/keys

gremlin> g.getIndex(Index.VERTICES, Vertex.class)
GET http://localhost:8182/tinkergraph/indices/vertices
GET http://localhost:8182/tinkergraph/indices/vertices/keys
old: 3 calls
GET http://localhost:8182/tinkergraph/indices
GET http://localhost:8182/tinkergraph/indices/vertices/keys
GET http://localhost:8182/tinkergraph/indices/vertices/keys

gremlin> g.dropIndex(Index.VERTICES)
gremlin> g.createManualIndex(Index.VERTICES, Vertex.class)
POST http://localhost:8182/tinkergraph/indices/vertices (type=manual&class=vertex)
old: 
POST http://localhost:8182/tinkergraph/indices/vertices?type=manual&class=vertex

gremlin> g.dropIndex(Index.VERTICES)
gremlin> g.createAutomaticIndex(Index.VERTICES, Vertex.class, null)


//
// VERTEX
//
gremlin> v = g.v(1)
GET http://localhost:8182/tinkergraph/vertices/1
old: same

gremlin> v.getRawVertex()
-
old:
GET http://localhost:8182/tinkergraph/vertices/1

gremlin> v.keys() // v.getPropertyKeys()
-
old:
GET http://localhost:8182/tinkergraph/vertices/1

gremlin> v.values()
-
old:
GET http://localhost:8182/tinkergraph/vertices/1
GET http://localhost:8182/tinkergraph/vertices/1?rexster.showTypes=true

gremlin> v.map()
-
old:
GET http://localhost:8182/tinkergraph/vertices/1
GET http://localhost:8182/tinkergraph/vertices/1?rexster.showTypes=true

gremlin> v.age // v.getProperty()
-
old:
http://localhost:8182/tinkergraph/vertices/1?rexster.showTypes=true

gremlin> v.age=30 // v.setProperty()
POST http://localhost:8182/tinkergraph/vertices/1 (age=(integer,30))
old:
POST http://localhost:8182/tinkergraph/vertices/1?age=(integer,30)

gremlin> v.removeProperty('age')
DELETE http://localhost:8182/tinkergraph/vertices/1?age
old: same

gremlin> v.inE // v.getInEdges()
GET http://localhost:8182/tinkergraph/vertices/1/inE?rexster.offset.start=0&rexster.offset.end=100
old:
GET http://localhost:8182/tinkergraph/vertices/1/inE?rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/vertices/1/inE?rexster.offset.start=100&rexster.offset.end=200

gremlin> v.inE('knows') // v.getInEdges(label)
GET http://localhost:8182/tinkergraph/vertices/1/inE?_label=knows&rexster.offset.start=0&rexster.offset.end=100
old:
GET http://localhost:8182/tinkergraph/vertices/1/inE?_label=knows&rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/vertices/1/inE?_label=knows&rexster.offset.start=100&rexster.offset.end=200

gremlin> v.outE // v.getOutEdges()
GET http://localhost:8182/tinkergraph/vertices/1/outE?rexster.offset.start=0&rexster.offset.end=100
old:
GET http://localhost:8182/tinkergraph/vertices/1/outE?rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/edges/7
GET http://localhost:8182/tinkergraph/edges/8
GET http://localhost:8182/tinkergraph/edges/9
GET http://localhost:8182/tinkergraph/vertices/1/outE?rexster.offset.start=100&rexster.offset.end=200

gremlin> v.outE('knows') // v.getOutEdges(label)
GET http://localhost:8182/tinkergraph/vertices/1/outE?_label=knows&rexster.offset.start=0&rexster.offset.end=100
old:
GET http://localhost:8182/tinkergraph/vertices/1/outE?_label=knows&rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/edges/7
GET http://localhost:8182/tinkergraph/edges/8
GET http://localhost:8182/tinkergraph/vertices/1/outE?_label=knows&rexster.offset.start=100&rexster.offset.end=200

gremlin> v.bothE // missing v.getBothEdges(), so splitted into v.getInEdges() and v.getOutEdges()
GET http://localhost:8182/tinkergraph/vertices/1/inE?rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/vertices/1/outE?rexster.offset.start=0&rexster.offset.end=100
old:
GET http://localhost:8182/tinkergraph/vertices/1/inE?_label=knows&rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/vertices/1/outE?rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/edges/7
GET http://localhost:8182/tinkergraph/edges/8
GET http://localhost:8182/tinkergraph/edges/9
GET http://localhost:8182/tinkergraph/vertices/1/inE?_label=knows&rexster.offset.start=100&rexster.offset.end=200
GET http://localhost:8182/tinkergraph/vertices/1/outE?rexster.offset.start=100&rexster.offset.end=200

gremlin> v.bothE('knows') // missing v.getBothEdges(label), so splitted into v.getInEdges(label) and v.getOutEdges(label)
GET http://localhost:8182/tinkergraph/vertices/1/inE?_label=knows&rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/vertices/1/outE?_label=knows&rexster.offset.start=0&rexster.offset.end=100
old:
GET http://localhost:8182/tinkergraph/vertices/1/inE?_label=knows&rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/vertices/1/outE?_label=knows&rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/edges/7
GET http://localhost:8182/tinkergraph/edges/8
GET http://localhost:8182/tinkergraph/vertices/1/inE?_label=knows&rexster.offset.start=100&rexster.offset.end=200
GET http://localhost:8182/tinkergraph/vertices/1/outE?_label=knows&rexster.offset.start=100&rexster.offset.end=200

gremlin> g.V // g.getVertices()
GET http://localhost:8182/tinkergraph/vertices?rexster.offset.start=0&rexster.offset.end=100
old:
GET http://localhost:8182/tinkergraph/vertices?rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/vertices/3
GET http://localhost:8182/tinkergraph/vertices/2
GET http://localhost:8182/tinkergraph/vertices/1
GET http://localhost:8182/tinkergraph/vertices/6
GET http://localhost:8182/tinkergraph/vertices/5
GET http://localhost:8182/tinkergraph/vertices/4
GET http://localhost:8182/tinkergraph/vertices?rexster.offset.start=100&rexster.offset.end=200

//
// EDGE
// 
gremlin> e = g.e(7)
GET http://localhost:8182/tinkergraph/edges/7
old: same

gremlin> e.getRawEdge()
-
old:
GET http://localhost:8182/tinkergraph/edges/7

gremlin> e.label
-
old: same

gremlin> e.outV // e.getOutVertex()
-
old:
GET http://localhost:8182/tinkergraph/vertices/1

gremlin> e.inV // e.getInVertex()
-
old:
GET http://localhost:8182/tinkergraph/vertices/2

gremlin> e.bothV // missing e.getBothVertex()
-
old:
GET http://localhost:8182/tinkergraph/vertices/1
GET http://localhost:8182/tinkergraph/vertices/2

gremlin> g.E // g.getEdges()
GET http://localhost:8182/tinkergraph/edges?rexster.offset.start=0&rexster.offset.end=100
old:
GET http://localhost:8182/tinkergraph/edges?rexster.offset.start=0&rexster.offset.end=100
GET http://localhost:8182/tinkergraph/edges/10
GET http://localhost:8182/tinkergraph/edges/7
GET http://localhost:8182/tinkergraph/edges/9
GET http://localhost:8182/tinkergraph/edges/8
GET http://localhost:8182/tinkergraph/edges/11
GET http://localhost:8182/tinkergraph/edges/12
GET http://localhost:8182/tinkergraph/edges?rexster.offset.start=100&rexster.offset.end=200


2. directly send Gremlin commands to Rexster
--------------------------------------------
Any Gremlin expression may be directly send to a graph database running at Rexster server if Gremlin Extension is enabled.
To enable Gremlin Extension, see rexster.xml configuration file.

This functionality is like to a remote procedure call: the Gremlin expression is sent to the Gremlin Extension of Rexster server.
The Gremlin Extension executes the Gremlin script using the normal Tinkerpop stack (Gremlin => Pipes => Blueprints => GraphDB) and 
returns the results in JSON format.

Gremlin Extension is provided at 3 levels:

- Graph:	g.runGremlin("...") with g as current graph reference
- Vertex:	v.runGremlin("...") with v as current vertex reference
- Edge:	e.runGremlin("...") with e as current edge reference

Note that the first call to Gremlin Extension takes some time to load several classes. Subsequents calls are very fast.

Examples:

gremlin> g.v(1).out.name
 12 calls in Blueprints 0.7
  5 calls in Blueprints 0.8
gremlin> g.runGremlin("g.v(1).out.name")
  1 call in Blueprints 0.8


gremlin> m=[:];g.v(1).out.in.groupCount(m)>>-1;m
 25 calls in Blueprints 0.7
 13 calls in Blueprints 0.8
gremlin> g.runGremlin("m=[:];g.v(1).out.in.groupCount(m)>>-1;m")
  1 call in Blueprints 0.8
