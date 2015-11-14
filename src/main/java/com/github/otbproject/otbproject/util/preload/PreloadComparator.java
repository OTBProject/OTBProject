package com.github.otbproject.otbproject.util.preload;

import com.github.otbproject.otbproject.command.Alias;
import com.github.otbproject.otbproject.command.Aliases;
import com.github.otbproject.otbproject.command.Command;
import com.github.otbproject.otbproject.command.Commands;
import com.github.otbproject.otbproject.database.DatabaseWrapper;
import com.github.otbproject.otbproject.filter.BasicFilter;
import com.github.otbproject.otbproject.filter.FilterGroup;
import com.github.otbproject.otbproject.filter.FilterGroups;
import com.github.otbproject.otbproject.filter.Filters;

import java.util.Optional;

class PreloadComparator {
    private PreloadComparator() {}

    static Alias generateAliasHybrid(DatabaseWrapper db, Alias newAlias, Alias oldAlias) {
        if ((oldAlias == null) || (newAlias == null) || !oldAlias.getName().equals(newAlias.getName())) {
            return newAlias;
        }

        Optional<Alias> optional = Aliases.get(db, newAlias.getName());
        if (!optional.isPresent()) {
            return newAlias;
        }
        Alias dbAlias = optional.get();

        // Check all alias fields
        if (!oldAlias.getCommand().equals(dbAlias.getCommand())) {
            newAlias.setCommand(dbAlias.getCommand());
        }
        if (!oldAlias.getModifyingUserLevel().equals(dbAlias.getModifyingUserLevel())) {
            newAlias.setModifyingUserLevel(dbAlias.getModifyingUserLevel());
        }
        if (oldAlias.isEnabled() != dbAlias.isEnabled()) {
            newAlias.setEnabled(dbAlias.isEnabled());
        }

        return newAlias;
    }

    static Command generateCommandHybrid(DatabaseWrapper db, Command newCommand, Command oldCommand) {
        if ((oldCommand == null) || (newCommand == null) || !oldCommand.getName().equals(newCommand.getName())) {
            return newCommand;
        }

        Optional<Command> optional = Commands.get(db, newCommand.getName());
        if (!optional.isPresent()) {
            return newCommand;
        }
        Command dbCommand = optional.get();

        // Check all command fields
        if (!oldCommand.getResponse().equals(dbCommand.getResponse())) {
            newCommand.setResponse(dbCommand.getResponse());
        }
        if (!oldCommand.getExecUserLevel().equals(dbCommand.getExecUserLevel())) {
            newCommand.setExecUserLevel(dbCommand.getExecUserLevel());
        }
        if (oldCommand.getMinArgs() != dbCommand.getMinArgs()) {
            newCommand.setMinArgs(dbCommand.getMinArgs());
        }
        // Skip count
        if (!oldCommand.modifyingUserLevels.getNameModifyingUL().equals(dbCommand.modifyingUserLevels.getNameModifyingUL())) {
            newCommand.modifyingUserLevels.setNameModifyingUL(dbCommand.modifyingUserLevels.getNameModifyingUL());
        }
        if (!oldCommand.modifyingUserLevels.getResponseModifyingUL().equals(dbCommand.modifyingUserLevels.getResponseModifyingUL())) {
            newCommand.modifyingUserLevels.setResponseModifyingUL(dbCommand.modifyingUserLevels.getResponseModifyingUL());
        }
        if (!oldCommand.modifyingUserLevels.getUserLevelModifyingUL().equals(dbCommand.modifyingUserLevels.getUserLevelModifyingUL())) {
            newCommand.modifyingUserLevels.setUserLevelModifyingUL(dbCommand.modifyingUserLevels.getUserLevelModifyingUL());
        }
        // Handle script
        if (    ((oldCommand.getScript() == null) && (dbCommand.getScript() != null))
                || ((oldCommand.getScript() != null) && !oldCommand.getScript().equals(dbCommand.getScript()))  ) {
            newCommand.setScript(dbCommand.getScript());
        }
        if (oldCommand.isEnabled() != dbCommand.isEnabled()) {
            newCommand.setEnabled(dbCommand.isEnabled());
        }
        if (oldCommand.isDebug() != dbCommand.isDebug()) {
            newCommand.setDebug(dbCommand.isDebug());
        }

        return newCommand;
    }

    static BasicFilter generateFilterHybrid(DatabaseWrapper db, BasicFilter newFilter, BasicFilter oldFilter) {
        if ((oldFilter == null) || (newFilter == null)
                || !(oldFilter.getData().equals(newFilter.getData()) && oldFilter.getType().equals(newFilter.getType()))) {
            return newFilter;
        }

        Optional<BasicFilter> optional = Filters.get(db, newFilter.getData(), newFilter.getType());
        if (!optional.isPresent()) {
            return newFilter;
        }
        BasicFilter dbFilter = optional.get();

        // Check all filter fields
        if (!oldFilter.getGroup().equals(dbFilter.getGroup())) {
            newFilter.setGroup(dbFilter.getGroup());
        }
        if (oldFilter.isEnabled() != dbFilter.isEnabled()) {
            newFilter.setEnabled(dbFilter.isEnabled());
        }

        return newFilter;
    }

    static FilterGroup generateFilterGroupHybrid(DatabaseWrapper db, FilterGroup newGroup, FilterGroup oldGroup) {
        if ((oldGroup == null) || (newGroup == null) || !oldGroup.getName().equals(newGroup.getName())) {
            return newGroup;
        }

        Optional<FilterGroup> optional = FilterGroups.get(db, newGroup.getName());
        if (!optional.isPresent()) {
            return newGroup;
        }
        FilterGroup dbGroup = optional.get();

        // Check all filter group fields
        if (!oldGroup.getResponseCommand().equals(dbGroup.getResponseCommand())) {
            newGroup.setResponseCommand(dbGroup.getResponseCommand());
        }
        if (!oldGroup.getUserLevel().equals(dbGroup.getUserLevel())) {
            newGroup.setUserLevel(dbGroup.getUserLevel());
        }
        if (!oldGroup.getAction().equals(dbGroup.getAction())) {
            newGroup.setAction(dbGroup.getAction());
        }
        if (oldGroup.isEnabled() != dbGroup.isEnabled()) {
            newGroup.setEnabled(dbGroup.isEnabled());
        }

        return newGroup;
    }
}
