package com.github.delphyne.swgoh.modscraper

import com.github.delphyne.swgoh.model.Mod
import com.github.delphyne.swgoh.model.Stat
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import org.ccil.cowan.tagsoup.Parser

import java.util.regex.Matcher
import java.util.regex.Pattern

class HtmlScraper {

	static final Pattern STAT_VALUE_PATTERN = ~/\+(?<value>[\d.]+)(?<percent>%)?/
	static final Pattern MOD_NAME_PATTERN = ~/\s*Mk\s+(?<rarity>[VI]+)-(?<tier>[A-E])\s+(?<set>\w+(?: \w+)?)\s+(?<slot>\w+(?:-\w+)?)\s*/

	boolean scrape(Reader reader, Map<String, List<Mod>> results) {
		XmlSlurper slurper = new XmlSlurper(new Parser())
		GPathResult html = slurper.parse(reader)
		findChildWithClass(html.find(), 'div', 'mod-list').'**'.findAll { NodeChild nc ->
			nc.name() == 'div' && nc.attributes().get('class')?.split(' ')?.contains('collection-mod')
		}.inject(results) { Map<String, List<Mod>> acc, NodeChild it ->
			def character = findChildWithClass(it, 'img', 'char-portrait-img').attributes().get('alt').trim()
			acc[character] << modFromNode(it)
			acc
		}
		findChildWithClass(html.find(), 'ul', 'pagination').'**'.findAll { NodeChild nc ->
			nc.name() == 'a' && nc.attributes().get('aria-label') == 'Next'
		}
	}

	private static NodeChild findChildWithClass(NodeChild parent, String tag, String cls) {
		parent.'**'.find { NodeChild nc ->
			nc.name() == tag && nc.attributes().get('class')?.split(' ')?.contains(cls)
		}
	}

	private static Mod modFromNode(NodeChild nc) {
		Mod mod = new Mod()

		String name = findChildWithClass(findChildWithClass(nc, 'div', 'statmod-preview'), 'img', 'statmod-img').attributes().get('alt').trim()
		mod.name = name

		Matcher m = MOD_NAME_PATTERN.matcher(name)
		assert m.matches()

		mod.rarity = m.group('rarity')
		mod.tier = m.group('tier')
		mod.set = m.group('set')
		mod.slot = m.group('slot')

		mod.level = findChildWithClass(nc, 'span', 'statmod-level').text()

		mod.primary = statFromNode(findChildWithClass(nc, 'div', 'statmod-stats-1'))

		findChildWithClass(nc, 'div', 'statmod-stats-2').'**'.findAll { NodeChild it ->
			it.name() == 'div' && it.attributes().get('class')?.split(' ')?.contains('statmod-stat')
		}.each {
			mod.secondary << statFromNode(it)
		}

		mod
	}

	private static Stat statFromNode(NodeChild nc) {
		String valueText = findChildWithClass(nc, 'span', 'statmod-stat-value').text()
		Matcher matcher = STAT_VALUE_PATTERN.matcher(valueText)
		assert matcher.matches()
		BigDecimal value = new BigDecimal(matcher.group('value'))
		boolean percent = matcher.group('percent') as Boolean
		new Stat(value: value, percent: percent, type: findChildWithClass(nc, 'span', 'statmod-stat-label').text())
	}

	private static String trim(String s) {
		s.replaceAll(~/\s+/, " ").trim()
	}
}
