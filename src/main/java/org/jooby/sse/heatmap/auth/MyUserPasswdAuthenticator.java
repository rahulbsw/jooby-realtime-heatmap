package org.jooby.sse.heatmap.auth;

import org.jooby.Jooby;
import org.jooby.sse.heatmap.dao.AuthDB;
import org.jooby.sse.heatmap.domain.User;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;

/**
 * Created by rjain on 6/27/17.
 */
public class MyUserPasswdAuthenticator extends Jooby implements Authenticator<UsernamePasswordCredentials> {
    private AuthDB authDB = new AuthDB();

    @Override
    public void validate(UsernamePasswordCredentials credentials, WebContext context) throws HttpAction, CredentialsException {
        {
            if (credentials == null) {
                throw new CredentialsException("No credential");
            }
            String username = credentials.getUsername();
            String password = credentials.getPassword();
            if (CommonHelper.isBlank(username)) {
                throw new CredentialsException("Username cannot be blank");
            }
            if (CommonHelper.isBlank(password)) {
                throw new CredentialsException("Password cannot be blank");
            }

            User u = authDB.findByUserPass(username, password);

            if (u == null) {
                throw new CredentialsException("Username : '" + username + "' does not match password");
            }

            CommonProfile profile = new CommonProfile();
            profile.setId(username);
            profile.addAttribute(Pac4jConstants.USERNAME, username);
            profile.addAttribute("Userid", u.getUsername());


//            //not in line with documentation
//            //http://www.pac4j.org/1.9.x/docs/clients.html#compute-roles-and-permissions
//
//            String roles=u.getRoles();
//            if (roles!=null && roles.length()>0)
//            {
//                for (String role: roles.split(",")) {
//                    profile.addRole(role);
//                }
//            }

            credentials.setUserProfile(profile);
            //session.set("userobject", u.toJson());
            credentials.setClientName("heatmap");
        }
    }
}