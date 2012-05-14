#!/usr/local/bin/graphlet
graph [    
#    This file is in version 1 of GML
    version 1
#   This file has been created by the program "demo"
    Creator "demo"
#   directed shows wether a graph is directed (1) or not (0).
#   In a directed graph, edges are have arrows that indicate
#   their direction.
    directed 1
#   A label is a text attatched to an object
    label "The principles of space travel"
    node [    
        id 1
        label "Earth"
        graphics [
            x 0.1
            y 0.0
            w 0.1
            h 0.1
            bitmap "earth.gif"
        ]
    ]
    node [
        id 2    
        label "Mars"
        graphics [
            x 0.9
            y 0.0
            w 0.055
            h 0.055
            bitmap "Mars.gif"
        ]
    ]
    edge [
        source 1
        target 2
    ]
]
