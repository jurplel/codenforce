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
package com.tcvcog.tcvce.application;

import javax.faces.context.ExternalContext;

/**
 *
 * @author sylvia
 */
public class LoginBB extends BackingBeanUtils {

    /**
     * Creates a new instance of LoginBB
     */
    public LoginBB() {
    }
    
     public String loginToMissionControl(){
         System.out.println("LoginBB.loginToMissionControl");
         System.out.print("External User: ");
         ExternalContext ec = getFacesContext().getExternalContext();
         System.out.println(ec.getRemoteUser());
         System.out.println(ec.getAuthType());
         System.out.println(ec.getSessionId(false));
         
        
        return "startInitiationProcess";
//           return "testInit";
           
    }
    
}
