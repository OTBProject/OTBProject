package com.github.otbproject.otbproject.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultConfigGeneratorTest {

    @Test
    public void defaultChannelConfigShouldBeCreatedWithNoNullElements() {
        ChannelConfig config = DefaultConfigGenerator.createChannelConfig();
        //Test that nothing that can be null, is null
        assertNotNull(config.getCommandCooldown());
        assertNotNull(config.isEnabled());
        assertNotNull(config.queueLimits);
        assertNotNull(config.queueLimits.getDefaultPriorityLimit());
        assertNotNull(config.queueLimits.getHighPriorityLimit());
        assertNotNull(config.userCooldowns);
        assertNotNull(config.userCooldowns.getUl_default());
        assertNotNull(config.userCooldowns.getUl_regular());
        assertNotNull(config.userCooldowns.getUl_subscriber());
    }

    @Test
    public void defaultChannelConfigShouldHaveAllTheDefaultValues(){
        ChannelConfig config = DefaultConfigGenerator.createChannelConfig();
        assertNotNull(config);
        assertEquals(config.getCommandCooldown(), Integer.valueOf(8));
        assertNotNull(config.userCooldowns);
        assertEquals(config.userCooldowns.getUl_internal(), 0);
        assertEquals(config.userCooldowns.getUl_broadcaster(), 0);
        assertEquals(config.userCooldowns.getUl_super_moderator(), 0);
        assertEquals(config.userCooldowns.getUl_moderator(), 0);
        assertEquals(config.userCooldowns.getUl_regular(), Integer.valueOf(15));
        assertEquals(config.userCooldowns.getUl_subscriber(), Integer.valueOf(30));
        assertEquals(config.userCooldowns.getUl_default(), Integer.valueOf(30));
        assertFalse(config.isDebug());
        assertTrue(config.isEnabled());
        assertFalse(config.isSilenced());
        assertNotNull(config.queueLimits);
        assertEquals(config.queueLimits.getHighPriorityLimit(), Integer.valueOf(-1));
        assertEquals(config.queueLimits.getDefaultPriorityLimit(), Integer.valueOf(5));
        assertEquals(config.queueLimits.getLowPriorityLimit(), 0);
    }

    @Test
    public void defaultAccountConfigShouldBeCreatedWithNoNullElements(){
        Account config = DefaultConfigGenerator.createAccountConfig();
        assertNotNull(config);
        assertNotNull(config.getName());
        assertNotNull(config.getPasskey());
    }
    @Test
    public void defaultAccountConfigShouldHaveAllTheDefaultValues(){
        Account config = DefaultConfigGenerator.createAccountConfig();
        assertNotNull(config);
        assertEquals(config.getName(), "your_name_here");
        assertEquals(config.getPasskey(), "your_passkey_here");
    }

    @Test
    public void defaultBotConfigShouldBeCreatedWithNoNullElements(){
        BotConfig config = DefaultConfigGenerator.createBotConfig();
        assertNotNull(config);
        assertNotNull(config.getChannelJoinSetting());
        assertNotNull(config.getMessageSendDelayInMilliseconds());
        assertNotNull(config.isBotChannelDebug());
        assertNotNull(config.blacklist);
        assertNotNull(config.whitelist);
        assertNotNull(config.currentChannels);
    }
    @Test
    public void defaultBotConfigShouldHaveAllTheDefaultValues(){
        BotConfig config = DefaultConfigGenerator.createBotConfig();
        assertNotNull(config);
        assertEquals(config.getChannelJoinSetting(), ChannelJoinSetting.NONE);
        assertEquals(config.getMessageSendDelayInMilliseconds(), Integer.valueOf(1600));
        assertTrue(config.isBotChannelDebug());
    }

    @Test
    public void defaultGeneralConfigShouldBeCreatedWithNoNullElements(){
        GeneralConfig config = DefaultConfigGenerator.createGeneralConfig();
        assertNotNull(config);
        assertNotNull(config.getIp_binding());
        assertNotNull(config.getServiceName());
        assertNotNull(config.permanently_enabled_commands);

    }
    @Test
    public void defaultGeneralConfigShouldHaveAllTheDefaultValues(){
        GeneralConfig config = DefaultConfigGenerator.createGeneralConfig();
        assertNotNull(config);
        assertEquals(config.getIp_binding(), "0.0.0.0");
        assertEquals(config.getServiceName(), ServiceName.TWITCH);
        assertEquals(config.getPortNumber(), 80);
        assertArrayEquals(config.permanently_enabled_commands.toArray(), new String[]{"!bot-enable-meta"});
    }

}
