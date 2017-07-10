/**
 * This file is part of CERMINE project.
 * Copyright (c) 2011-2016 ICM-UW
 *
 * CERMINE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CERMINE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with CERMINE. If not, see <http://www.gnu.org/licenses/>.
 */

package pl.edu.icm.cermine.metadata.zoneclassification.features;

import java.util.Locale;
import pl.edu.icm.cermine.structure.model.BxPage;
import pl.edu.icm.cermine.structure.model.BxZone;
import pl.edu.icm.cermine.tools.classification.general.FeatureCalculator;

/**
 * @author Dominika Tkaczyk (d.tkaczyk@icm.edu.pl)
 */
public class BibinfoFeature extends FeatureCalculator<BxZone, BxPage> {

    @Override
    public double calculateFeatureValue(BxZone zone, BxPage page) {

        String[] keywords = {"cite", "pages", "article", "volume", "publishing", "journal", "doi", "cite this article",
                             "citation", "issue", "issn"};

        String[] otherKeywords = {"author details", "university", "department", "school", "institute", "affiliation", 
                             "hospital", "laboratory", "faculty", "author", "abstract", "keywords", "key words",
                             "correspondence", "editor", "address", "email"};

        
        int count = 0;
        for (String keyword : keywords) {
            if (zone.toText().toLowerCase(Locale.ENGLISH).contains(keyword)) {
                count += 2;
            }
        }
        for (String keyword : otherKeywords) {
            if (count > 0 && zone.toText().toLowerCase(Locale.ENGLISH).contains(keyword)) {
                count--;
            }
        }

        return (double)count / 2;
    }

}
