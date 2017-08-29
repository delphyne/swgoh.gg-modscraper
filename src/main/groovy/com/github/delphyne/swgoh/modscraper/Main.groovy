package com.github.delphyne.swgoh.modscraper

import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.Argument
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace

class Main {

	void run(String... args) {
		ArgumentParser parser = ArgumentParsers
				.newArgumentParser(getClass().getCanonicalName())
				.description('Given a username, scrapes the users mods from swgoh.gg into a CSV.')
				.defaultHelp(true)

		Argument account = parser
				.addArgument('account-name')
				.help('Your account on swgoh.gg')
				.required(true)
				.type(String)

		Argument output = parser
				.addArgument('-o', '--output-file')
				.help('The file to output')
				.type(Arguments.fileType().verifyCanWrite())
				.setDefault(new File('out.csv'))

		Namespace namespace
		try {
			namespace = parser.parseArgs(args)
		} catch (ArgumentParserException e) {
			parser.handleError(e)
			System.exit(42)
			throw new Exception() // noop
		}

		int page = 1
		namespace.<File>get(output.getDest()).withWriter {
			def results = [:].withDefault {[]}
			while(true) {
				URL url = new URL("https://swgoh.gg/u/${namespace.get(account.getDest())}/mods/?page=${page++}")
				URLConnection connection = url.openConnection()
				// SWGOH.gg returns 403 forbiddens for the java user agent in an
				// attempt to stop tools like this from scraping their website.
				// oops.
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36")
				connection.connect()
				def next = new BufferedReader(new InputStreamReader(connection.inputStream)).withCloseable {
					new HtmlScraper().scrape(it, results)
				}
				if (!next) {
					break
				}
			}
			new CsvWriter().write(results, it)
		}
	}

	static void main(String... args) {
		new Main().run(args)
	}
}
