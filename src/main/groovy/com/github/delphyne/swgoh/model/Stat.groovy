package com.github.delphyne.swgoh.model

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true, includePackage = false)
class Stat {
	String type
	BigDecimal value
	boolean percent
}
