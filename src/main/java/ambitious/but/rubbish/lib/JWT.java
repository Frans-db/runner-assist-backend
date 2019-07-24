package ambitious.but.rubbish.lib;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.json.JSONObject;

import java.security.AccessControlException;
import java.security.Key;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

public class JWT {
    private KeyPair keyPair;
    private Map<String, Object> header = new HashMap<>();
    private Map<String, Object> claims = new HashMap<>();
    private boolean outgoing;
    private ResourceManager resourceManager = new ResourceManager(getClass().getResource("/server_credentials.txt"), new String[] {"type", "key"});

    /**
     * Constructor for and outgoing JWS.
     *
     */
    public JWT() {
        this.keyPair = new KeyPairBuilder(resourceManager.get("public", "key"), resourceManager.get("private", "key")).generateKeyPair();
        header.put("typ", "JWT");
        outgoing = true;
    }

    /**
     * Constructor for incoming JWS that needs to be parsed.
     *
     * @param incoming Incoming JWS
     */
    public JWT(String incoming) {
        // TODO Test with actual frontend
         this.keyPair = new KeyPairBuilder(resourceManager.get("public", "key"), resourceManager.get("private", "key")).generateKeyPair();
        Jws<Claims> jws;
        try {
            jws = Jwts.parser().setSigningKey(keyPair.getPublic()).parseClaimsJws(incoming);
            claims = new HashMap<>(jws.getBody());
            outgoing = false;
        } catch (JwtException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the claims via a map. (Cannot be Accessed if JWT is incoming)
     *
     * @param claims Map Containing Claims
     * @throws AccessControlException Throws exception if a outbound JWT is attempted to be altered.
     */
    public void addViaMap(Map<String, Object> claims) {
        if (outgoing) {
            this.claims = claims;
        } else {
            throw new AccessControlException("Cannot Alter Claims of Inbound JWT");
        }
    }

    /**
     * Add single claim via String Object. (Cannot be Accessed if JWT is incoming)
     *
     * @param claim Claim
     * @param value Associated Value
     * @throws AccessControlException Throws exception if a outbound JWT is attempted to be altered.
     */
    public void addClaim(String claim, Object value) {
        if (outgoing) {
            claims.put(claim, value);
        } else {
            throw new AccessControlException("Cannot Alter Claims of Inbound JWT");
        }
    }

    /**
     * Add Claims via JSON. (Cannot be Accessed if JWT is incoming)
     *
     * @param claims JSON Object Containing Claims
     * @throws AccessControlException Throws exception if a outbound JWT is attempted to be altered.
     */
    public void addViaJSON(JSONObject claims) {
        if (outgoing) {
            addViaMap(claims.toMap());
        } else {
            throw new AccessControlException("Cannot Alter Claims of Inbound JWT");
        }
    }

    /**
     * Gets the current claim of the JWT. (Cannot be accessed if JWT is outgoing)
     *
     * @throws AccessControlException Throws exception if JWT is attempted to be accessed when it is inbound.
     * @return Map Object of Current Claims
     */
    public Map<String, Object> claims() {
        if (!outgoing) {
            return this.claims;
        } else {
            throw new AccessControlException("Cannot Access Claims of Outbound JWT");
        }
    }

    /**
     * Gets specific claims from claims. (Cannot be accessed if JWT is outgoing)
     *
     * @param host Claim in which the Desired Claim is Nested in
     * @param claim Desired Claim
     * @throws MalformedJwtException Throws exception if host claim is not a Map object.
     * @throws AccessControlException Throws exception if JWT is attempted to be accessed when it is inbound.
     * @return Claim in an Object Instance
     */
    @SuppressWarnings("unchecked")
    public Object getClaims(String host,String claim) {
        if (!outgoing) {
            Object obj = this.claims.get(host);
            if (obj.getClass().getName().contains("HashMap")) {
                return ((Map<String, Object>) obj).get(claim);
            } else {
                throw new MalformedJwtException("Malformed Host Claims in Received JWT");
            }
        } else {
            throw new AccessControlException("Cannot Access Claims of Outbound JWT");
        }
    }

    /**
     * Gets the String representation of the sever public key.
     *
     * @return The Server Public Key in String Form
     */
    public String getAppKey() {
        return resourceManager.get("app_key", "key");
    }

    /**
     * Gets the length of the current clams
     *
     * @return Size of Claims
     */
    public int lenght() {
        return claims.size();
    }

    /**
     * Constructs the current JWT instance into a JWS to be sent, as well as adding the appropriate data to database
     *
     * @return JWS to be Sent
     */
    // TODO Move database and response handling to another class
    // TODO Make wrapper class for servlets
    public String constructJWT() {
        if (!outgoing) {
            return null;
        }
        return Jwts.builder().setHeader(header).setClaims(claims).signWith(keyPair.getPrivate()).compact();
    }
}