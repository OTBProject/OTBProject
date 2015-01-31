package com.github.opentwitchbotteam.otbproject.commands.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ParserTest {
    private static final String TS = "[[";  // Term Start
    private static final String TE = "]]";  // Term End
    private static final String MD = ".";   // Modifier Delimiter
    private static final String ES = "{{";  // Embedded String Start
    private static final String EE = "}}";  // Embedded String End

    private static final String USER = "nthportal";
    private static final int COUNT = 1;
    private static final String CHANNEL = "the_lone_devil";

    @Test
    // Term-independent tests
    public void generalTest() {
        String[] args = new String[2];
        args[0] = "foo";
        args[1] = "bar";

        // InvalidTerm
        String parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"foo"+TE);
        assertEquals(TS+"foo"+TE, parsed);
    }

    @Test
    // Tests [[user]] term, as well as some general term parsing
    public void userTest() {
        String[] args = new String[2];
        args[0] = "foo";
        args[1] = "bar";

        // Basic Test
        // Term at beginning of string
        String parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"user"+TE+" says hi.");
        assertEquals("nthportal says hi.", parsed);

        // Term at end of string
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Go to twitch.tv/"+TS+"user"+TE);
        assertEquals("Go to twitch.tv/nthportal", parsed);

        // Term in middle of string
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "start-"+TS +"user"+TE +"-end");
        assertEquals("start-nthportal-end", parsed);

        // Multiple times in one string, next to each other
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"user"+TE+TS +"user"+TE);
        assertEquals("nthportalnthportal", parsed);

        // Multiple times in one string, apart
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"user"+TE+"-foo-"+TS +"user"+TE);
        assertEquals("nthportal-foo-nthportal", parsed);

        // Space in term
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"user "+TE+TS +"user"+TE);
        assertNotEquals("nthportalnthportal", parsed);

        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"user"+TE+TS +"user "+TE);
        assertNotEquals("nthportalnthportal", parsed);

        // Modifiers work for term in general
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"user"+MD+ModifierTypes.UPPER+TE);
        assertEquals("NTHPORTAL", parsed);
    }

    @Test
    // Test [[channel]] term
    public void channelTest() {
        String[] args = new String[2];
        args[0] = "foo";
        args[1] = "bar";

        String parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "You are in "+TS+"channel"+TE+"'s channel.");
        assertEquals("You are in the_lone_devil's channel.", parsed);

        // Modifiers work for term in general
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"channel"+MD+ModifierTypes.UPPER+TE);
        assertEquals("THE_LONE_DEVIL", parsed);
    }

    @Test
    // Test [[count]] term
    public void countTest() {
        String[] args = new String[2];
        args[0] = "foo";
        args[1] = "bar";

        // count == 0
        int count = 0;
        String parsed = CommandResponseParser.parse(USER, CHANNEL, count, args, "This command has been run "+TS+"count"+TE+" times.");
        assertEquals("This command has been run 0 times.", parsed);

        // large count
        count = 30000;
        parsed = CommandResponseParser.parse(USER, CHANNEL, count, args, "This command has been run "+TS+"count"+TE+" times.");
        assertEquals("This command has been run 30000 times.", parsed);

        // negative count (shouldn't ever happen, but testing for it anyway)
        count = -30000;
        parsed = CommandResponseParser.parse(USER, CHANNEL, count, args, "This command has been run "+TS+"count"+TE+" times.");
        assertEquals("This command has been run -30000 times.", parsed);
    }

    @Test
    // Test [[args{{default}}]] term, as well as defaults
    // also tests for empty embedded string and term args
    public void argsTest() {
        // Empty args
        String[] args = new String[0];
        String parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi "+TS+"args"+TE+".");
        assertEquals("Hi .", parsed);

        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi "+TS+"args"+ ES +"person"+ EE +TE+".");
        assertEquals("Hi person.", parsed);

        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi "+TS+"args"+ ES +TS+"user"+TE+ EE +TE+".");
        assertEquals("Hi nthportal.", parsed);

        // Empty default (tests for any empty embedded string)
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi "+TS+"args"+ES+EE+TE+".");
        assertEquals("Hi .", parsed);

        // 1 arg
        args = new String[1];
        args[0] = "Justin";

        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi "+TS+"args"+TE+".");
        assertEquals("Hi Justin.", parsed);

        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi "+TS+"args"+ ES +"person"+ EE +TE+".");
        assertEquals("Hi Justin.", parsed);

        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi "+TS+"args"+ ES +TS+"user"+TE+ EE +TE+".");
        assertEquals("Hi Justin.", parsed);

        // 2 args
        args = new String[2];
        args[0] = "awesome";
        args[1] = "people";

        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi "+TS+"args"+TE+".");
        assertEquals("Hi awesome people.", parsed);

        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi "+TS+"args"+ ES +"person"+ EE +TE+".");
        assertEquals("Hi awesome people.", parsed);

        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi "+TS+"args"+ ES +TS+"user"+TE+ EE +TE+".");
        assertEquals("Hi awesome people.", parsed);

        // Modifiers work for term in general
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"args"+MD+ModifierTypes.UPPER+TE);
        assertEquals("AWESOME PEOPLE", parsed);

        // Terms as args (should not be parsed)
        args[0] = TS+"user"+TE;
        args[1] = TS+"channel"+TE;
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"args"+TE);
        assertNotEquals(USER + " " + CHANNEL, parsed);

        // Embedded string as args
        args[0] = ES+"string"+EE;
        args[1] = ES+"thing"+EE;
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "start-"+TS+"args"+TE+"-end");
        assertEquals("start- { {string} } { {thing} } -end", parsed);
    }

    @Test
    // Test [[ifargs{{string}}]]
    public void ifargsTest() {
        // Empty args
        String[] args = new String[0];
        String parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi"+TS+"ifargs"+ES+" "+EE+TE+TS+"args"+TE+".");
        assertEquals("Hi.", parsed);

        // 1 arg
        args = new String[1];
        args[0] = "Justin";
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi"+TS+"ifargs"+ES+" "+EE+TE+TS+"args"+TE+".");
        assertEquals("Hi Justin.", parsed);

        // 2 args
        args = new String[2];
        args[0] = "awesome";
        args[1] = "people";
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "Hi"+TS+"ifargs"+ES+" "+EE+TE+TS+"args"+TE+".");
        assertEquals("Hi awesome people.", parsed);
    }

    @Test
    // Test [[ifargN{{string}}]] term (N is a natural number)
    public void ifargNTest() {
        String rawMsg = "Watch all perspectives at http://kadgar.net/live/"+TS+"channel"+TE+"/"
                +TS+"arg1"+TE+TS+"ifarg1"+ES+"/"+EE+TE+TS+"arg2"+TE+TS+"ifarg2"+ES+"/"+EE+TE
                +TS+"arg3"+TE+TS+"ifarg3"+ES+"/"+EE+TE+TS+"arg4"+TE+TS+"ifarg4"+ES+"/"+EE+TE;

        // 0 args
        String[] args = new String[0];
        String parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("Watch all perspectives at http://kadgar.net/live/the_lone_devil/", parsed);

        // 1 arg
        args = "maddiiemaneater".split(" ");
        assertEquals(1, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("Watch all perspectives at http://kadgar.net/live/the_lone_devil/maddiiemaneater/", parsed);

        // 2 args
        args = "maddiiemaneater mktheworst".split(" ");
        assertEquals(2, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("Watch all perspectives at http://kadgar.net/live/the_lone_devil/maddiiemaneater/mktheworst/", parsed);

        // 3 args
        args = "maddiiemaneater mktheworst aureylian".split(" ");
        assertEquals(3, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("Watch all perspectives at http://kadgar.net/live/the_lone_devil/maddiiemaneater/mktheworst/aureylian/", parsed);

        // 4 args
        args = "maddiiemaneater mktheworst aureylian nthportal".split(" ");
        assertEquals(4, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("Watch all perspectives at http://kadgar.net/live/the_lone_devil/maddiiemaneater/mktheworst/aureylian/nthportal/", parsed);

        // 5 args
        args = "maddiiemaneater mktheworst aureylian nthportal nefarious411".split(" ");
        assertEquals(5, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("Watch all perspectives at http://kadgar.net/live/the_lone_devil/maddiiemaneater/mktheworst/aureylian/nthportal/", parsed);
    }

    @Test
    // Test [[argN{{default}}]] term (N is a natural number)
    public void argNTest() {
        // Empty args
        String[] args = new String[0];
        String rawMsg = "words "+TS+"arg1"+ES+"something"+EE+TE+" words "+TS+"arg2"+ES
                +TS+"arg1"+TE+EE+TE+" words "+TS+"arg3"+ES+TS+"arg2"+ES
                +TS+"arg1"+ES+"dunno"+EE+TE+EE+TE+EE+TE
                +" words "+TS+"arg4"+ES+TS+"arg2"+TE+EE+TE+" words";
        String parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("words something words  words dunno words  words", parsed);

        // 1 arg
        args = "one".split(" ");
        assertEquals(1, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("words one words one words one words  words", parsed);

        // 2 args
        args = "one two".split(" ");
        assertEquals(2, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("words one words two words two words two words", parsed);

        // 3 args
        args = "one two three".split(" ");
        assertEquals(3, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("words one words two words three words two words", parsed);

        // 4 args
        args = "one two three four".split(" ");
        assertEquals(4, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("words one words two words three words four words", parsed);

        // 5 args
        args = "one two three four five".split(" ");
        assertEquals(5, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("words one words two words three words four words", parsed);

        // 2 digit arg number (11)
        args = "1 2 3 4 5 6 7 8 9 10 11".split(" ");
        assertEquals(11, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"arg11"+TE);
        assertEquals("11", parsed);
    }

    @Test
    // Test [[foreach{{prepend}}{{append}}]]
    public void foreachTest() {
        String rawMsg = "Watch all perspectives at http://kadgar.net/live/"+TS+"channel"+TE+"/"
                +TS+"foreach"+ES+EE+ES+"/"+EE+TE;

        // 0 args
        String[] args = new String[0];
        String parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("Watch all perspectives at http://kadgar.net/live/the_lone_devil/", parsed);

        // 1 arg
        args = "maddiiemaneater".split(" ");
        assertEquals(1, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("Watch all perspectives at http://kadgar.net/live/the_lone_devil/maddiiemaneater/", parsed);

        // 2 args
        args = "maddiiemaneater mktheworst".split(" ");
        assertEquals(2, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("Watch all perspectives at http://kadgar.net/live/the_lone_devil/maddiiemaneater/mktheworst/", parsed);

        rawMsg = "Here are some channels: ["+TS+"channel"+TE+"]"+TS+"foreach"+ES+" ["+EE+ES+"]"+EE+TE;
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("Here are some channels: [the_lone_devil] [maddiiemaneater] [mktheworst]", parsed);

        // Modifiers work for term in general
        rawMsg = "Here are some channels: ["+TS+"channel"+TE+"]"+TS+"foreach"+MD+ModifierTypes.FIRST_CAP+ES+" ["+EE+ES+"]"+EE+TE;
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, rawMsg);
        assertEquals("Here are some channels: [the_lone_devil] [Maddiiemaneater] [Mktheworst]", parsed);
    }

    @Test
    // Test [[numargs]]
    public void numargsTest() {
        // 0 args
        String[] args = new String[0];
        String parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "There are "+TS+"numargs"+TE+" args.");
        assertEquals("There are 0 args.", parsed);

        // 1 arg
        args = "maddiiemaneater".split(" ");
        assertEquals(1, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "There is "+TS+"numargs"+TE+" arg.");
        assertEquals("There is 1 arg.", parsed);

        // 2 args
        args = "maddiiemaneater mktheworst".split(" ");
        assertEquals(2, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "There are "+TS+"numargs"+TE+" args.");
        assertEquals("There are 2 args.", parsed);

        // 3 args
        args = "maddiiemaneater mktheworst aureylian".split(" ");
        assertEquals(3, args.length);
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, "There are "+TS+"numargs"+TE+" args.");
        assertEquals("There are 3 args.", parsed);
    }

    @Test
    // Test modifiers ([[args]] used for completeness)
    public void modifierTest() {
        String[] args = "this is a TEST sentence.".split(" ");

        // lower
        String parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"args"+MD+ModifierTypes.LOWER+TE);
        assertEquals("this is a test sentence.", parsed);

        // upper
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"args"+MD+ModifierTypes.UPPER+TE);
        assertEquals("THIS IS A TEST SENTENCE.", parsed);

        // first_cap
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"args"+MD+ModifierTypes.FIRST_CAP+TE);
        assertEquals("This is a test sentence.", parsed);

        // word_cap
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"args"+MD+ModifierTypes.WORD_CAP+TE);
        assertEquals("This Is A Test Sentence.", parsed);

        // first_cap_soft
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"args"+MD+ModifierTypes.FIRST_CAP_SOFT+TE);
        assertEquals("This is a TEST sentence.", parsed);

        // word_cap_soft
        parsed = CommandResponseParser.parse(USER, CHANNEL, COUNT, args, TS+"args"+MD+ModifierTypes.WORD_CAP_SOFT+TE);
        assertEquals("This Is A TEST Sentence.", parsed);
    }
}
