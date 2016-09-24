package com.github.delphyne.swgoh.modscraper

import com.github.delphyne.swgoh.model.Character
import com.github.delphyne.swgoh.model.Mod
import com.github.delphyne.swgoh.model.Stat

class Row {

	Mod mod
	Character equippedOn

	Map<String, String> columns() {
		def c = [
		        Name: mod.name,
				Set: mod.set,
				Level: mod.level,
				Rarity: mod.rarity,
				Tier: mod.tier,
				Slot: mod.slot,
				'Equipped By': equippedOn.name
		] + [
				(statKey(mod.primary)): mod.primary.value
		]

		mod.secondary.each { Stat stat ->
			def key = statKey(stat)
			c[key] = (c[key] ?: 0) + stat.value
		}

		c
	}

	static String statKey(Stat stat) {
		"${stat.type}${stat.percent ? " %" : ""}"
	}
}
