graph [
	comment "This is a sample graph"
	directed 1
	IsPlanar 1
	# This is a comment
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
		label "Node 3"
	    escape_property "Node 3 \"with quote\""
	]
	edge [
		source 1
		target 2
		label "Edge from node 1 to node 2"
	]
	edge [
		source 2
		target 3
		label "Edge from node 2 to node 3"
	]
	edge [
		source 3
		target 1
		label "Edge from node 3 to node 1" # This is also a comment
	]
]