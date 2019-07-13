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
package com.tcvcog.tcvce.integration;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.MalformedBOBException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseEvent;
import com.tcvcog.tcvce.entities.Choice;
import com.tcvcog.tcvce.entities.ChoiceEventCat;
import com.tcvcog.tcvce.entities.Directive;
import com.tcvcog.tcvce.entities.ChoiceEventPageNavigation;
import com.tcvcog.tcvce.entities.ChoiceEventRule;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.Event;
import com.tcvcog.tcvce.entities.Proposable;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Choice is given to the user in a Directive and can take one of the
 following forms:
 An EventCategory
 An EventRule
 A page redirection via JSF navigation subsystem
 * @author sylvia
 */
public class ChoiceIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of ChoiceIntegrator
     */
    public ChoiceIntegrator() {
    }
    
    public Choice getChoice(int choiceID) throws IntegrationException, MalformedBOBException{
        
       Choice c = null;
  
        StringBuilder sb = new StringBuilder();
        sb.append(  " SELECT choiceid, title, description, eventcat_catid, addeventcat, eventrule_ruleid, \n" +
                    "       addeventrule, relativeorder, active, minimumrequireduserranktoview, \n" +
                    "       minimumrequireduserranktochoose, icon_iconid, worflowpagetriggerconstantvar\n" +
                    "  FROM public.choice WHERE choiceid = ?;\n");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, choiceID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                c = generateChoice(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event proposal response", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return new ChoiceEventCat();
    }
    
    public List<Proposable> getChoiceList(int directiveID){
        
        return new ArrayList<>();
        
    }
    
  
    
    
    private Choice generateChoice(ResultSet rs) throws SQLException, MalformedBOBException, IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        Choice choice;
        
        // CRUDE FIRST DFRAFT AT DECIDING WHAT SUB-TYPE OF CHOICE TO MAKE
        // USE WHICHEVER NONZERO CONTENT COLUMN (EVENTCAT, EVENTRULE, OR NAV)
        // I ENCOUNTER FIRST
        if(rs.getInt("eventcat_catid") != 0) { // This choice will propose an event of a given category
            ChoiceEventCat choiceEvCat = new ChoiceEventCat();
            choiceEvCat.setEventCategory(ei.getEventCategory(rs.getInt("eventcat_catid")));
            choiceEvCat.setAddCategoryFuncSwitch(rs.getBoolean("addeventcat"));
            choice = choiceEvCat;
        } else if (rs.getInt("eventrule_ruleid") != 0) {
            ChoiceEventRule choiceEvRule = new ChoiceEventRule();
            choiceEvRule.setRule(ei.getEventRule(rs.getInt("eventrule_ruleid")));
            choiceEvRule.setAddRuleFuncSwitch(rs.getBoolean("addeventrule"));
            choice = choiceEvRule;
        } else if (rs.getString("worflowpagetriggerconstantvar") != null){
            ChoiceEventPageNavigation choiceNav = new ChoiceEventPageNavigation();
            choiceNav.setNavigationKeyConstant(rs.getString("worflowpagetriggerconstantvar"));
            choice = choiceNav;
        } else {
            throw new MalformedBOBException("Choice does not have any content!");
        }
        
        choice.setChoiceID(rs.getInt("choiceid"));
        choice.setTitle(rs.getString("title"));
        choice.setDescription(rs.getString("description"));
        choice.setActive(rs.getBoolean("active"));
        choice.setMinimumRequiredUserRankToView(rs.getInt("minimumrequireduserranktoview"));
        choice.setMinimumRequiredUserRankToChoose(rs.getInt("minimumrequireduserranktochoose"));
        choice.setIcon(si.getIcon(rs.getInt("icon_iconid")));
        choice.setRelativeOrder(rs.getInt("relativeorder"));
        return choice;
    }
    
    public Proposal getProposal(int propID) throws IntegrationException{
        Proposal prop = null;
        StringBuilder sb = new StringBuilder();
        sb.append(  "SELECT proposalid, directive_directiveid, generatingevent_cecaseeventid, \n" +
                    "       initiator_userid, responderintended_userid, activateson, expireson, \n" +
                    "       responderactual_userid, rejectproposal, responsetimestamp, responseevent_cecaseeventid, \n" +
                    "       active, notes, relativeorder, hidden, generatingevent_occeventid, \n" +
                    "       responseevent_occeventid, occperiod_periodid, cecase_caseid\n" +
                    "  FROM public.choiceproposal WHERE proposalid=?;");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, propID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                prop = generateProposal(rs);
                
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event proposal response", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return prop;
        
    }
    
    public List<Proposal> getProposalList(CECase cse) throws IntegrationException{
        List<Proposal> proposalList = new ArrayList<>();
  
        StringBuilder sb = new StringBuilder();
        sb.append(  "SELECT proposalid\n" +
                    "  FROM public.choiceproposal\n" +
                    "  WHERE cecase_caseid=?;");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, cse.getCaseID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                proposalList.add(getProposal(rs.getInt("proposalid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event proposal response", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return proposalList;
        
    }
    
    
    public List<Proposal> getProposalList(OccPeriod occPer) throws IntegrationException{
        
         List<Proposal> proposalList = new ArrayList<>();
  
        StringBuilder sb = new StringBuilder();
        sb.append(  "SELECT proposalid\n" +
                    "  FROM public.choiceproposal\n" +
                    "  WHERE occperiod_periodid=?;");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, occPer.getPeriodID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                proposalList.add(getProposal(rs.getInt("proposalid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event proposal response", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return proposalList;
    }
    
    /**
     * TODO: complete for occbeta
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
     private Proposal generateProposal(ResultSet rs) throws SQLException, IntegrationException{
        Proposal prop = new Proposal();
        UserIntegrator ui = getUserIntegrator();
        EventIntegrator ei = getEventIntegrator();
        
        prop.setProposalID(rs.getInt("proposalid"));
        prop.setDirective(getDirective(rs.getInt("directive_directiveid")));
        if(rs.getInt("generatingevent_cecaseeventid") != 0){
            prop.setGeneratingEvent(ei.getEventCECase(rs.getInt("generatingevent_cecaseeventid")));
        }
        if(rs.getInt("generatingevent_occeventid") != 0){
            prop.setGeneratingEvent(ei.getOccEvent(rs.getInt("generatingevent_occeventid")));
        }
        if(rs.getInt("responseevent_cecaseeventid") != 0){
            prop.setResponseEvent(ei.getEventCECase(rs.getInt("responseevent_cecaseeventid")));
        }
        if(rs.getInt("responseevent_occeventid") != 0){
            prop.setResponseEvent(ei.getOccEvent(rs.getInt("responseevent_occeventid")));
        }
               
        prop.setInitiator(ui.getUser(rs.getInt("initiator")));
        prop.setResponderIntended(ui.getUser(rs.getInt("responderintended_userid")));
        if(rs.getTimestamp("activateson") != null){
            prop.setActivatesOn(rs.getTimestamp("activateson").toLocalDateTime());
        }
        if(rs.getTimestamp("expireson") != null){
            prop.setExpiresOn(rs.getTimestamp("expireson").toLocalDateTime());
        }
        
        prop.setResponderActual(ui.getUser(rs.getInt("responder_userid")));
        prop.setProposalRejected(rs.getBoolean("rejectproposal"));
        if(rs.getTimestamp("responsetimestamp") != null){
            prop.setResponseTimestamp(rs.getTimestamp("responsetimestamp").toLocalDateTime());
        }
        
        prop.setActive(rs.getBoolean("active"));
        prop.setNotes(rs.getString("notes"));
        prop.setOrder(rs.getInt("relativeorder"));
        prop.setHidden(rs.getBoolean("hidden"));
        
        prop.setCecaseID(rs.getInt("cecase_caseid"));
        prop.setOccperiodID(rs.getInt("occperiod_periodid"));
        
        return prop;
    }
    
    public void updateProposal(Proposal imp) throws IntegrationException{
          String query =    "UPDATE public.ceeventproposalimplementation\n" +
                            "   SET proposal_propid=?, generatingevent_eventid=?, \n" +
                            "       initiator_userid=?, responderintended_userid=?, activateson=?, \n" +
                            "       expireson=?, expiredorinactive=?, notes=?\n" +
                            " WHERE implementationid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(2, imp.getGeneratingEventID());
            stmt.setInt(3, imp.getInitiator().getUserID());
            stmt.setInt(4, imp.getResponderIntended().getUserID());
            
            if(imp.getActivatesOn() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(imp.getActivatesOn()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(imp.getExpiresOn() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(imp.getExpiresOn()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            stmt.setString(7, imp.getNotes());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot udpate event proposal implementation, sorry", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
        
    }
    
    public void logResponseToProposal(CECaseEvent ev) throws IntegrationException {

       String query = "UPDATE public.ceeventproposalimplementation\n" +
                        "   SET responderactual_userid=?, rejectproposal=?, responsetimestamp=?, \n" +
                        "       responseevent_eventid=?, notes=?\n" +
                        " WHERE implementationid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        Proposal imp;
        
//        if(ev.getEventProposalImplementation()!= null){
//            imp = ev.getEventProposalImplementation();
//
//            try {
//
//                stmt = con.prepareStatement(query);
//                stmt.setInt(1, imp.getResponderActual().getUserID());
//                stmt.setBoolean(2, imp.isProposalRejected());
//                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(imp.getResponseTimestamp()));
//                int responseEventID;
//                if(imp.getResponseEvent()!= null){
//                    responseEventID = imp.getResponseEvent().getEventID();
//                    stmt.setInt(4, responseEventID);
//                } else {
//                    stmt.setNull(4, java.sql.Types.NULL);
//                }
//                stmt.setString(5, imp.getNotes());
//                stmt.executeUpdate();
//            } catch (SQLException ex) {
//                System.out.println(ex.toString());
//                throw new IntegrationException("Cannot udpate event proposal implementation, sorry", ex);
//
//            } finally {
//                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
//                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
//            }
//        }
    }
    
    
    
  
    public Directive getDirective(int directiveID) throws IntegrationException{

        Directive proposal = new Directive();
        
        StringBuilder sb = new StringBuilder();
        sb.append(      "SELECT directiveid, title, overalldescription, creator_userid, directtodefaultmuniceo, \n" +
                        "       directtodefaultmunistaffer, directtodeveloper, executechoiceiflonewolf, \n" +
                        "       applytoclosedentities, instantiatemultiple, inactivategeneventoneval, \n" +
                        "       maintainreldatewindow, autoinactivateonbobclose, autoinactiveongeneventinactivation, \n" +
                        "       minimumrequireduserranktoview, minimumrequireduserranktoevaluate, \n" +
                        "       active, icon_iconid, relativeorder, directtomunisysadmin, requiredevaluationforbobclose, \n" +
                        "       forcehideprecedingproposals, forcehidetrailingproposals, refusetobehidden\n" +
                        "  FROM public.choicedirective WHERE directiveid=?");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, directiveID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                proposal = generateDirective(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive EventProposal", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return proposal;
        
        
    }
    
    /**
     
     * 
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private Directive generateDirective(ResultSet rs) throws SQLException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        SystemIntegrator si = getSystemIntegrator();

        Directive dir = new Directive();
        
        dir.setTitle(rs.getString("title"));
        dir.setDescription(rs.getString("overalldescription"));
        dir.setCreator(ui.getUser(rs.getInt("creator_userid")));
        dir.setDirectPropToDefaultMuniCEO(rs.getBoolean("directtodefaultmuniceo"));
        
        dir.setDirectPropToDefaultMuniStaffer(rs.getBoolean("directproposaltodefaultmunistaffer"));
        dir.setDirectPropToDeveloper(rs.getBoolean("directproposaltodeveloper"));
        dir.setExecuteChoiceIfLoneWolf(rs.getBoolean("executechoiceiflonewolf"));
        
        dir.setApplyToClosedBOBs(rs.getBoolean("applytoclosedentities"));
        dir.setInstantiateMultipleOnBOB(rs.getBoolean("instantiatemultiple"));
        dir.setInactivateGeneratingEventOnEvaluation(rs.getBoolean("inactivategeneventoneval"));
        
        dir.setMaintainRelativeDateWindow(rs.getBoolean("maintainreldatewindow"));
        dir.setAutoInactiveOnBOBClose(rs.getBoolean("autoinactivateonbobclose"));
        dir.setAutoInactiveOnGenEventInactivation(rs.getBoolean("autoinactiveongeneventinactivation"));
        
        dir.setMinimumRequiredUserRankToView(rs.getInt("minimumrequireduserranktoview"));
        dir.setMinimumRequiredUserRankToEvaluate(rs.getInt("minimumrequireduserranktoevaluate"));
        
        dir.setActive(rs.getBoolean("active"));
        dir.setIcon(si.getIcon(rs.getInt("icon_iconid")));
        dir.setRelativeorder(rs.getInt("relativeorder"));
        dir.setDirectPropToMuniSysAdmin(rs.getBoolean("directtomunisysadmin"));
        dir.setRequiredEvaluationForBOBClose(rs.getBoolean("requiredevaluationforbobclose"));
        
        dir.setForceHidePrecedingProps(rs.getBoolean("forcehideprecedingproposals"));
        dir.setForceHideTrailingProps(rs.getBoolean("forcehidetrailingproposals"));
        dir.setRefuseToBeHidden(rs.getBoolean("refusetobehidden"));
        
        dir.setChoiceList(getChoiceList(dir.getDirectiveID()));
        
        return dir;
    }
    
    public void insertDirective(Directive dir) throws IntegrationException{
         String query = "INSERT INTO public.choicedirective(\n" +
                        "            directiveid, title, overalldescription, creator_userid, directtodefaultmuniceo, \n" +
                        "            directtodefaultmunistaffer, directtodeveloper, executechoiceiflonewolf, \n" +
                        "            applytoclosedentities, instantiatemultiple, inactivategeneventoneval, \n" +
                        "            maintainreldatewindow, autoinactivateonbobclose, autoinactiveongeneventinactivation, \n" +
                        "            minimumrequireduserranktoview, minimumrequireduserranktoevaluate, \n" +
                        "            active, icon_iconid, relativeorder, directtomunisysadmin, requiredevaluationforbobclose, \n" +
                        "            forcehideprecedingproposals, forcehidetrailingproposals, refusetobehidden)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, dir.getTitle());
            stmt.setString(2, dir.getDescription());
            stmt.setInt(3, dir.getCreator().getUserID());
            stmt.setBoolean(4, dir.isDirectPropToDefaultMuniCEO());
            
            stmt.setBoolean(5, dir.isDirectPropToDefaultMuniStaffer());
            stmt.setBoolean(6, dir.isDirectPropToDeveloper());
            stmt.setBoolean(7, dir.isExecuteChoiceIfLoneWolf());
            
            stmt.setBoolean(8, dir.isApplyToClosedBOBs());
            stmt.setBoolean(9, dir.isInstantiateMultipleOnBOB());
            stmt.setBoolean(10, dir.isInactivateGeneratingEventOnEvaluation());
            
            stmt.setBoolean(11, dir.isMaintainRelativeDateWindow());
            stmt.setBoolean(12, dir.isAutoInactiveOnBOBClose());
            stmt.setBoolean(13, dir.isAutoInactiveOnGenEventInactivation());
            
            stmt.setInt(14, dir.getMinimumRequiredUserRankToView());
            stmt.setInt(15, dir.getMinimumRequiredUserRankToEvaluate());
            
            stmt.setBoolean(16, dir.isActive());
            stmt.setInt(17, dir.getIcon().getIconid());
            stmt.setInt(18, dir.getRelativeorder());
            stmt.setBoolean(19, dir.isDirectPropToMuniSysAdmin());
            stmt.setBoolean(20, dir.isRequiredEvaluationForBOBClose());
            
            stmt.setBoolean(21, dir.isForceHidePrecedingProps());
            stmt.setBoolean(22, dir.isForceHideTrailingProps());
            stmt.setBoolean(23, dir.isRefuseToBeHidden());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert directive", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
   
    
   

    public void insertChoiceProposal(Proposal prop) throws MalformedBOBException, IntegrationException{
        String query = "INSERT INTO public.ceeventproposalimplementation(\n" +
                        "            implementationid, proposal_propid, generatingevent_eventid, initiator_userid, \n" +
                        "            responderintended_userid, activateson, expireson, responderactual_userid, \n" +
                        "            rejectproposal, responsetimestamp, responseevent_eventid, expiredorinactive, \n" +
                        "            notes)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?);";

        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(2, prop.getGeneratingEventID());
            
            if(prop.getInitiator() != null){
                stmt.setInt(3, prop.getInitiator().getUserID());
            } else { 
                throw new MalformedBOBException("EventProposalImplementations must contain a User object as an initiator");
            }
            
            if(prop.getResponderIntended()!= null){
                stmt.setInt(4, prop.getResponderIntended().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(prop.getActivatesOn()));
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(prop.getExpiresOn()));
            
            if(prop.getResponderActual() != null){
                stmt.setInt(7, prop.getResponderActual().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(8, prop.isProposalRejected());
            
            if(prop.getResponseTimestamp() != null){
                stmt.setTimestamp(9, java.sql.Timestamp.valueOf(prop.getResponseTimestamp()));
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }

//            stmt.setInt(10, prop.getResponseEventID());
            stmt.setString(12, prop.getNotes());
            
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert EventProposal", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
    private Proposal generateProposal(ResultSet rs, Directive dir){
        Proposal proposal = new Proposal();
        
        
        return proposal;
        
    }

    
    
    
}
