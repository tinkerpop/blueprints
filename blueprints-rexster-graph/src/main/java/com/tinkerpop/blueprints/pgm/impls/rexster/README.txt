Blueprints RexsterGraph 
Performance improvements
Pierre De Wilde, May 2011.

//
// GRAPH
//
gremlin> g = new RexsterGraph('http://localhost:8182/tinkergraph')
GET http://localhost:8182/tinkergraph (new RexsterGraph())
old:
GET http://localhost:8182/tinkergraph (new RexsterGraph())
GET http://localhost:8182/tinkergraph (toString())
GET http://localhost:8182/tinkergraph (toString())

gremlin> g.toString()
-
old: 
GET http://localhost:8182/tinkergraph

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
GET http://localhost:8182/tinkergraph/indices/edges/keys
GET http://localhost:8182/tinkergraph/indices/vertices/keys

gremlin> g.dropIndex(Index.VERTICES)
DELETE http://localhost:8182/tinkergraph/indices/vertices

gremlin> idx = g.createAutomaticIndex(Index.VERTICES, Vertex.class, null)
POST http://localhost:8182/tinkergraph/indices/vertices
type=automatic&class=vertex
GET http://localhost:8182/tinkergraph/indices/vertices/keys
GET http://localhost:8182/tinkergraph/indices/vertices/keys

gremlin> idx.getAutoIndexKeys()
GET http://localhost:8182/tinkergraph/indices/vertices/keys

gremlin> g.getIndex(Index.VERTICES, Vertex.class)
GET http://localhost:8182/tinkergraph/indices
GET http://localhost:8182/tinkergraph/indices/vertices/keys
GET http://localhost:8182/tinkergraph/indices/vertices/keys

gremlin> g.getIndex(Index.EDGES, Edge.class)
GET http://localhost:8182/tinkergraph/indices
GET http://localhost:8182/tinkergraph/indices/edges/keys
GET http://localhost:8182/tinkergraph/indices/edges/keys




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
http://localhost:8182/tinkergraph/vertices/1?rexster.showTypes=true

gremlin> v.map()
-
old:
GET http://localhost:8182/tinkergraph/vertices/1
http://localhost:8182/tinkergraph/vertices/1?rexster.showTypes=true

gremlin> v.age // v.getProperty()
-
old:
http://localhost:8182/tinkergraph/vertices/1?rexster.showTypes=true

gremlin> v.age=30 // v.setProperty()
POST http://localhost:8182/tinkergraph/vertices/1
age=(integer,30)
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
with support of v.getBothEdges():
GET http://localhost:8182/tinkergraph/vertices/1/bothE?rexster.offset.start=0&rexster.offset.end=100
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
with support of v.getBothEdges(label):
GET http://localhost:8182/tinkergraph/vertices/1/bothE?_label=knows&rexster.offset.start=0&rexster.offset.end=100
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








