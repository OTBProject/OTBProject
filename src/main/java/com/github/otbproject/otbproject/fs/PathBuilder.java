package com.github.otbproject.otbproject.fs;

import com.github.otbproject.otbproject.fs.groups.Base;
import com.github.otbproject.otbproject.fs.groups.Chan;
import com.github.otbproject.otbproject.fs.groups.Load;

import java.io.File;

public class PathBuilder {
    private Base base;
    private Chan channelDir;
    private String channelName;
    private Load load;

    public PathBuilder() {
        init();
    }

    private void init() {
        base = null;
        channelDir = null;
        channelName = null;
        load = null;
    }

    public PathBuilder base(Base base) {
        this.base = base;
        return this;
    }

    public PathBuilder channels(Chan chan) {
        this.channelDir = chan;
        return this;
    }

    public PathBuilder forChannel(String channelName) {
        this.channelName = channelName;
        return this;
    }

    public PathBuilder load(Load load) {
        this.load = load;
        return this;
    }

    public String create() throws PathBuildException {
        if ((base == null) || (channelDir == null) || (load == null)
                || ((channelDir == Chan.SPECIFIC) && (channelName == null))) {
            throw new PathBuildException();
        }

        StringBuilder path = new StringBuilder();

        switch (base) {
            case ALIAS:
                path.append(FSUtil.aliasesDir());
                break;
            case CMD:
                path.append(FSUtil.commandsDir());
                break;
            case FILTER:
                path.append(FSUtil.filtersDir());
                break;
            case FILTER_GRP:
                path.append(FSUtil.filterGroupsDir());
                break;
        }
        path.append(File.separator);
        switch (channelDir) {
            case ALL:
                path.append(FSUtil.DirNames.ALL_CHANNELS);
                break;
            case BOT:
                path.append(FSUtil.DirNames.BOT_CHANNEL);
                break;
            case SPECIFIC:
                path.append(FSUtil.DirNames.CHANNELS).append(File.separator).append(channelName);
                break;
        }
        path.append(File.separator);
        switch (load) {
            case TO:
                path.append(FSUtil.DirNames.TO_LOAD);
                break;
            case ED:
                path.append(FSUtil.DirNames.LOADED);
                break;
            case FAIL:
                path.append(FSUtil.DirNames.FAILED);
                break;
        }

        init();
        return path.toString();
    }

    public File asFile() {
        return new File(this.create());
    }
}
