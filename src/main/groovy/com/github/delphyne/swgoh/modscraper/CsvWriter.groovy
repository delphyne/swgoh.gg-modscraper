package com.github.delphyne.swgoh.modscraper

import com.github.delphyne.swgoh.model.Character
import com.github.delphyne.swgoh.model.Mod
import com.opencsv.CSVWriter

class CsvWriter {

	void write(List<Character> characters, Writer writer) {
		// pivot characters into list of mods, storing each unique stat(+percentage) type
		Set<String> stats = []

		List<Row> rows = characters.inject((List<Row>) []) { List<Row> rows, Character character ->
			rows + character.mods.collect {Mod mod ->
				stats << Row.statKey(mod.primary)
				stats += mod.secondary.collect { Row.statKey(it) }
				new Row(mod: mod, equippedOn: character)
			}
		}

		new CSVWriter(writer).withCloseable { CSVWriter out ->
			List<String> columns = [
			        'Name',
					'Set',
					'Level',
					'Rarity',
					'Tier',
					'Slot'
			] + stats.sort(false) + ["Equipped By"]

			out.writeNext(columns as String[], true)

			rows.each { Row row ->
				def rc = row.columns()
				out.writeNext(columns.collect { String column -> rc[column] } as String[], true)
			}
		}
	}
}
