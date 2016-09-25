package com.github.delphyne.swgoh.modscraper

import com.github.delphyne.swgoh.model.Character
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.Argument
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup
import net.sourceforge.argparse4j.inf.Namespace

class Main {

	void run(String... args) {
		ArgumentParser parser = ArgumentParsers
				.newArgumentParser(getClass().getCanonicalName())
				.description('Given a username, scrapes the users mods from swgoh.gg into a CSV.')
				.defaultHelp(true)

		MutuallyExclusiveGroup group = parser.addMutuallyExclusiveGroup('Input')
			.required(true)

		Argument account = group
				.addArgument('-a', '--account-name')
				.help('Your account on swgoh.gg')
				.type(String)

		Argument input = group
				.addArgument('-i', '--character-html')
				.help('A saved HTML character collection page')
				.type(Arguments.fileType().verifyIsFile().verifyCanRead())

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

		List<Character> characters
		if (namespace.get(account.getDest())) {
			URL url = new URL("https://swgoh.gg/u/${namespace.get(account.getDest())}/collection/")
			URLConnection connection = url.openConnection()
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36")
			connection.connect()
			characters = new BufferedReader(new InputStreamReader(connection.inputStream)).withCloseable {
				new HtmlScraper().scrape(it)
			}

		} else (
			characters = namespace.<File>get(input.getDest()).withReader {
				new HtmlScraper().scrape(it)
			}
		)

		namespace.<File>get(output.getDest()).withWriter {
			new CsvWriter().write(characters, it)
		}
	}

	static void main(String... args) {
		new Main().run(args)
	}
}
