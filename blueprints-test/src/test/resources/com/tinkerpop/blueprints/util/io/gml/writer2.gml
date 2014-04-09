graph [
	node [
		id 1
		label "Node 1"
	]
	node [
		id 2
		label "Node 2"
	]
	node [
		id 3
		escape_property "Node 3 \"with quote\""
		label "Node 3"
	]
	edge [
		source 1
		target 2
		label "Edge from node 1 to node 2"
		directed "true"
	]
	edge [
		source 2
		target 3
		label "Edge from node 2 to node 3"
		directed "true"
	]
	edge [
		source 3
		target 1
		label "Edge from node 3 to node 1"
		directed "true"
	]
]
