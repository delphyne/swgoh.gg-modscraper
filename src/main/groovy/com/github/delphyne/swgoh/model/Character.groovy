package com.github.delphyne.swgoh.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true, includePackage = false)
class Character {
	String name
	String level
	String gearLevel
	List<Mod> mods = []
}
