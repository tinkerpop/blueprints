graph [
	node [
		id 1
		blueprintsId "3"
		name "lop"
		lang "java"
	]
	node [
		id 2
		blueprintsId "2"
		name "vadas"
		age 27
	]
	node [
		id 3
		blueprintsId "1"
		name "marko"
		age 29
	]
	node [
		id 4
		blueprintsId "6"
		name "peter"
		age 35
	]
	node [
		id 5
		blueprintsId "5"
		name "ripple"
		lang "java"
	]
	node [
		id 6
		blueprintsId "4"
		name "josh"
		age 32
	]
	edge [
		source 6
		target 5
		label "created"
		blueprintsId "10"
		weight 1.0
	]
	edge [
		source 3
		target 2
		label "knows"
		blueprintsId "7"
		weight 0.5
	]
	edge [
		source 3
		target 1
		label "created"
		blueprintsId "9"
		weight 0.4
	]
	edge [
		source 3
		target 6
		label "knows"
		blueprintsId "8"
		weight 1.0
	]
	edge [
		source 6
		target 1
		label "created"
		blueprintsId "11"
		weight 0.4
	]
	edge [
		source 4
		target 1
		label "created"
		blueprintsId "12"
		weight 0.2
	]
]