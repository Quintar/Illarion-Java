/*
 * This file is part of the Illarion easyNPC Editor.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion easyNPC Editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion easyNPC Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion easyNPC Editor.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.easynpc;

import illarion.common.util.Crypto;
import illarion.common.util.TableLoader;
import illarion.easynpc.docu.DocuEntry;
import illarion.easynpc.gui.Config;
import illarion.easynpc.parser.*;
import illarion.easynpc.parser.tasks.ParseScriptTask;
import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * This class parses a easyNPC script that contains the plain script data to a parsed script that contains the
 * analyzed script data.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class Parser implements DocuEntry {
    /**
     * The name of this application.
     */
    @SuppressWarnings("nls")
    public static final String APPLICATION = "Illarion easyNPC";

    /**
     * The version and name of this parser.
     */
    @SuppressWarnings("nls")
    public static final String VERSION = "1.23.1";

    /**
     * The full name of the application. (Name and Version)
     */
    @SuppressWarnings("nls")
    public static final String FULLNAME = "easyNPC Parser v" + VERSION;

    /**
     * The singleton instance of this class.
     */
    private static final Parser INSTANCE = new Parser();

    /**
     * The logger instance that takes care for the logging output of this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Parser.class);

    /**
     * This pattern is used to find a string in the text.
     */
    @SuppressWarnings("nls")
    private static final Pattern STRING_ENTRY = Pattern.compile("(\"[^\"]*\")", Pattern.MULTILINE);

    /**
     * The list of NPC types this parser knows.
     */
    @Nonnull
    private final NpcType[] types;

    /**
     * This executor service takes care for the asynchronous execution of the parser.
     */
    private ExecutorService executorService;

    /**
     * The private constructor to avoid any instances but the singleton instance. This also prepares the list that
     * are required to work and the registers the parsers working in this parser.
     */
    @SuppressWarnings("nls")
    private Parser() {
        final Crypto crypt = new Crypto();
        crypt.loadPublicKey();
        TableLoader.setCrypto(crypt);

        final List<NpcType> typeList = new ArrayList<NpcType>();
        typeList.add(new NpcComment());
        typeList.add(new NpcBasics());
        typeList.add(new NpcColors());
        typeList.add(new NpcHair());
        typeList.add(new NpcEquipment());
        typeList.add(new NpcTalk());
        typeList.add(new NpcEmpty());
        typeList.add(new NpcCycleText());
        typeList.add(new NpcWalk());
        typeList.add(new NpcTradeComplex());
        typeList.add(new NpcTradeSimple());
        typeList.add(new NpcTradeText());

        types = typeList.toArray(new NpcType[typeList.size()]);

        executorService = new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors() * 2, 500,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    /**
     * Get the singleton instance of this parser.
     *
     * @return the singleton instance of this class
     */
    @Nonnull
    public static Parser getInstance() {
        return INSTANCE;
    }

    /**
     * This function starts the parser without GUI and is used to parse some
     * scripts directly.
     *
     * @param args the path to the script or the folder with the scripts to parse
     * @throws IOException in case the script can't be read
     */
    @SuppressWarnings("nls")
    public static void main(@Nonnull final String[] args) throws IOException {
        Config.getInstance().init();

        if (args.length == 0) {
            System.out.println("You need to set a script to parse, else nothing can be done.");
        }

        for (final String arg : args) {
            final File sourceFile = new File(arg);
            if (sourceFile.exists() && sourceFile.isFile()) {
                parseScript(sourceFile);
            } else if (sourceFile.exists() && sourceFile.isDirectory()) {
                final String[] fileNames =
                        sourceFile.list(new FilenameFilter() {
                            private static final String fileEnding = ".npc";

                            @Override
                            public boolean accept(final File dir, @Nonnull final String name) {
                                return name.endsWith(fileEnding);
                            }
                        });

                for (final String fileName : fileNames) {
                    final File targetFile =
                            new File(sourceFile.getPath() + File.separator
                                    + fileName);
                    parseScript(targetFile);
                }
            }
        }
    }

    /**
     * Parse one script.
     *
     * @param file the file of the script to parse
     * @throws IOException in case reading the script fails
     */
    @SuppressWarnings("nls")
    public static void parseScript(@Nonnull final File file) throws IOException {
        final EasyNpcScript script = new EasyNpcScript(file);
        final ParsedNpc parsedNPC = getInstance().parse(script);

        System.out.print("File \"" + file.getName() + "\" parsed - Encoding: "
                + script.getScriptEncoding().name() + " - Errors: ");
        if (parsedNPC.hasErrors()) {
            System.out.println(Integer.toString(parsedNPC.getErrorCount()));
            final int errorCount = parsedNPC.getErrorCount();
            for (int i = 0; i < errorCount; ++i) {
                final ParsedNpc.Error error = parsedNPC.getError(i);
                System.out.println("\tLine "
                        + Integer.toString(error.getLine().getLineNumber()) + ": "
                        + error.getMessage());
            }
            System.out.println();
            return;
        }
        System.out.println("none");

        final ScriptWriter writer = new ScriptWriter();
        writer.setTargetLanguage(ScriptWriter.ScriptWriterTarget.LUA);
        writer.setSource(parsedNPC);
        final File luaTargetFile =
                new File(file.getParentFile().getParent() + File.separator
                        + parsedNPC.getLuaFilename());
        Writer outputWriter =
                new OutputStreamWriter(new FileOutputStream(luaTargetFile),
                        "ISO-8859-1");
        writer.setWritingTarget(outputWriter);
        writer.write();
        outputWriter.close();

        writer.setTargetLanguage(ScriptWriter.ScriptWriterTarget.EasyNPC);
        outputWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        writer.setWritingTarget(outputWriter);
        writer.write();
        outputWriter.close();
    }

    @SuppressWarnings("nls")
    @Override
    public DocuEntry getChild(final int index) {
        if ((index < 0) || (index >= types.length)) {
            throw new IllegalArgumentException("Index out of range.");
        }
        return types[index];
    }

    @Override
    public int getChildCount() {
        return types.length;
    }

    @SuppressWarnings("nls")
    @Override
    public String getDescription() {
        return Lang.getMsg(getClass(), "Docu.description");
    }

    @Nullable
    @Override
    public String getExample() {
        return null;
    }

    @Nullable
    @Override
    public String getSyntax() {
        return null;
    }

    @SuppressWarnings("nls")
    @Override
    public String getTitle() {
        return Lang.getMsg(getClass(), "Docu.title");
    }


    /**
     * Parse the script asynchronously. You have to monitor the event bus for the {@link illarion.easynpc.parser.events.ParserFinishedEvent}
     * to find out when the parsing is done.
     *
     * @param source the script that is parsed
     */
    public void parseAsynchronously(final EasyNpcScript source) {
        executorService.submit(new ParseScriptTask(source));
    }

    /**
     * Parse the NPC and return the parsed version of the NPC.
     *
     * @param source the easyNPC source script that is supposed to be parsed.
     * @return the parsed version of the NPC
     */
    @Nonnull
    @SuppressWarnings("nls")
    public ParsedNpc parse(@Nonnull final EasyNpcScript source) {
        final long start = System.currentTimeMillis();
        final int count = source.getEntryCount();
        final ParsedNpc resultNpc = new ParsedNpc();

        for (int i = 0; i < count; i++) {
            EasyNpcScript.Line line = source.getEntry(i);
            boolean lineParsed = false;
            for (final NpcType type : types) {
                if (type.canParseLine(line)) {
                    type.parseLine(line, resultNpc);
                    lineParsed = true;
                    break;
                }
            }

            if (!lineParsed) {
                resultNpc.addError(line, "No parser seems to know what to do with this line.");
            }
        }

        LOGGER.debug("Parsing the script took " + Long.toString(System.currentTimeMillis() - start) + "ms");

        return resultNpc;
    }

    public void enlistHighlightedWords(final TokenMap map) {
        for (int parserIdx = 0; parserIdx < types.length; ++parserIdx) {
            types[parserIdx].enlistHighlightedWords(map);
        }
    }
}
