graph [
	node [
		id 1
		blueprintsId "1"
		label "Node 1"
	]
	node [
		id 2
		blueprintsId "2"
		label "Node 2"
	]
	node [
		id 3
		blueprintsId "3"
		escape_property "Node 3 \"with quote\""
		label "Node 3"
	]
	edge [
		source 1
		target 2
		label "Edge from node 1 to node 2"
		blueprintsId "0"
		directed "true"
	]
	edge [
		source 2
		target 3
		label "Edge from node 2 to node 3"
		blueprintsId "1"
		directed "true"
	]
	edge [
		source 3
		target 1
		label "Edge from node 3 to node 1"
		blueprintsId "2"
		directed "true"
	]
]
