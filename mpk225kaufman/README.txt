CSE341 Project - Hotel California
Michael Kaufman

------------------------------------

Build Instructions:
	To run the executable, the command can be run "java -jar mpk225.jar" in the top level directory (current directory).
	In the event this does not work, the source code for the project, along with a manifest and the intended version of ojdbc jar file
	is in the mpk225 subdirectory.  This executable was produced on my personal machine, but the code was developed and tested in the 
	sunlab development environment.

Directory Structure:
	The top level directory, "mpk225kaufman" contains the "mpk225" subdirectory, the executable ("mpk225.jar"), the intended version of ojdbc and this README
	that you are reading (lol).  In the "mpk225" subdirectory, the source code for the executable is present ("mpk225.java") as well
	as the manifest for compilation.


Application Description:
	Front Desk Clerk Interface:
		0. Past Customer - Before being brought to the menu for this interface, the clerk must indicate whether or not they are dealing with a past customer.
		If this is the case, the customer's address and name are taken and then queried for in the database.  If they are in the customer table, the menu appears;
		If not, they are prompted for name and address and added to the customer table in the database.

		1. Check In -  For this option, the customer's reserved start date is entered, and it is compared to the system local date.  If these dates match
		then the a stored procedure is called to add the customer and their info to the check in table in the database.  Else, the transaction is aborted and the
		customer is turned away.  In the case of a successful check in, the stored procedure automatically assigns an empty room to the customer.

		2. Check Out - For this option, the customer's reserved end of stay date is entered and it is compared to the system local date.  If these dates do not
		match, the customer may not check out (You can check in anytime you like, but you can never leave).  In the case of matching dates, the customer is added
		to the check out table and their previous room is set to be clean (via the Room table cleanoccupiedbool attribute).  Since, the front desk clerk is assumed
		to have some training with the interface, it is their job then to select the pay/transaction interface for the customer to settle up their calculated amount due.  

		3. Cancellation - For this option, the customer's info is entered.  A query is then made to see if there is a reservation in the system matching the info.  The customer is 
		then placed in the cancellation table.  Note: this does not negate their reservation table row, but is checked for in the check in stored procedure (that the customer is not in
		the cancellation table).

		4. Pay - For this option, the customer's calculated amount due (in Reservation table) is found and used for a new entry in the transaction table.  
		Payment is intended to indicate end of stay, so their room is then indicated to not be occupied and unclean in the Room table.  It is assumed the actual 
		exchange of currency or points is done via an external payment device/interface.

		5. See Availability - For this option, a list of properties is displayed.  Once a property is chosen, room types and corresponding room numbers are displayed.
		Once a room number is chosen, the option indicates whether the room is available or not.

		6. Exit - This option closes the interface and application.

	Housekeeping Interface:
		1. Mark Room As Clean - This option is used by custodial services to mark a room as clean in the database.

		2. View Unclean Rooms - This option is used by custodial services to view a list of unclean rooms at a specified property (property is prompted for).

		3. Exit - This option closes the interface and application.

	Customer Interface:
		0. Past Customer - Before being brought to the menu for this interface, the customer must indicate whether or not they are a past customer.
		If this is the case, the customer's address and name are taken and then queried for in the database.  If they are in the customer table, the menu appears;
		If not, they are prompted for name and address and added to the customer table in the database.

		1. Make Reservation - This option is used by a customer to reserve a type of room at a property for a date in the future.  The hotel is very popular so only
		dates in 2024 and the future can be reserved.  Note the Y2K date format, thus new reservation with YY 45 are not 1945 but 2045.  Once valid input is received
		an associated stored procedure is called and an entry is added to the reservation table in the database.

		2. Pay - For this option, the customer's calculated amount due (in Reservation table) is found and used for a new entry in the transaction table.  
		Payment is intended to indicate end of stay, so their room is then indicated to not be occupied and unclean in the Room table.  It is assumed the actual 
		exchange of currency or points is done via an external payment device/interface.

		3. See Availability - For this option, a list of properties is displayed.  Once a property is chosen, room types and corresponding room numbers are displayed.
		Once a room number is chosen, the option indicates whether the room is available or not.  Note that this shows current availability of rooms, not availability
		for a future date; this option is intended only for potential customers to gauge occupancy throughout the year at a specific property.

		4. Join Frequent Guest Program - This option takes the customer and adds them to the frequent guest program.  The customer's subsequent reservation will 
		be made with a rate related to their membership of the program.

		5. Exit - This option closes the interface and application. 


Code Description:
	The java code consists of about 1300 lines of simple functions related to interface menu options and gathering valid information.  Each function has
	associated javadoc comments, describing use, arguments and return values.  Certain functions make an additional connection to the database for a query, and 
	also close said connection.  The code has fields username, password, userinterfaces, userinterface, and currentdate.  All uses can be deduced by name except for userInterfaces and
	userInterface; these are used to check user's choice of interface for one iteration of big executable loop.  Many various stored procedures have been implemented
	in PLSQL in the database, each relating to the population and changing of entries in the database.  Java functions that interact with the stored procedures
	either call them with a regular statement with java checked arguments, or with prepared statements that use jdbc features to ward off possible SQL injection attacks. 


Database Design:
	Database tables include
	- Ammenity - list of ammenities for a specific property
	- Cancellation - cancelled reservations
	- Check_in - list of checked in customers
	- Check_out - list of checked out customers
	- Cost - cost of certain rooms and types for certain periods of time
	- Customer - list of past and current customers at hotel chain
	- Member - list of customers enrolled in frequent guest program
	- Payment_type - types of avenues for customer payment
	- Property - list of properties and associated information
	- Rate - rates for various rooms and types
	- Reservation - list of reservations past and present for hotel chain
	- Room - list of rooms for every property in hotel chain
	- Room_type - list of possible types of room
	- Transaction - list of transactions regarding customer payment for hotel chain

	NOTE: while all tables are populated, not all ended up being used for interface.


Assumptions:
	- The front desk clerk is trained and knows what order to use the options in when dealing with a customer.
	- While there is no way to query ammenities, the customer can look up associated ones with some website (not included in project).
	- Hotel is all inclusive; no additional cost for use of ammenities.
	- Dateofstay in customer table is the date of their first stay at the hotel chain.
	- There is no checks for a proper address format (the Batcave is a vaild address).  We do not discrimate address at Hotel California.
	- Cancellations do not delete reservation rows because full price is still expected to be paid in case of cancellation.
	- The cleanoccuppiedbool attribute of Room table is the concatentaion of a double boolean.  0 or 00 indicates not occupied and dirty, 1 or 01 indicated occupied and dirty,
	10 indicated clean and not occupied, and 11 indicated occupied and clean.  This is revolutionary and used throughout interface database interaction.
	- I forgot SQL had their own date format variable type so I made my own for this project => a string that is formatted MM-DD-YY (E.G. "11-1-77")

Notes to Grader/Limitations/Oddities:
	- No accountability for dishonest customers.  No way for a clerk to query reservations, thus it is assumed customers are honest when divulging check in information
	- There is nointerface that interacts with ammenities, I did not know how to this ...
	- Not all functions that execute updates via stored procedures use prepared statments.  However, ones that do not extensively check their arguments in the java program.
	Ones that do are more complicated input like address or name (Customer related functions use prepared statments)
	- Some foreign key constraints have been disabled in the database either during data population (for ease of population) or during testing because
	they made the program blow up.  While some foreign key constraints have been disbaled, stored procedures related to tables ensure that primary keys and checks
	are made in order to preserve indicated database design in ER diagram submitted.
	- Not all tables in ER diagram are present in actual database becuase, theory aside, some were useless or redundant in context of the user interfaces.

