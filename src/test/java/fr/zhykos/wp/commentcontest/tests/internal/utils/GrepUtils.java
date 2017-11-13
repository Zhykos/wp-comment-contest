package fr.zhykos.wp.commentcontest.tests.internal.utils;
/*
 * Copyright (c) 2001, 2014, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* TODO Changer commentaire car le code a beaucoup changé et peut être virer le copyright...
 * Search a list of files for lines that match a given regular-expression
 * pattern. Demonstrates NIO mapped byte buffers, charsets, and regular
 * expressions. https://docs.oracle.com/javase/8/docs/technotes/guides/io/example/Grep.java
 */

final class GrepUtils {
	// XXX autre charset???
	private static Charset charset = Charset.forName("ISO-8859-15"); //$NON-NLS-1$
	private static CharsetDecoder decoder = charset.newDecoder();
	private static Pattern linePattern = Pattern.compile(".*\r?\n"); //$NON-NLS-1$
	private static Pattern pattern;

	private GrepUtils() {
		// DO NOTHING AND MUST NOT BE CALLED
	}

	private static boolean grep(final CharBuffer buffer) {
		boolean result = false;
		final Matcher matcher = linePattern.matcher(buffer); // Line matcher
		Matcher currentMatcher = null; // Pattern matcher
		while (matcher.find()) {
			final CharSequence group = matcher.group(); // The current line
			if (currentMatcher == null) {
				currentMatcher = pattern.matcher(group);
			} else {
				currentMatcher.reset(group);
			}
			if (currentMatcher.find()) {
				result = true;
				break;
			}
			if (matcher.end() == buffer.limit()) {
				break;
			}
		}
		return result;
	}

	private static boolean grep(final File file) throws IOException {
		try (final FileInputStream input = new FileInputStream(file);
				final FileChannel fileChannel = input.getChannel();) {
			final int channelSize = (int) fileChannel.size();
			final MappedByteBuffer mappedBuffer = fileChannel
					.map(FileChannel.MapMode.READ_ONLY, 0, channelSize);
			final CharBuffer buffer = decoder.decode(mappedBuffer);
			return grep(buffer);
		}
	}

	public static boolean grep(final File file, final String patternToSearch)
			throws UtilsException {
		try {
			pattern = Pattern.compile(patternToSearch);
			return grep(file);
		} catch (final Exception e) {
			throw new UtilsException(e);
		}
	}

	public static File[] grep(final File[] files, final String patternToSearch)
			throws UtilsException {
		try {
			final Set<File> foundFiles = new HashSet<>();
			pattern = Pattern.compile(patternToSearch);
			for (final File file : files) {
				final boolean patternFound = grep(file);
				if (patternFound) {
					foundFiles.add(file);
				}
			}
			return foundFiles.toArray(new File[foundFiles.size()]);
		} catch (final Exception e) {
			throw new UtilsException(e);
		}
	}

}