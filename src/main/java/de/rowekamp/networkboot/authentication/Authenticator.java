package de.rowekamp.networkboot.authentication;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Locale;

import de.rowekamp.networkboot.database.AuthDatabase;
import de.rowekamp.networkboot.ipxeBootScript.handler.ParseFunctions;

public class Authenticator {

	private AuthDatabase db;
	public final int maxFailedLogins, blockingTimeIncreaseMinutes;
	private ParseFunctions pf = new ParseFunctions();

	public Authenticator(File dbFile, int maxFailedLogins, int blockingTimeIncreaseMinutes) {
		this.db = new AuthDatabase(dbFile);
		this.maxFailedLogins = maxFailedLogins;
		this.blockingTimeIncreaseMinutes = blockingTimeIncreaseMinutes;
	}

	/**
	 * Authenticates a user on the backend from a specific pc
	 * @param macUnparsed
	 * @param username
	 * @param password
	 * @return 2 if mac was invalid, 1 if user tried to login too often, 0 if login succeeded, 3 if username/password were wrong, 4 and 5 if errors occurred during authentication, 6 if username/password weren't transfered correctly, 7 if host wasn't allowed to login, 8 if an database error occurred.  
	 */
	public int authenticate(String macUnparsed, String username, String password) {
		String mac = pf.parseMac(macUnparsed);
		if (mac == null){ //Mac invalid --> Error
			return 2;
		}
		try{
			if(db.isHostAllowedToLogin(mac)){ //If host is allowed to login, try to authenticate
				if(username != null && password != null){ //If a password and username were transmitted --> login request
					if(db.getFailedLoginAttempts(username) >= maxFailedLogins && !blockingTimeReached(username, db)){ //User tried to login too often --> Blocking message
						return 1;
					}else{ //Check if username and password match
						String hashedPassword = db.getHashedPassword(username);
							try {
								if(hashedPassword != null && PasswordHash.validatePassword(password, hashedPassword)){//login successful
									db.resetFailedLoginAttempts(username);
									return 0;
								}else{//login failed
									db.increaseFailedLogins(username);
									increaseBlockingEndTime(username,db);
									return 3;
								}
							} catch (NoSuchAlgorithmException e) {
								e.printStackTrace();
								return 4;
							} catch (InvalidKeySpecException e) {
								e.printStackTrace();
								return 5;
							} 
					}
				}else{ //Username or password weren't transferred correctly --> Login Page
					return 6;
				}
			}else{ //Host is not allowed to login --> forward to BootScriptHandler
				return 7;
			}
		}catch(SQLException e){ //Database error occured
			e.printStackTrace();
			return 8;
		}
	}
	
	/**
	 * Authenticates a user from the frontend.
	 * @return 0 if login succeeded, 1 if username/password were wrong, 2 if user tried to login too often, 3 if user wasn't allowed to login, 4 if username and password weren't transmitted correctly, 5 and 6 if errors occurred during authentication, 7 if an database error occurred
	 */
	public int frontendAuthentication(String username, String password){
		if(username != null && password != null){ //If a password and username were transmitted --> login request
			try{
				if(db.isUserAllowedToLogin(username)){ //If user is allowed to login, try to authenticate
						if(db.getFailedLoginAttempts(username) >= maxFailedLogins && !blockingTimeReached(username, db)){ //User tried to login too often --> Blocking message
							return 2;
						} else{	//Check if username and password match
							String hashedPassword = db.getHashedPassword(username);
								try {
									if(hashedPassword != null && PasswordHash.validatePassword(password, hashedPassword)){//login successful
										db.resetFailedLoginAttempts(username);
										return 0;
									}else{//login failed
										db.increaseFailedLogins(username);
										increaseBlockingEndTime(username,db);
										return 1;
									}
								} catch (NoSuchAlgorithmException e) {
									e.printStackTrace();
									return 5;
								} catch (InvalidKeySpecException e) {
									e.printStackTrace();
									return 6;
								}
						}
				}else{ //User is not allowed to login (frontend)
					return 3;
				}
			}
			catch(SQLException e){ //Database error occured
				e.printStackTrace();
				return 7;
			}
		}else{//Username and password weren't transmitted correctly
			return 4;
		}
	}
	
	/**
	 * Closes the database
	 */
	public void close(){
		db.close();
	}
	
	/**
	 * Checks if the blocking time is reached and the user can log in again.
	 * @param db
	 * @return
	 */
	private boolean blockingTimeReached(String username, AuthDatabase db) throws SQLException{
		boolean reached = false;
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		if(cal.getTimeInMillis()-db.getBlockingEndTime(username)>0) reached=true;		
		return reached;
	}
	
	/**
	 * Increases the blocking end time about blockingTimeIncreaseMinutes min.
	 * @param username
	 * @param db
	 */
	private void increaseBlockingEndTime(String username, AuthDatabase db) throws SQLException{
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		long blockingEndTime;
		if (blockingTimeIncreaseMinutes > 0) blockingEndTime = cal.getTimeInMillis() + blockingTimeIncreaseMinutes*60*1000;
		else blockingEndTime = cal.getTimeInMillis() + 30*60*1000;
		db.updateBlockingEndTime(username, blockingEndTime);
	}
}
