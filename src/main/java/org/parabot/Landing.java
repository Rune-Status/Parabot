package org.parabot;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.parabot.api.io.Directories;
import org.parabot.api.translations.TranslationHelper;
import org.parabot.core.Core;
import org.parabot.core.arguments.LandingArgument;
import org.parabot.core.network.NetworkInterface;
import org.parabot.core.network.proxy.ProxySocket;
import org.parabot.core.network.proxy.ProxyType;
import org.parabot.core.ui.BotUI;
import org.parabot.core.ui.ServerSelector;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author Everel, JKetelaar, Matt, Dane
 * @version 3.0
 * @see <a href="http://www.parabot.org">Homepage</a>
 */
public final class Landing {

    public static void main(String... args) throws IOException {

        parseArgs(args);

        Directories.validate();

        Core.verbose(TranslationHelper.translate("DEBUG_MODE") + Core.isMode(Core.LaunchMode.LOCAL_ONLY));

        try {
            Core.verbose(TranslationHelper.translate("SETTING_LOOK_AND_FEEL")
                    + UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (!Core.isMode(Core.LaunchMode.LOCAL_ONLY) && Core.hasValidation() && !Core.isValid()) {
            Core.downloadNewVersion();
            return;
        }

        Core.verbose(TranslationHelper.translate("STARTING_LOGIN_GUI"));
        Core.getInjector().getInstance(BotUI.class);
    }

    private static void parseArgs(String... args) {
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();

        for (LandingArgument.Argument argument : LandingArgument.Argument.values()){
            for (String s : argument.getLandingArgumentClass().getArguments()){
                optionParser.accepts( s );
            }
        }

        OptionSet set = optionParser.parse(args);

        for (LandingArgument.Argument argument : LandingArgument.Argument.values()){
            for (String s : argument.getLandingArgumentClass().getArguments()){
                if (set.has( s )){
                    argument.getLandingArgumentClass().has();
                    break;
                }
            }
        }

        for (int i = 0; i < args.length; i++) {
            final String arg = args[i].toLowerCase();
            switch (arg.toLowerCase()) {
                case "-createdirs":
                    Directories.validate();
                    System.out
                            .println(TranslationHelper.translate(("DIRECTORIES_CREATED")));
                    System.exit(0);
                    break;
                case "-v":
                case "-verbose":
                    Core.setVerbose(true);
                    break;
                case "-server":
                    ServerSelector.initServer = args[++i];
                    break;
                case "-dump":
                    Core.setDump(true);
                    break;
                case "-scriptsbin":
                    Directories.setScriptCompiledDirectory(new File(args[++i]));
                    break;
                case "-serversbin":
                    Directories.setServerCompiledDirectory(new File(args[++i]));
                    break;
                case "-clearcache":
                    Directories.clearCache();
                    break;
                case "-mac":
                    byte[] mac = new byte[6];
                    String str = args[++i];
                    if (str.toLowerCase().equals("random")) {
                        new java.util.Random().nextBytes(mac);
                    } else {
                        i--;
                        for (int j = 0; j < 6; j++) {
                            mac[j] = Byte.parseByte(args[++i], 16); // parses a hex
                            // number
                        }
                    }
                    NetworkInterface.setMac(mac);
                    break;
                case "-proxy":
                    ProxyType type = ProxyType.valueOf(args[++i].toUpperCase());
                    if (type == null) {
                        System.err.println(TranslationHelper.translate("INVALID_PROXY_TYPE") + args[i]);
                        System.exit(1);
                        return;
                    }
                    ProxySocket.setProxy(type, args[++i],
                            Integer.parseInt(args[++i]));
                    break;
                case "-auth":
                    ProxySocket.auth = true;
                    ProxySocket.setLogin(args[++i], args[++i]);
                    break;
                case "-no_sec":
                    Core.disableSec();
                    break;
                case "-no_validation":
                    Core.disableValidation();
                    break;
            }

            for (Core.LaunchMode mode : Core.LaunchMode.values()) {
                if (arg.equalsIgnoreCase(mode.getArg())) {
                    Core.setMode(mode);
                    break;
                }
            }
        }
    }
}
