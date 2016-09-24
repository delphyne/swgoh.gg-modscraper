package com.github.delphyne.swgoh.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true, includePackage = false)
class Mod {
	String name
	String set
	String level
	String rarity
	String tier
	String slot
	Stat primary
	List<Stat> secondary = []
}
