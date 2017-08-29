package com.github.delphyne.swgoh.modscraper

import com.github.delphyne.swgoh.model.Character
import com.github.delphyne.swgoh.model.Mod
import com.github.delphyne.swgoh.model.Stat
import groovy.util.slurpersupport.GPathResult
import org.ccil.cowan.tagsoup.Parser
import org.junit.Test

class HtmlScraperTest {

	@Test
	void testFoo() {
		def r = new File('/Users/bcarr/Desktop/mods.htm').withReader { reader ->
			new HtmlScraper().scrape(reader)
		}

		println r
	}

	@Test
	void testStatFromNodePercent() {
		def slurper = new XmlSlurper();
		GPathResult gpath = slurper.parseText('''
			<w>
				<div class="statmod-stat">
					<span class="statmod-stat-value">+4.27%</span>
					<span class="statmod-stat-label">Critical Chance</span>
				</div>
			</w>
		''')
		Stat stat = HtmlScraper.statFromNode(gpath.find())
		assert stat.value == 4.27
		assert stat.type == 'Critical Chance'
		assert stat.percent
	}

	@Test
	void testStatFromNodeRaw() {
		def slurper = new XmlSlurper();
		GPathResult gpath = slurper.parseText('''
			<w>
				<div class="statmod-stat">
					<span class="statmod-stat-value">+664</span>
					<span class="statmod-stat-label">Protection</span>
				</div>
			</w>
		''')
		Stat stat = HtmlScraper.statFromNode(gpath.find())
		assert stat.value == 664
		assert stat.type == 'Protection'
		assert !stat.percent
	}

	@Test
	void testModFromNode() {
		def slurper = new XmlSlurper(new Parser())
		GPathResult gpath = slurper.parseText('''
			<div>
				<div class="statmod-full" data-classes="statmod statmod-t5">
					<div class="statmod-title">
						Mk V-A Health Transmitter
					</div>
					<div class="statmod-preview">
						<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_1.png" alt="Mk V-A Health Transmitter">
					</div>
					<div class="statmod-details">
						<div class="statmod-stats statmod-stats-1">
							<div class="statmod-stats-heading">
								Primary Stats
							</div>
							<div class="statmod-stat">
								<span class="statmod-stat-value">+5.88%</span> <span class="statmod-stat-label">Offense</span>
							</div>
						</div>
						<div class="statmod-stats statmod-stats-2">
							<div class="statmod-stats-heading">
								Secondary Stats
							</div>
							<div class="statmod-stat">
								<span class="statmod-stat-value">+4.27%</span> <span class="statmod-stat-label">Protection</span>
							</div>
							<div class="statmod-stat">
								<span class="statmod-stat-value">+2.38%</span> <span class="statmod-stat-label">Defense</span>
							</div>
							<div class="statmod-stat">
								<span class="statmod-stat-value">+664</span> <span class="statmod-stat-label">Protection</span>
							</div>
							<div class="statmod-stat">
								<span class="statmod-stat-value">+2.25%</span> <span class="statmod-stat-label">Critical Chance</span>
							</div>
						</div>
					</div>
				</div>
			</div>
		''')
		Mod mod = HtmlScraper.modFromNode(gpath.find())
		assert mod.name == 'Mk V-A Health Transmitter'
		assert mod.set == 'Health'
		assert mod.level == '15'
		assert mod.rarity == 'V'
		assert mod.tier == 'A'
		assert mod.slot == 'Transmitter'
		assert mod.primary == new Stat(type: "Offense", value: 5.88, percent: true)
		assert mod.secondary == [
				new Stat(type: "Protection", value: 4.27, percent: true),
				new Stat(type: "Defense", value: 2.38, percent: true),
				new Stat(type: "Protection", value: 664, percent: false),
				new Stat(type: "Critical Chance", value: 2.25, percent: true)
		]
	}

	@Test
	void testModWithNoSecondaries() {
		def slurper = new XmlSlurper(new Parser())
		GPathResult gpath = slurper.parseText('''
			<div class="collection-mod" data-id="-Hu8FKOhRJetQ_kJpbjRwQ">
				<div class="statmod pc-statmod pc-statmod-slot6 statmod-t1">
					<div class="statmod-summary">
						<div class="statmod-preview">
							<span class="statmod-level">1</span><a class="statmod-char-portrait" href="/u/pmuscat/collection/kylo-ren/"></a>
							<div title="" class="char-portrait char-portrait-dark-side char-portrait-xsmall" data-toggle="tooltip" data-original-title="Kylo Ren">
								<div class="char-portrait-image">
									<img class="char-portrait-img" src="//swgoh.gg/static/img/assets/tex.charui_kyloren.png" alt="Kylo Ren">
								</div>
							</div><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_5_6.png" alt="Mk III-E Critical Chance Multiplexer">
						</div>
					</div>
					<div class="statmod-full" data-classes="statmod statmod-t1">
						<div class="statmod-details">
							<div class="statmod-stats statmod-stats-1">
								<div class="statmod-stat">
									<span class="statmod-stat-value">+1.5%</span> <span class="statmod-stat-label">Protection</span>
								</div>
							</div>
							<div class="statmod-stats statmod-stats-2"></div>
						</div>
					</div>
				</div>
			</div>
		''')
		Mod mod = HtmlScraper.modFromNode(gpath.find())
		assert mod.name == 'Mk III-E Critical Chance Multiplexer'
		assert mod.set == 'Critical Chance'
		assert mod.level == '1'
		assert mod.rarity == 'III'
		assert mod.tier == 'E'
		assert mod.slot == 'Multiplexer'
		assert mod.primary == new Stat(type: 'Protection', value: 1.5, percent: true)
		assert mod.secondary == []
	}

	@Test
	void testCharacterFromNode() {
		def slurper = new XmlSlurper(new Parser())
		GPathResult gpath = slurper.parseText('''
			<body>
				<div class="collection-char collection-char-light-side">
					<div class="player-char-portrait char-portrait-full char-portrait-full-gear-t11">
						<a href="/u/kelraeger/collection/stormtrooper-han/" class="char-portrait-full-link" rel="nofollow"><img class="char-portrait-full-img" src="//swgoh.gg/static/img/assets/tex.charui_trooperstorm_han.png" alt="Stormtrooper Han" title="Stormtrooper Han"></a>
						<div class="char-portrait-full-gear"></div>
						<div class="star star1"></div>
						<div class="star star2"></div>
						<div class="star star3"></div>
						<div class="star star4"></div>
						<div class="star star5"></div>
						<div class="star star6"></div>
						<div class="star star7"></div>
						<div class="char-portrait-full-level">
							80
						</div>
						<div class="char-portrait-full-gear-level">
							XI
						</div>
					</div>
					<div class="collection-char-name">
						<a class="collection-char-name-link" href="/u/kelraeger/collection/stormtrooper-han/" rel="nofollow">Stormtrooper Han</a>
					</div>
					<div class="collection-char-statmods">
						<div class="pc-statmod-list">
							<div class="pc-statmod-list-sets">
								<div class="pc-statmod-list-set pc-statmod-list-set-1 pc-statmod-list-set-1-max"></div>
								<div class="pc-statmod-list-line pc-statmod-list-line-1 pc-statmod-list-line-1-max"></div>
								<div class="pc-statmod-list-line pc-statmod-list-line-2 pc-statmod-list-line-2-max"></div>
								<div class="pc-statmod-list-set pc-statmod-list-set-2 pc-statmod-list-set-2-max"></div>
								<div class="pc-statmod-list-line pc-statmod-list-line-3 pc-statmod-list-line-3-max"></div>
								<div class="pc-statmod-list-line pc-statmod-list-line-4 pc-statmod-list-line-4-max"></div>
								<div class="pc-statmod-list-set pc-statmod-list-set-3 pc-statmod-list-set-3-max"></div>
								<div class="pc-statmod-list-line pc-statmod-list-line-5 pc-statmod-list-line-5-max"></div>
								<div class="pc-statmod-list-line pc-statmod-list-line-6 pc-statmod-list-line-6-max"></div>
							</div>
							<div class="statmod pc-statmod statmod-small pc-statmod-slot1 statmod-t5">
								<div class="statmod-summary">
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_1.png" alt="Mk V-A Health Transmitter">
									</div>
								</div>
								<div class="statmod-full" data-classes="statmod statmod-t5">
									<div class="statmod-title">
										Mk V-A Health Transmitter
									</div>
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_1.png" alt="Mk V-A Health Transmitter">
									</div>
									<div class="statmod-details">
										<div class="statmod-stats statmod-stats-1">
											<div class="statmod-stats-heading">
												Primary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+5.88%</span> <span class="statmod-stat-label">Offense</span>
											</div>
										</div>
										<div class="statmod-stats statmod-stats-2">
											<div class="statmod-stats-heading">
												Secondary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+4</span> <span class="statmod-stat-label">Speed</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+1.57%</span> <span class="statmod-stat-label">Critical Chance</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+539</span> <span class="statmod-stat-label">Protection</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+1.36%</span> <span class="statmod-stat-label">Defense</span>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div class="statmod pc-statmod statmod-small pc-statmod-slot2 statmod-t5">
								<div class="statmod-summary">
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_2.png" alt="Mk V-A Health Receiver">
									</div>
								</div>
								<div class="statmod-full" data-classes="statmod statmod-t5">
									<div class="statmod-title">
										Mk V-A Health Receiver
									</div>
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_2.png" alt="Mk V-A Health Receiver">
									</div>
									<div class="statmod-details">
										<div class="statmod-stats statmod-stats-1">
											<div class="statmod-stats-heading">
												Primary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+30</span> <span class="statmod-stat-label">Speed</span>
											</div>
										</div>
										<div class="statmod-stats statmod-stats-2">
											<div class="statmod-stats-heading">
												Secondary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+0.8%</span> <span class="statmod-stat-label">Offense</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+1.61%</span> <span class="statmod-stat-label">Critical Chance</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+252</span> <span class="statmod-stat-label">Health</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+2.13%</span> <span class="statmod-stat-label">Tenacity</span>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div class="statmod pc-statmod statmod-small pc-statmod-slot3 statmod-t5">
								<div class="statmod-summary">
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_3.png" alt="Mk V-A Health Processor">
									</div>
								</div>
								<div class="statmod-full" data-classes="statmod statmod-t5">
									<div class="statmod-title">
										Mk V-A Health Processor
									</div>
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_3.png" alt="Mk V-A Health Processor">
									</div>
									<div class="statmod-details">
										<div class="statmod-stats statmod-stats-1">
											<div class="statmod-stats-heading">
												Primary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+11.75%</span> <span class="statmod-stat-label">Defense</span>
											</div>
										</div>
										<div class="statmod-stats statmod-stats-2">
											<div class="statmod-stats-heading">
												Secondary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+0.38%</span> <span class="statmod-stat-label">Offense</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+15</span> <span class="statmod-stat-label">Speed</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+443</span> <span class="statmod-stat-label">Protection</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+4.9%</span> <span class="statmod-stat-label">Potency</span>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div class="statmod pc-statmod statmod-small pc-statmod-slot4 statmod-t5">
								<div class="statmod-summary">
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_4.png" alt="Mk V-A Health Holo-Array">
									</div>
								</div>
								<div class="statmod-full" data-classes="statmod statmod-t5">
									<div class="statmod-title">
										Mk V-A Health Holo-Array
									</div>
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_4.png" alt="Mk V-A Health Holo-Array">
									</div>
									<div class="statmod-details">
										<div class="statmod-stats statmod-stats-1">
											<div class="statmod-stats-heading">
												Primary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+11.75%</span> <span class="statmod-stat-label">Defense</span>
											</div>
										</div>
										<div class="statmod-stats statmod-stats-2">
											<div class="statmod-stats-heading">
												Secondary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+10</span> <span class="statmod-stat-label">Speed</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+0.32%</span> <span class="statmod-stat-label">Offense</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+1.26%</span> <span class="statmod-stat-label">Critical Chance</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+454</span> <span class="statmod-stat-label">Protection</span>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div class="statmod pc-statmod statmod-small pc-statmod-slot5 statmod-t5">
								<div class="statmod-summary">
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_5.png" alt="Mk V-A Health Data-Bus">
									</div>
								</div>
								<div class="statmod-full" data-classes="statmod statmod-t5">
									<div class="statmod-title">
										Mk V-A Health Data-Bus
									</div>
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_5.png" alt="Mk V-A Health Data-Bus">
									</div>
									<div class="statmod-details">
										<div class="statmod-stats statmod-stats-1">
											<div class="statmod-stats-heading">
												Primary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+23.5%</span> <span class="statmod-stat-label">Protection</span>
											</div>
										</div>
										<div class="statmod-stats statmod-stats-2">
											<div class="statmod-stats-heading">
												Secondary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+1.72%</span> <span class="statmod-stat-label">Tenacity</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+14</span> <span class="statmod-stat-label">Speed</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+0.61%</span> <span class="statmod-stat-label">Health</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+326</span> <span class="statmod-stat-label">Health</span>
											</div>
										</div>
									</div>
								</div>
							</div>
							<div class="statmod pc-statmod statmod-small pc-statmod-slot6 statmod-t5">
								<div class="statmod-summary">
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_6.png" alt="Mk V-A Health Multiplexer">
									</div>
								</div>
								<div class="statmod-full" data-classes="statmod statmod-t5">
									<div class="statmod-title">
										Mk V-A Health Multiplexer
									</div>
									<div class="statmod-preview">
										<span class="statmod-level">15</span><img class="statmod-img" src="//swgoh.gg/static/img/assets/tex.statmodmystery_1_6.png" alt="Mk V-A Health Multiplexer">
									</div>
									<div class="statmod-details">
										<div class="statmod-stats statmod-stats-1">
											<div class="statmod-stats-heading">
												Primary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+5.88%</span> <span class="statmod-stat-label">Health</span>
											</div>
										</div>
										<div class="statmod-stats statmod-stats-2">
											<div class="statmod-stats-heading">
												Secondary Stats
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+1.8%</span> <span class="statmod-stat-label">Critical Chance</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+5</span> <span class="statmod-stat-label">Speed</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+6</span> <span class="statmod-stat-label">Defense</span>
											</div>
											<div class="statmod-stat">
												<span class="statmod-stat-value">+342</span> <span class="statmod-stat-label">Health</span>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="collection-char-sets">
							<div class="collection-char-sets-values">
								<div class="collection-char-set collection-char-set1 collection-char-set-max" data-toggle="tooltip" data-title="Set Bonus: 5% Health"></div>
								<div class="collection-char-set collection-char-set1 collection-char-set-max" data-toggle="tooltip" data-title="Set Bonus: 5% Health"></div>
								<div class="collection-char-set collection-char-set1 collection-char-set-max" data-toggle="tooltip" data-title="Set Bonus: 5% Health"></div>
							</div>
						</div>
					</div>
				</div>
			</body>
		''')
		Character character = HtmlScraper.characterFromNode(gpath.find())
		assert character.name == 'Stormtrooper Han'
		assert character.level == '80'
		assert character.gearLevel == "XI"
		assert character.mods == [
				new Mod(name: 'Mk V-A Health Transmitter',
						set:'Health',
						level: '15',
						rarity: 'V',
						tier: 'A',
						slot: 'Transmitter',
						primary: new Stat(type: 'Offense', value: 5.88, percent: true),
						secondary: [
								new Stat(type: 'Speed', value: 4, percent: false),
								new Stat(type: 'Critical Chance', value: 1.57, percent: true),
								new Stat(type: 'Protection', value: 539, percent: false),
								new Stat(type: 'Defense', value: 1.36, percent: true)
						]),
				new Mod(name: 'Mk V-A Health Receiver',
						set:'Health',
						level: '15',
						rarity: 'V',
						tier: 'A',
						slot: 'Receiver',
						primary: new Stat(type: 'Speed', value: 30, percent: false),
						secondary: [
								new Stat(type: 'Offense', value: 0.8, percent: true),
								new Stat(type: 'Critical Chance', value: 1.61, percent: true),
								new Stat(type: 'Health', value: 252, percent: false),
								new Stat(type: 'Tenacity', value: 2.13, percent: true)
						]),
				new Mod(name: 'Mk V-A Health Processor',
						set:'Health',
						level: '15',
						rarity: 'V',
						tier: 'A',
						slot: 'Processor',
						primary: new Stat(type: 'Defense', value: 11.75, percent: true),
						secondary: [
								new Stat(type: 'Offense', value: 0.38, percent: true),
								new Stat(type: 'Speed', value: 15, percent: false),
								new Stat(type: 'Protection', value: 443, percent: false),
								new Stat(type: 'Potency', value: 4.9, percent: true)
						]),
				new Mod(name: 'Mk V-A Health Holo-Array',
						set:'Health',
						level: '15',
						rarity: 'V',
						tier: 'A',
						slot: 'Holo-Array',
						primary: new Stat(type: 'Defense', value: 11.75, percent: true),
						secondary: [
								new Stat(type: 'Speed', value: 10, percent: false),
								new Stat(type: 'Offense', value: 0.32, percent: true),
								new Stat(type: 'Critical Chance', value: 1.26, percent: true),
								new Stat(type: 'Protection', value: 454, percent: false)
						]),
				new Mod(name: 'Mk V-A Health Data-Bus',
						set:'Health',
						level: '15',
						rarity: 'V',
						tier: 'A',
						slot: 'Data-Bus',
						primary: new Stat(type: 'Protection', value: 23.5, percent: true),
						secondary: [
								new Stat(type: 'Tenacity', value: 1.72, percent: true),
								new Stat(type: 'Speed', value: 14, percent: false),
								new Stat(type: 'Health', value: 0.61, percent: true),
								new Stat(type: 'Health', value: 326, percent: false)
						]),
				new Mod(name: 'Mk V-A Health Multiplexer',
						set:'Health',
						level: '15',
						rarity: 'V',
						tier: 'A',
						slot: 'Multiplexer',
						primary: new Stat(type: 'Health', value: 5.88, percent: true),
						secondary: [
								new Stat(type: 'Critical Chance', value: 1.8, percent: true),
								new Stat(type: 'Speed', value: 5, percent: false),
								new Stat(type: 'Defense', value: 6, percent: false),
								new Stat(type: 'Health', value: 342, percent: false)
						]),
		]
	}
}
