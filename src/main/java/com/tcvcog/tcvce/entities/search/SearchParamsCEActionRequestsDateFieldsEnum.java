/*
 * Copyright (C) 2019 Technology Rediscovery LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.entities.search;

/**
 * Utility enum for allowing users to choose a date field for building query
 * search param objects. Contains direct mapping of DB field names to 
 * human friendly descriptions of dates available
 * 
 * @author ellen bascomb
 */
public  enum SearchParamsCEActionRequestsDateFieldsEnum 
        implements IFace_dateFieldHolder {
    
    SUBMISSION_TS              ("Insertion timestamp", 
                                "submittedtimestamp"), 
    
    SUBMISSION_DOR               ("Date of record",
                                "ceactionrequest.dateofrecord"), 
    
    CASEATTACHMENT_TS           ("Case attachment timestamp", 
                                "caseattachmenttimestamp");
    
    private final String title;
    private final String dbField;
    
    private SearchParamsCEActionRequestsDateFieldsEnum(String t, String db){
        title = t;
        dbField = db;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    @Override
    public String extractDateFieldString() {
        return dbField;
    }
    
}
