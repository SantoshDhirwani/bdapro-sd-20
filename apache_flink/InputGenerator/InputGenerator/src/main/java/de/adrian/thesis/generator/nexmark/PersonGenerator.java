package de.adrian.thesis.generator.nexmark;/*
   NEXMark Generator -- Niagara Extension to XMark Data Generator

   Acknowledgements:
   The NEXMark Generator was developed using the xmlgen generator 
   from the XMark Benchmark project as a basis. The NEXMark
   generator generates streams of auction elements (bids, items
   for auctions, persons) as opposed to the auction files
   generated by xmlgen.  xmlgen was developed by Florian Waas.
   See http://www.xml-benchmark.org for information.

   Copyright (c) Dept. of  Computer Science & Engineering,
   OGI School of Science & Engineering, OHSU. All Rights Reserved.

   Permission to use, copy, modify, and distribute this software and
   its documentation is hereby granted, provided that both the
   copyright notice and this permission notice appear in all copies
   of the software, derivative works or modified versions, and any
   portions thereof, and that both notices appear in supporting
   documentation.

   THE AUTHORS AND THE DEPT. OF COMPUTER SCIENCE & ENGINEERING 
   AT OHSU ALLOW USE OF THIS SOFTWARE IN ITS "AS IS" CONDITION, 
   AND THEY DISCLAIM ANY LIABILITY OF ANY KIND FOR ANY DAMAGES 
   WHATSOEVER RESULTING FROM THE USE OF THIS SOFTWARE.

   This software was developed with support from NSF ITR award
   IIS0086002 and from DARPA through NAVY/SPAWAR 
   Contract No. N66001-99-1-8098.

*/


import de.adrian.thesis.generator.nexmark.data.*;

import java.nio.CharBuffer;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates a person object to be send for the Nexmark Benchmark.
 */
public class PersonGenerator {

    public static int NUM_CATEGORIES = 1000;

    // Currently generate the same person data every time
    private Random random;

    PersonGenerator() {
        this.random = new Random(12343234);
    }

    PersonGenerator(ThreadLocalRandom random) {
        this.random = random;
    }

    class Profile {
        public Vector<String> interests = new Vector<>();

        public boolean hasEducation;
        public boolean hasGender;
        public boolean hasAge;

        public String education;
        public String gender;
        public String business;
        public String age;

        public CharBuffer income = CharBuffer.allocate(30);
    }

    public Profile profile = new Profile();

    public void generateValues(StringBuilder builder, boolean limitAttributes) {
        int ifn = random.nextInt(Firstnames.NUM_FIRSTNAMES);
        int iln = random.nextInt(Lastnames.NUM_LASTNAMES);
        int iem = random.nextInt(Emails.NUM_EMAILS);

        // First Name
        builder.append(",");
        builder.append(Firstnames.FIRSTNAMES[ifn]);
        builder.append(" ");
        builder.append(Lastnames.LASTNAMES[iln]);

        builder.append(",");
        builder.append(Lastnames.LASTNAMES[iln]);
        builder.append("@");
        builder.append(Emails.EMAILS[iem]);

        // Phone number
        builder.append(",");
        builder.append("+");
        builder.append(NumberMapping.STRINGS[random.nextInt(98) + 1]);
        builder.append("(");
        builder.append(NumberMapping.STRINGS[random.nextInt(989) + 10]);
        builder.append(")");
        builder.append(String.valueOf(random.nextInt(9864196) + 123457));

        genAddress(builder);

        // Homepage
        builder.append(",");
        builder.append("http://www.");
        builder.append(Emails.EMAILS[iem]);
        builder.append("/~");
        builder.append(Lastnames.LASTNAMES[iln]);

        // Creditcard
        builder.append(",");
        builder.append(String.valueOf(random.nextInt(9000) + 1000));
        builder.append(" ");
        builder.append(String.valueOf(random.nextInt(9000) + 1000));
        builder.append(" ");
        builder.append(String.valueOf(random.nextInt(9000) + 1000));
        builder.append(" ");
        builder.append(String.valueOf(random.nextInt(9000) + 1000));

        // TODO Add profile as well
        if (limitAttributes) {
            genProfile();
        }
    }

    private void genAddress(StringBuilder builder) {
        int ist = random.nextInt(Lastnames.NUM_LASTNAMES); // street
        int ict = random.nextInt(Cities.NUM_CITIES); // city
        int icn = (random.nextInt(4) != 0) ? 0 : random.nextInt(Countries.NUM_COUNTRIES);
        int ipv = (icn == 0) ? random.nextInt(Provinces.NUM_PROVINCES) : random.nextInt(Lastnames.NUM_LASTNAMES);  // PROVINCES are really states

        // Street
        builder.append(",");
        builder.append(String.valueOf((random.nextInt(99) + 1)));
        builder.append(" ");
        builder.append(Lastnames.LASTNAMES[ist]);
        builder.append(" St");

        // City
        builder.append(",");
        builder.append(Cities.CITIES[ict]);

        // Country
        builder.append(",");
        if (icn == 0) {
            builder.append("United States");
            builder.append(Provinces.PROVINCES[ipv]);
        } else {
            builder.append(Countries.COUNTRIES[icn]);
            builder.append(Lastnames.LASTNAMES[ipv]);
        }

        // Zipcode
        builder.append(",");
        builder.append(String.valueOf(random.nextInt(99999) + 1));
    }

    private void genProfile() {
        if (random.nextBoolean()) {
            profile.hasEducation = true;
            profile.education =
                    Education.EDUCATION[random.nextInt(Education.NUM_EDUCATION)];
        } else {
            profile.hasEducation = false;
        }

        if (random.nextBoolean()) {
            profile.hasGender = true;
            profile.gender = (random.nextInt(2) == 1) ? "male" : "female";
        } else {
            profile.hasGender = false;
        }

        profile.business = (random.nextInt(2) == 1) ? "Yes" : "No";

        if (random.nextBoolean()) {
            profile.hasAge = true;
            profile.age = NumberMapping.STRINGS[random.nextInt(15) + 30]; // HERE
        } else {
            profile.hasAge = false;
        }

        // incomes are zipfian - change this if we start to use
        // income values KT
        profile.income.clear();
        profile.income.put(String.valueOf((random.nextInt(30000) + 40000)));
        profile.income.put(".");
        profile.income.put(NumberMapping.STRINGS[random.nextInt(99)]); //  HERE

        int interests = random.nextInt(5);
        profile.interests.setSize(0);
        for (int i = 0; i < interests; i++) {
            profile.interests.add(String.valueOf(random.nextInt(NUM_CATEGORIES)));
        }
    }
}