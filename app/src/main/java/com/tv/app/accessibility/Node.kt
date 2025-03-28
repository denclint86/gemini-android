package com.tv.app.accessibility

data class Node(
    val text :String,
    val className : String,
    val left : Int,
    val top : Int,
    val childNode : List<Node>?
)

