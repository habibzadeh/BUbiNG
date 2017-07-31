package it.unimi.di.law.bubing.tool;

/*
 * Copyright (C) 2013 Paolo Boldi, Massimo Santini, and Sebastiano Vigna
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.io.FastBufferedReader;
import it.unimi.dsi.lang.MutableString;
import it.unimi.dsi.logging.ProgressLogger;

import java.io.IOException;
import java.io.InputStreamReader;

import com.google.common.base.Charsets;

/** Builds and saves the <em>repetition set</em> of a crawl.
 *
 * <p>The input format for the tool are TAB-separated triples &lt;store,position,URL&gt;, which are assumed to be stably sorted by URL (the position
 * is the ordinal position in the store, as generated by the chosen filters).
 * The triples must contain <strong>all</strong> the URLs overall appearing in all involved stores. For each URL that appears more than once,
 * the pairs &lt;store,position&gt; of the copies following the first appearance are saved in a {@link LongOpenHashSet} as
 * <pre class=code>
 * 	store &lt;&lt; 48 | position
 * </pre>
 *
 */

//RELEASE-STATUS: DIST

public class BuildRepetitionSet {

	public static void main(String[] arg) throws IOException {
		if (arg.length == 0) {
			System.err.println("Usage: " + BuildRepetitionSet.class.getSimpleName() + " REPETITIONSET");
			System.exit(1);
		}

		final FastBufferedReader fastBufferedReader = new FastBufferedReader(new InputStreamReader(System.in, Charsets.US_ASCII));
		final MutableString s = new MutableString();
		final LongOpenHashSet repeatedSet = new LongOpenHashSet();
		final String outputFilename = arg[0];
		final ProgressLogger pl = new ProgressLogger();

		MutableString lastUrl = new MutableString();
		pl.itemsName = "lines";
		pl.start("Reading... ");
		while(fastBufferedReader.readLine(s) != null) {
			final int firstTab = s.indexOf('\t');
			final int secondTab = s.indexOf('\t', firstTab + 1);
			MutableString url = s.substring(secondTab + 1);
			if (url.equals(lastUrl)) {
				final int storeIndex = Integer.parseInt(new String(s.array(), 0, firstTab));
				final long storePosition = Long.parseLong(new String(s.array(), firstTab + 1, secondTab - firstTab - 1));
				repeatedSet.add((long)storeIndex << 48 | storePosition);
				System.out.print(storeIndex);
				System.out.print('\t');
				System.out.print(storePosition);
				System.out.print('\t');
				System.out.println(url);
			}

			lastUrl = url;
			pl.lightUpdate();
		}

		pl.done();

		fastBufferedReader.close();
		BinIO.storeObject(repeatedSet, outputFilename);
	}
}
