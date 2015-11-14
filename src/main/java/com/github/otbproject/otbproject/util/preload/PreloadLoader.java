package com.github.otbproject.otbproject.util.preload;

import com.github.otbproject.otbproject.App;
import com.github.otbproject.otbproject.command.Alias;
import com.github.otbproject.otbproject.command.Aliases;
import com.github.otbproject.otbproject.command.Command;
import com.github.otbproject.otbproject.command.Commands;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.database.Databases;
import com.github.otbproject.otbproject.filter.BasicFilter;
import com.github.otbproject.otbproject.filter.FilterGroup;
import com.github.otbproject.otbproject.filter.FilterGroups;
import com.github.otbproject.otbproject.filter.Filters;
import com.github.otbproject.otbproject.fs.FSUtil;
import com.github.otbproject.otbproject.fs.PathBuilder;
import com.github.otbproject.otbproject.fs.groups.Base;
import com.github.otbproject.otbproject.fs.groups.Chan;
import com.github.otbproject.otbproject.fs.groups.Load;
import com.github.otbproject.otbproject.util.JsonHandler;
import com.github.otbproject.otbproject.util.Watcher;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// TODO rewrite to use class hierarchy
public class PreloadLoader {
    private PreloadLoader() {}

    public static void loadDirectoryForEachChannel(Base base, LoadStrategy strategy) {
        FSUtil.streamDirectory(new File(FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS),
                "Failed to load objects of type '" + base.toString() + "' for each channel - unable to get list of channels")
                .filter(File::isDirectory)
                .forEach(file -> loadDirectory(base, Chan.SPECIFIC, file.getName(), strategy));
    }

    public static void loadDirectory(Base base, Chan chan, String channelName, LoadStrategy strategy) {
        List<PreloadPair<?>> list = loadDirectoryContents(base, chan, channelName, strategy);
        if (list.isEmpty()) {
            return;
        }

        switch (chan) {
            case ALL:
                loadForAllChannels(list, channelName, base, strategy);
                break;
            case SPECIFIC:
                loadForChannel(list, channelName, base, strategy);
                break;
            case BOT:
                loadForBotChannel(list, base, strategy);
                break;
        }
    }

    private static void loadForAllChannels(List<PreloadPair<?>> list, String channelName, Base base, LoadStrategy strategy) {
        if (channelName != null) {
            loadForChannel(list, channelName, base, strategy);
            return;
        }

        App.logger.info("Loading objects of type '" + base.toString() + "' for all channels");
        FSUtil.streamDirectory(new File(FSUtil.dataDir() + File.separator + FSUtil.DirNames.CHANNELS),
                "Failed to load objects of type '" + base.toString() + "' for each channel - unable to get list of channels")
                .filter(File::isDirectory)
                .forEach(file -> loadForChannel(list, file.getName(), base, strategy));
        App.logger.info("Finished loading objects of type '" + base.toString() + "' for all channels");
    }

    private static void loadForChannel(List<PreloadPair<?>> list, String channel, Base base, LoadStrategy strategy) {
        App.logger.info("Loading " + list.size() + " object(s) of type '" + base.toString() + "' for channel: " + channel);
        DatabaseWrapper db = Databases.createChannelMainDbWrapper(channel);
        if (db == null) {
            App.logger.error("Failed to load objects of type '" + base.toString() + "' for channel '" + channel + "' - unable to get database");
            return;
        }
        long successful = loadFromList(list, db, base, strategy);
        App.logger.info("Successfully loaded " + successful + " object(s) of type '" + base.toString() + "' for channel: " + channel);
        if (successful != list.size()) {
            App.logger.warn((list.size() - successful) + " object(s) failed to load");
        }
    }

    private static void loadForBotChannel(List<PreloadPair<?>> list, Base base, LoadStrategy strategy) {
        App.logger.info("Loading " + list.size() + " object(s) of type '" + base.toString() + "' for bot channel");
        DatabaseWrapper db = Databases.createBotDbWrapper();
        if (db == null) {
            App.logger.error("Failed to load objects of type '" + base.toString() + "' for bot channel - unable to get bot database");
            return;
        }
        long successful = loadFromList(list, db, base, strategy);
        App.logger.info("Successfully loaded " + successful + " object(s) of type '" + base.toString() + "' for bot channel");
        if (successful != list.size()) {
            App.logger.warn((list.size() - successful) + " object(s) failed to load");
        }
    }

    private static long loadFromList(List<PreloadPair<?>> list, DatabaseWrapper db, Base base, LoadStrategy strategy) {
        return list.stream()
                .filter(preloadPair -> loadObjectUsingStrategy(db, preloadPair.tNew, preloadPair.tOld, base, strategy))
                .count();
    }

    private static <T> boolean loadObjectUsingStrategy(DatabaseWrapper db, T tNew, T tOld, Base base, LoadStrategy strategy) {
        try {
            switch (base) {
                case ALIAS:
                    Alias alias = (strategy == LoadStrategy.UPDATE) ?
                            PreloadComparator.generateAliasHybrid(db, (Alias) tNew, (Alias) tOld) : (Alias) tNew;
                    if (alias == null) {
                        return false;
                    }
                    if (Aliases.addAliasFromObj(db, alias)) {
                        App.logger.debug("Loaded alias: " + alias.getName());
                        return true;
                    } else {
                        App.logger.error("Failed to load alias: " + alias.getName());
                        return false;
                    }
                case CMD:
                    Command command = (strategy == LoadStrategy.UPDATE) ?
                            PreloadComparator.generateCommandHybrid(db, (Command) tNew, (Command) tOld) : (Command) tNew;
                    if (command == null) {
                        return false;
                    }
                    if (Commands.addCommandFromObj(db, command)) {
                        App.logger.debug("Loaded command: " + command.getName());
                        return true;
                    } else {
                        App.logger.error("Failed to load command: " + command.getName());
                        return false;
                    }
                case FILTER:
                    BasicFilter filter = (strategy == LoadStrategy.UPDATE) ?
                            PreloadComparator.generateFilterHybrid(db, (BasicFilter) tNew, (BasicFilter) tOld) : (BasicFilter) tNew;
                    if (filter == null) {
                        break;
                    }
                    if (Filters.addFilterFromObj(db, filter)) {
                        App.logger.debug("Loaded filter: " + filter.getData());
                        return true;
                    } else {
                        App.logger.error("Failed to load filter: " + filter.getData());
                        return false;
                    }
                case FILTER_GRP:
                    FilterGroup group = (strategy == LoadStrategy.UPDATE) ?
                            PreloadComparator.generateFilterGroupHybrid(db, (FilterGroup) tNew, (FilterGroup) tOld) : (FilterGroup) tNew;
                    if (group == null) {
                        break;
                    }
                    if (FilterGroups.addFilterGroupFromObj(db, group)) {
                        App.logger.debug("Loaded filter group: " + group.getName());
                        return true;
                    } else {
                        App.logger.error("Failed to load filter group: " + group.getName());
                        return false;
                    }
                default:
                    App.logger.warn("Unable to load object into database based on base: " + base.toString());
            }
        } catch (ClassCastException e) {
            App.logger.catching(e);
            Watcher.logException();
        }
        return false;
    }

    private static List<PreloadPair<?>> loadDirectoryContents(Base base, Chan chan, String channelName, LoadStrategy strategy) {
        if ((chan == Chan.SPECIFIC) && (channelName == null)) {
            return Collections.emptyList();
        }
        File dir;
        PathBuilder builder = new PathBuilder();
        if (strategy == LoadStrategy.FROM_LOADED) {
            dir = builder.base(base).channels(chan).forChannel(channelName).load(Load.ED).asFile();
        } else {
            dir = builder.base(base).channels(chan).forChannel(channelName).load(Load.TO).asFile();
        }

        final Class<?> tClass = getClassFromBase(base);
        if (tClass == null) {
            App.logger.error("Unable to determine class to load as for base: " + base.toString());
            return Collections.emptyList();
        }

        List<PreloadPair<?>> list = FSUtil.streamDirectory(dir)
                .map(file -> {
                    String name = file.getName();
                    String pathOld = builder.base(base).channels(chan).forChannel(channelName).load(Load.ED).create() + File.separator + name;
                    String pathNew;
                    if (strategy == LoadStrategy.FROM_LOADED) {
                        pathNew = pathOld;
                    } else {
                        pathNew = builder.base(base).channels(chan).forChannel(channelName).load(Load.TO).create() + File.separator + name;
                    }
                    String pathFail = builder.base(base).channels(chan).forChannel(channelName).load(Load.FAIL).create() + File.separator + name;
                    return loadFromFile(pathNew, pathOld, pathFail, tClass, strategy);
                })
                .filter(preloadPair -> preloadPair.tNew != null)
                .collect(Collectors.toList());
        if (list.size() > 0) {
            App.logger.info("Read " + list.size() + " object(s) of type '" + base.toString() + "' from directory: " + dir.getPath());
        }
        return list;
    }

    private static <T> PreloadPair<T> loadFromFile(String pathNew, String pathOld, String pathFail, Class<T> tClass, LoadStrategy strategy) {
        App.logger.debug("Attempting to load from file: " + pathNew);
        T tNew = JsonHandler.readValue(pathNew, tClass).orElse(null); // TODO change orElse()
        T tOld;
        if (strategy == LoadStrategy.UPDATE) {
            tOld = JsonHandler.readValue(pathOld, tClass).orElse(null); // TODO change orElse()
        } else {
            tOld = null;
        }

        if (tNew == null) {
            doMove(pathNew, pathFail);
            App.logger.warn("Failed to load file: " + pathNew);
        } else {
            doMove(pathNew, pathOld);
            App.logger.debug("Successfully loaded file: " + pathNew);
        }
        return new PreloadPair<>(tNew, tOld);
    }

    private static Class<?> getClassFromBase(Base base) {
        switch (base) {
            case ALIAS:
                return Alias.class;
            case CMD:
                return Command.class;
            case FILTER:
                return BasicFilter.class;
            case FILTER_GRP:
                return FilterGroup.class;
            default:
                return null;
        }
    }

    private static void doMove(String sourcePath, String destPath) {
        if (!move(new File(sourcePath), new File(destPath)) && !sourcePath.equals(destPath)) {
            App.logger.error("Failed to move file '" + sourcePath + "' to file '" + destPath + "'");
        }
    }

    private static boolean move(File source, File dest) {
        // Check if they're the same
        if (source.equals(dest)) {
            return true;
        }
        // Make sure overwrites
        if (dest.exists() && !dest.delete()) {
            App.logger.error("Failed to delete file '" + dest.getPath() + "' when trying to replace it with file '" + source.getPath() + "'");
        }
        return source.renameTo(dest);
    }
}
