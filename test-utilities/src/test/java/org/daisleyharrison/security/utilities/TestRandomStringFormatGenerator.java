package org.daisleyharrison.security.utilities;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.daisleyharrison.security.utilities.typegenerators.RandomStringFormatGenerator;

public class TestRandomStringFormatGenerator {

    @Test
    public void testFormatEmail() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*",
                "<first-name>.<last-name>@{outlook gmail yahoo yelp linkedin}.com");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatPhone() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*",
                "{1-}[0.5]\\({1 2 3 4 5 6 7 8 9}<digit>[2]\\)<digit>[3]-<digit>[4]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatPhoneMacro() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<phone-number>");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatAlphaUpper() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<alpha-upper>|,|[1-26]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatAlphaLower() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<alpha-lower>|,|[1-26]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatAlphaMixed() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<alpha-mixed>|,|[1-52]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatNumeric() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<digit>|,|[1-62]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatDouble() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<double>|,|[1-10]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatDoubleE() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<double>|,|%e|[1-10]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatFloat() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<float>|,|[1-10]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatInteger() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<integer>|,|[1-10]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatBoolean() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<boolean>|,|[1-10]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatDate() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<date>|,|[1-10]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatDateFmt() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*",
                "<date>|, |%tm/%<td/%<tY %<tH:%<tm:%<tS|[1-10]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatAlphaNumericMixed() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<alpha-numeric-mixed>|,|[1-62]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatFirstName() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<first-name>|, |[1 - 10]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatLastName() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<last-name> |, |  [ 1 - 10 ]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatNames() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*",
                "<first-name> <alpha-upper>. <last-name>");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testNextInt01() throws Exception {
        boolean found0 = false;
        boolean found1 = false;
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*",
                "<first-name> <alpha-upper>. <last-name>");
        for (int i = 0; i < 100; i++) {
            int actual = RandomHelper.nextInt(0, 1);
            if (actual == 0) {
                found0 = true;
            }
            if (actual == 0) {
                found1 = true;
            }
            if (found0 && found1) {
                break;
            }
        }
        assertTrue(found0 && found1);
    }

    @Test
    public void testFormatStreet() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*",
                "<digit>[1-6] <last-name> {Street Ave St Court Ct Avenue} {N S E W NE SE NW SW North South}[0.5]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatCityCountryZip() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<us-city> USA <digit>[5]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatLorumWords() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<lorem-word>| |[5-50]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatLorumParagraphs() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<lorem-paragraph>| |[5-50]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatUserNames() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*",
                "<english-word:digify><separator>[0.5]<english-word:digify><digit>[0.5 4]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testBraces() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*",
                "( 1 2 buckle my \\(web page\\) at <web-url>)| ## |[1-4]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testWebUrlMacro() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<web-url>");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testPictureUrlMacro() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<image-url>");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testModifierNoop() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<lorem-word:noop>|,|[0-5]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testModifierUpper() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<lorem-word:upper>|,|[0-5]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testModifierLower() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<lorem-word:lower>|,|[0-5]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testModifierDigify() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<english-word:digify>|, |[0-5]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testModifierTitle() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*",
                "<lorem-paragraph:title>| *** |[0.3 5]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testModifierCamel() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*",
                "<lorem-paragraph:camel>| *** |[0.3 5]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testFormatPassword() throws Exception {
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "<alpha-numeric-mixed>[8-16]");
        for (int i = 0; i < 10; i++) {
            String actual = gen.apply();
            System.out.println(actual);
            assertNotNull(actual);
        }
    }

    @Test
    public void testMax() throws Exception {
        boolean foundEmpty = false;
        boolean foundPresent = false;
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "{present}[0.5]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            if (actual.isEmpty()) {
                foundEmpty = true;
            } else {
                foundPresent = true;
            }
        }
        assertTrue(foundEmpty);
        assertTrue(foundPresent);
    }

    @Test
    public void testMinMax01() throws Exception {
        boolean foundEmpty = false;
        boolean foundPresent = false;
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "{present}[0-1]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            if (actual.isEmpty()) {
                foundEmpty = true;
            } else {
                foundPresent = true;
            }
        }
        assertTrue(foundEmpty);
        assertTrue(foundPresent);
    }

    @Test
    public void testMinMax13() throws Exception {
        boolean foundEmpty = false;
        boolean foundPresent = false;
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "{present}[1-3]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            if (actual.isEmpty()) {
                foundEmpty = true;
            } else {
                foundPresent = true;
            }
        }
        assertFalse(foundEmpty);
        assertTrue(foundPresent);
    }

    @Test
    public void testMinMax_33() throws Exception {
        boolean foundEmpty = false;
        boolean foundPresent = false;
        RandomStringFormatGenerator gen = new RandomStringFormatGenerator(".*", "{present}[0.5 3]");
        for (int i = 0; i < 100; i++) {
            String actual = gen.apply();
            if (actual.isEmpty()) {
                foundEmpty = true;
            } else {
                foundPresent = true;
            }
        }
        assertTrue(foundEmpty);
        assertTrue(foundPresent);
    }

}